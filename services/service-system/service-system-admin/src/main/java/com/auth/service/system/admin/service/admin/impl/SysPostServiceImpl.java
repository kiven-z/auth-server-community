package com.auth.service.system.admin.service.admin.impl;

import cn.hutool.core.collection.CollUtil;
import com.auth.common.core.constants.BatchSizes;
import com.auth.common.core.model.form.IdsEnableStatusForm;
import com.auth.common.core.utils.FieldChangeSupport;
import com.auth.common.data.model.PageResponse;
import com.auth.service.system.admin.convert.admin.ReferenceConverter;
import com.auth.service.system.admin.convert.admin.SysPostConverter;
import com.auth.service.system.admin.exception.SystemAdminResultCode;
import com.auth.service.system.admin.mapper.admin.post.SysPostMapper;
import com.auth.service.system.admin.mapper.authorization.PostRelationQueryMapper;
import com.auth.service.system.admin.model.entity.SysPostEntity;
import com.auth.service.system.admin.model.form.post.SysPostForm;
import com.auth.service.system.admin.model.po.post.SysPostPageRowPO;
import com.auth.service.system.admin.model.po.post.SysPostSearchItemPO;
import com.auth.service.system.admin.model.po.reference.DeptReferencePO;
import com.auth.service.system.admin.model.query.post.SysPostQuery;
import com.auth.service.system.admin.model.vo.post.SysPostDetailVO;
import com.auth.service.system.admin.model.vo.post.SysPostPageVO;
import com.auth.service.system.admin.model.vo.post.SysPostSearchItemVO;
import com.auth.service.system.admin.service.admin.SysPostService;
import com.auth.service.system.admin.support.post.PostReferenceChecker;
import com.auth.service.system.admin.support.post.PostWriteGuard;
import com.auth.service.system.admin.support.sqlbuild.SysPostPageOrderSqlBuilder;
import com.auth.service.system.authorization.dispatch.trigger.PostAuthorizationInvalidationTrigger;
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
 * 系统岗位服务实现
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Slf4j
@Service
public class SysPostServiceImpl extends ServiceImpl<SysPostMapper, SysPostEntity> implements SysPostService {

	private final AuditUserDisplayService auditUserDisplayService;

	private final PostAuthorizationInvalidationTrigger postInvalidationTrigger;

	private final PostReferenceChecker postReferenceChecker;

	private final PostRelationQueryMapper postRelationQueryMapper;

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(readOnly = true)
	public PageResponse<SysPostPageVO> getPage(SysPostQuery query) {
		Page<SysPostEntity> pageParams = new Page<>(query.getPageIndex(), query.getPageSize());
		String orderBySql = SysPostPageOrderSqlBuilder.buildOrderBySql(query.getSort());
		IPage<SysPostPageRowPO> page = baseMapper.selectListByPage(pageParams, query, orderBySql);
		IPage<SysPostPageVO> voPage = page.convert(SysPostConverter.INSTANCE::toPageVo);

		auditUserDisplayService.enrichAuditUsernames(voPage, null, null);
		return PageResponse.of(voPage);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(readOnly = true)
	public List<SysPostSearchItemVO> search(String keyword, Boolean status, Integer limit) {
		int cappedLimit = limit == null ? 20 : Math.max(1, Math.min(limit, 50));

		List<SysPostSearchItemPO> poList = baseMapper.search(keyword, status, cappedLimit);
		return SysPostConverter.INSTANCE.toSearchItemVoList(poList);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(readOnly = true)
	public SysPostDetailVO getDetail(Long id) {
		SysPostEntity entity = postReferenceChecker.getExistingActive(id);

		SysPostDetailVO detail = SysPostConverter.INSTANCE.toDetailVo(entity);
		detail.setEffective(baseMapper.countEffectiveById(id) > 0);
		DeptReferencePO boundDept = baseMapper.selectBoundDeptByPostId(id);
		detail.setBoundDept(ReferenceConverter.INSTANCE.toDeptReference(boundDept));
		detail.setBoundUserCount(postRelationQueryMapper.countUsersByPostId(id, null));

		auditUserDisplayService.enrichAuditUsernames(Collections.singletonList(detail), null, null);
		return detail;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void createBatchFromImport(List<SysPostForm> forms) {
		PostWriteGuard.requireBatchCreatable(baseMapper, forms);
		if (CollUtil.isEmpty(forms)) {
			return;
		}
		List<SysPostEntity> entities = forms.stream().map(SysPostConverter.INSTANCE::toEntity).toList();
		super.saveBatch(entities, BatchSizes.SIZE_500);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void update(SysPostForm form) {
		Long id = form.getId();
		SysPostEntity existing = postReferenceChecker.getExistingActive(id);

		Long deptId = form.getDeptId();
		if (!Objects.equals(existing.getDeptId(), deptId)) {
			List<Long> assignableDeptIds = baseMapper.selectAssignableDeptIds(List.of(deptId));
			if (assignableDeptIds.isEmpty()) {
				log.warn("department unavailable: id={}", deptId);
				throw new SystemBusinessException(SystemCommonResultCode.DATA_UNAVAILABLE);
			}
		}

		String postCode = form.getPostCode();
		long otherCount = baseMapper.selectCount(Wrappers.<SysPostEntity>lambdaQuery()
			.eq(SysPostEntity::getDeptId, deptId)
			.eq(SysPostEntity::getPostCode, postCode)
			.ne(SysPostEntity::getId, id));
		if (otherCount > 0) {
			log.warn("Post code duplicate in department: deptId={}, postCode={}", deptId, postCode);
			throw new SystemBusinessException(SystemAdminResultCode.POST_CODE_DUPLICATE_IN_DEPT, postCode);
		}

		boolean statusChanged = FieldChangeSupport.valueChanged(existing.getStatus(), form.getStatus());
		SysPostConverter.INSTANCE.applyUpdateForm(form, existing);
		super.updateById(existing);

		if (statusChanged) {
			postInvalidationTrigger.submitByPostIds(List.of(id), "update");
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

		Boolean status = form.getStatus();
		List<SysPostEntity> updates = ids.stream().map(id -> {
			SysPostEntity entity = new SysPostEntity();
			entity.setId(id);
			entity.setStatus(status);
			return entity;
		}).toList();
		super.updateBatchById(updates);
		postInvalidationTrigger.submitByPostIds(ids, "update");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void deleteById(Long id) {
		postReferenceChecker.getExistingActive(id);

		long countUserPostByPostId = baseMapper.countUserPostByPostId(id);
		if (countUserPostByPostId > 0) {
			log.warn("post in use: id={}", id);
			throw new SystemBusinessException(SystemCommonResultCode.DATA_IN_USE);
		}

		postInvalidationTrigger.submitByPostIds(List.of(id), "delete");
		super.removeById(id);
	}

}
