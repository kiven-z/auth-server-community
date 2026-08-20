package com.auth.service.system.admin.service.admin.impl;

import cn.hutool.core.collection.CollUtil;
import com.auth.common.core.constants.BatchSizes;
import com.auth.common.core.model.form.IdsEnableStatusForm;
import com.auth.common.core.utils.FieldChangeSupport;
import com.auth.common.data.model.PageResponse;
import com.auth.common.data.support.BusinessKeyAssert;
import com.auth.service.system.admin.convert.admin.role.SysRoleConverter;
import com.auth.service.system.admin.mapper.admin.role.GrantTableMapper;
import com.auth.service.system.admin.mapper.admin.role.SysRoleMapper;
import com.auth.service.system.admin.model.entity.SysRoleEntity;
import com.auth.service.system.admin.model.form.role.SysRoleForm;
import com.auth.service.system.admin.model.po.reference.RoleReferencePO;
import com.auth.service.system.admin.model.query.role.SysRoleQuery;
import com.auth.service.system.admin.model.vo.authorization.RoleAuthorizationSummaryVO;
import com.auth.service.system.admin.model.vo.role.SysRoleDetailVO;
import com.auth.service.system.admin.model.vo.role.SysRoleOptionVO;
import com.auth.service.system.admin.model.vo.role.SysRolePageVO;
import com.auth.service.system.admin.service.admin.SysRoleService;
import com.auth.service.system.admin.service.authorization.query.RoleAuthorizationSurfaceService;
import com.auth.service.system.admin.support.sqlbuild.SysRolePageOrderSqlBuilder;
import com.auth.service.system.authorization.dispatch.trigger.RoleAuthorizationInvalidationTrigger;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.common.exception.code.SystemCommonResultCode;
import com.auth.service.system.common.service.AuditUserDisplayService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * 系统角色服务实现
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Slf4j
@Service
public class SysRoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRoleEntity> implements SysRoleService {

	private final AuditUserDisplayService auditUserDisplayService;

	private final RoleAuthorizationInvalidationTrigger roleInvalidationTrigger;

	private final GrantTableMapper grantTableMapper;

	private final RoleAuthorizationSurfaceService roleAuthorizationSurfaceService;

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(readOnly = true)
	public PageResponse<SysRolePageVO> getPage(SysRoleQuery query) {
		Page<SysRoleEntity> pageParams = new Page<>(query.getPageIndex(), query.getPageSize());
		String orderBySql = SysRolePageOrderSqlBuilder.buildOrderBySql(query.getSort());
		IPage<SysRoleEntity> entityPage = baseMapper.selectListByPage(pageParams, query, orderBySql);
		IPage<SysRolePageVO> page = entityPage.convert(SysRoleConverter.INSTANCE::toPageVo);

		auditUserDisplayService.enrichAuditUsernames(page, null, null);
		return PageResponse.of(page);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(readOnly = true)
	public SysRoleDetailVO getDetail(Long id) {
		SysRoleEntity entity = baseMapper.selectById(id);
		if (entity == null) {
			log.warn("role not found: id={}", id);
			throw new SystemBusinessException(SystemCommonResultCode.DATA_NOT_EXIST);
		}

		SysRoleDetailVO detail = SysRoleConverter.INSTANCE.toDetailVo(entity);
		RoleAuthorizationSummaryVO summary = roleAuthorizationSurfaceService.getAuthorizationSummary(id);
		detail.setPermissionCount(summary.getPermissionCount());
		detail.setMenuCount(summary.getMenuCount());
		detail.setGrantUserCount(summary.getGrantUserCount());

		auditUserDisplayService.enrichAuditUsernames(Collections.singletonList(detail), null, null);
		return detail;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(readOnly = true)
	public List<SysRoleOptionVO> listOptions(String roleName, String roleCode) {
		List<SysRoleEntity> entities = baseMapper.selectRoleOptions(roleName, roleCode);
		return SysRoleConverter.INSTANCE.toOptionVoList(entities);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void createBatchFromImport(List<SysRoleForm> forms) {
		if (CollUtil.isEmpty(forms)) {
			return;
		}

		List<String> roleCodes = forms.stream().map(SysRoleForm::getRoleCode).toList();
		List<String> distinctCodes = BusinessKeyAssert.requireDistinct(roleCodes,
				code -> new SystemBusinessException(SystemCommonResultCode.DATA_CODE_DUPLICATE, code));

		List<RoleReferencePO> existing = baseMapper.selectReferenceByRoleCodes(distinctCodes);
		if (CollUtil.isNotEmpty(existing)) {
			throw new SystemBusinessException(SystemCommonResultCode.DATA_CODE_DUPLICATE,
					existing.get(0).getRoleCode());
		}

		List<SysRoleEntity> entities = forms.stream().map(SysRoleConverter.INSTANCE::toEntity).toList();
		super.saveBatch(entities, BatchSizes.SIZE_500);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void update(SysRoleForm form) {
		Long id = form.getId();
		SysRoleEntity existing = baseMapper.selectById(id);
		if (existing == null) {
			log.warn("update role not found: id={}", id);
			throw new SystemBusinessException(SystemCommonResultCode.DATA_NOT_EXIST);
		}

		boolean statusChanged = FieldChangeSupport.valueChanged(existing.getStatus(), form.getStatus());
		String oldRoleCode = existing.getRoleCode();
		String newRoleCode = form.getRoleCode();
		boolean roleCodeChanged = FieldChangeSupport.codeChanged(oldRoleCode, newRoleCode);
		if (roleCodeChanged) {
			BusinessKeyAssert.requireAbsent(baseMapper,
					Wrappers.<SysRoleEntity>lambdaQuery()
						.eq(SysRoleEntity::getRoleCode, newRoleCode)
						.ne(SysRoleEntity::getId, existing.getId()),
					() -> new SystemBusinessException(SystemCommonResultCode.DATA_CODE_DUPLICATE, newRoleCode));
		}

		SysRoleConverter.INSTANCE.applyUpdateForm(form, existing);
		super.updateById(existing);

		if (FieldChangeSupport.anyChanged(roleCodeChanged, statusChanged)) {
			List<String> roleCodes = FieldChangeSupport.renameCodes(oldRoleCode, newRoleCode);
			roleInvalidationTrigger.submitByRoleCodes(roleCodes, "update");
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void batchUpdateStatus(IdsEnableStatusForm form) {
		List<Long> ids = form.getIds().stream().filter(Objects::nonNull).distinct().toList();
		if (CollUtil.isEmpty(ids)) {
			return;
		}

		// 失效按角色编码，先查出存在行（不存在的 id 忽略）
		List<SysRoleEntity> existing = listByIds(ids);
		if (CollUtil.isEmpty(existing)) {
			return;
		}

		Boolean status = form.getStatus();
		List<SysRoleEntity> updates = existing.stream().map(row -> {
			SysRoleEntity entity = new SysRoleEntity();
			entity.setId(row.getId());
			entity.setStatus(status);
			return entity;
		}).toList();
		super.updateBatchById(updates);

		List<String> roleCodes = existing.stream().map(SysRoleEntity::getRoleCode).toList();
		roleInvalidationTrigger.submitByRoleCodes(roleCodes, "update");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void deleteById(Long id) {
		SysRoleEntity existing = baseMapper.selectById(id);
		if (existing == null) {
			log.warn("delete role not found: id={}", id);
			throw new SystemBusinessException(SystemCommonResultCode.DATA_NOT_EXIST);
		}

		String roleCode = existing.getRoleCode();
		if (grantTableMapper.countByRoleId(existing.getId()) > 0) {
			throw new SystemBusinessException(SystemCommonResultCode.DATA_IN_USE, roleCode);
		}

		super.removeById(id);

		roleInvalidationTrigger.submitByRoleCodes(List.of(roleCode), "delete");
	}

}
