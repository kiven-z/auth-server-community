package com.auth.service.system.admin.service.admin.impl;

import com.auth.common.data.model.PageResponse;
import com.auth.service.system.admin.convert.admin.user.UserDeptConverter;
import com.auth.service.system.admin.exception.SystemAdminResultCode;
import com.auth.service.system.admin.mapper.admin.user.UserDeptMapper;
import com.auth.service.system.admin.model.entity.UserDeptEntity;
import com.auth.service.system.admin.model.form.user.UserDeptAssignForm;
import com.auth.service.system.admin.model.po.user.UserDeptPageRowPO;
import com.auth.service.system.admin.model.query.user.UserDeptPageQuery;
import com.auth.service.system.admin.model.vo.user.UserDeptPageVO;
import com.auth.service.system.admin.service.admin.SysUserDeptService;
import com.auth.service.system.admin.support.dept.DeptReferenceChecker;
import com.auth.service.system.admin.support.user.UserOrgRelationBatchRemoveSupport;
import com.auth.service.system.admin.support.user.UserReferenceChecker;
import com.auth.service.system.authorization.dispatch.trigger.UserAuthorizationInvalidationTrigger;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.common.exception.code.SystemCommonResultCode;
import com.auth.service.system.common.service.AuditUserDisplayService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

/**
 * 用户部门关联服务实现
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class SysUserDeptServiceImpl extends ServiceImpl<UserDeptMapper, UserDeptEntity> implements SysUserDeptService {

	private final UserReferenceChecker userReferenceChecker;

	private final DeptReferenceChecker deptReferenceChecker;

	private final UserAuthorizationInvalidationTrigger userAuthorizationInvalidationTrigger;

	private final AuditUserDisplayService auditUserDisplayService;

	private final UserOrgRelationBatchRemoveSupport userOrgRelationBatchRemoveSupport;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public PageResponse<UserDeptPageVO> getPage(Long userId, UserDeptPageQuery query) {
		userReferenceChecker.getExistingActive(userId);

		Page<UserDeptEntity> pageParams = new Page<>(query.getPageIndex(), query.getPageSize());
		IPage<UserDeptPageRowPO> page = baseMapper.selectListByPage(pageParams, userId, query);
		IPage<UserDeptPageVO> voPage = page.convert(UserDeptConverter.INSTANCE::toPageVo);

		auditUserDisplayService.enrichAuditUsernames(voPage, null, null);
		return PageResponse.of(voPage);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void create(Long userId, UserDeptAssignForm form) {
		Long deptId = form.getDeptId();
		userReferenceChecker.getExistingActive(userId);
		userReferenceChecker.requireOperable(List.of(userId));
		deptReferenceChecker.requireEffective(deptId);

		if (baseMapper.countByUserIdAndDeptId(userId, deptId) > 0) {
			log.warn("create user department relation duplicate: userId={}, deptId={}", userId, deptId);
			throw new SystemBusinessException(SystemAdminResultCode.USER_DEPT_DUPLICATE);
		}

		if (form.getIsPrimary() != null && form.getIsPrimary()) {
			baseMapper.demotePrimaryByUserId(userId);
		}

		UserDeptEntity entity = UserDeptConverter.INSTANCE.toEntity(form);
		entity.setUserId(userId);
		save(entity);

		userAuthorizationInvalidationTrigger.submitByUserIds(List.of(userId), "create-dept");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void update(Long userId, Long id, UserDeptAssignForm form) {
		userReferenceChecker.getExistingActive(userId);
		userReferenceChecker.requireOperable(List.of(userId));

		UserDeptEntity existing = baseMapper.selectByIdAndUserId(id, userId);
		if (existing == null) {
			log.warn("update user department relation not found: id={}", id);
			throw new SystemBusinessException(SystemCommonResultCode.DATA_NOT_EXIST);
		}

		if (!Objects.equals(existing.getDeptId(), form.getDeptId())) {
			deptReferenceChecker.requireEffective(form.getDeptId());

			UserDeptEntity conflict = baseMapper.selectByUserIdAndDeptId(userId, form.getDeptId());
			if (conflict != null && !Objects.equals(conflict.getId(), id)) {
				log.warn("update user department relation duplicate: userId={}, deptId={}", userId, form.getDeptId());
				throw new SystemBusinessException(SystemAdminResultCode.USER_DEPT_DUPLICATE);
			}
		}

		if (form.getIsPrimary() != null && form.getIsPrimary()) {
			baseMapper.demotePrimaryByUserId(userId);
		}
		UserDeptConverter.INSTANCE.applyUpdateForm(form, existing);
		updateById(existing);
		userAuthorizationInvalidationTrigger.submitByUserIds(List.of(userId), "update-dept");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void removeBatch(Long userId, List<Long> ids) {
		userOrgRelationBatchRemoveSupport.removeBatch(userId, ids, this::removeByIds, "delete-dept");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void removeAll(Long userId) {
		userReferenceChecker.getExistingActive(userId);
		userReferenceChecker.requireOperable(List.of(userId));

		if (baseMapper.deleteByUserId(userId) > 0) {
			userAuthorizationInvalidationTrigger.submitByUserIds(List.of(userId), "clear-dept");
		}
	}

}
