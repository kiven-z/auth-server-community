package com.auth.service.system.admin.service.admin.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.auth.common.core.constants.BatchSizes;
import com.auth.common.core.model.form.IdsEnableStatusForm;
import com.auth.common.core.utils.FieldChangeSupport;
import com.auth.common.data.model.PageResponse;
import com.auth.common.data.support.BusinessKeyAssert;
import com.auth.service.system.admin.convert.admin.SysPermissionConverter;
import com.auth.service.system.admin.mapper.admin.permission.SysPermissionMapper;
import com.auth.service.system.admin.mapper.admin.role.SysRolePermissionMapper;
import com.auth.service.system.admin.mapper.authorization.RolePermissionBindingQueryMapper;
import com.auth.service.system.admin.model.entity.SysPermissionEntity;
import com.auth.service.system.admin.model.form.permission.SysPermissionForm;
import com.auth.service.system.admin.model.po.reference.PermissionReferencePO;
import com.auth.service.system.admin.model.query.permission.SysPermissionQuery;
import com.auth.service.system.admin.model.vo.permission.SysPermissionDetailVO;
import com.auth.service.system.admin.model.vo.permission.SysPermissionPageVO;
import com.auth.service.system.admin.service.admin.SysPermissionService;
import com.auth.service.system.admin.support.sqlbuild.SysPermissionPageOrderSqlBuilder;
import com.auth.service.system.authorization.dispatch.trigger.PermissionAuthorizationInvalidationTrigger;
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
 * 系统权限服务实现
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Slf4j
@Service
public class SysPermissionServiceImpl extends ServiceImpl<SysPermissionMapper, SysPermissionEntity>
		implements SysPermissionService {

	private final AuditUserDisplayService auditUserDisplayService;

	private final PermissionAuthorizationInvalidationTrigger permissionInvalidationTrigger;

	private final RoleAuthorizationInvalidationTrigger roleInvalidationTrigger;

	private final SysRolePermissionMapper sysRolePermissionMapper;

	private final RolePermissionBindingQueryMapper rolePermissionBindingQueryMapper;

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(readOnly = true)
	public PageResponse<SysPermissionPageVO> getPage(SysPermissionQuery query) {
		Page<SysPermissionEntity> pageParams = new Page<>(query.getPageIndex(), query.getPageSize());
		String orderBySql = SysPermissionPageOrderSqlBuilder.buildOrderBySql(query.getSort());
		IPage<SysPermissionEntity> entityPage = baseMapper.selectListByPage(pageParams, query, orderBySql);
		IPage<SysPermissionPageVO> page = entityPage.convert(SysPermissionConverter.INSTANCE::toPageVo);

		auditUserDisplayService.enrichAuditUsernames(page, null, null);
		return PageResponse.of(page);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(readOnly = true)
	public SysPermissionDetailVO getDetail(Long id) {
		SysPermissionEntity entity = baseMapper.selectById(id);
		if (entity == null) {
			log.warn("permission not found: id={}", id);
			throw new SystemBusinessException(SystemCommonResultCode.DATA_NOT_EXIST);
		}

		SysPermissionDetailVO detail = SysPermissionConverter.INSTANCE.toDetailVo(entity);
		detail.setBoundRoleCount(rolePermissionBindingQueryMapper.countRolesByPermissionId(id, null));

		auditUserDisplayService.enrichAuditUsernames(Collections.singletonList(detail), null, null);
		return detail;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void createBatchFromImport(List<SysPermissionForm> forms) {
		if (CollUtil.isEmpty(forms)) {
			return;
		}

		List<String> permissionCodes = forms.stream().map(SysPermissionForm::getPermissionCode).toList();
		List<String> distinctCodes = BusinessKeyAssert.requireDistinct(permissionCodes,
				code -> new SystemBusinessException(SystemCommonResultCode.DATA_CODE_DUPLICATE, code));
		List<PermissionReferencePO> existing = baseMapper.selectReferenceByPermissionCodes(distinctCodes);
		if (CollUtil.isNotEmpty(existing)) {
			throw new SystemBusinessException(SystemCommonResultCode.DATA_CODE_DUPLICATE,
					existing.get(0).getPermissionCode());
		}

		List<SysPermissionEntity> entities = SysPermissionConverter.INSTANCE.toEntityList(forms);
		super.saveBatch(entities, BatchSizes.SIZE_500);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void update(SysPermissionForm form) {
		Long id = form.getId();
		SysPermissionEntity existing = baseMapper.selectById(id);
		if (existing == null) {
			log.warn("update permission not found: id={}", id);
			throw new SystemBusinessException(SystemCommonResultCode.DATA_NOT_EXIST);
		}

		boolean statusChanged = FieldChangeSupport.valueChanged(existing.getStatus(), form.getStatus());
		String oldPermissionCode = existing.getPermissionCode();
		String newPermissionCode = form.getPermissionCode();
		boolean permissionCodeChanged = FieldChangeSupport.codeChanged(oldPermissionCode, newPermissionCode);
		if (permissionCodeChanged) {
			BusinessKeyAssert.requireAbsent(baseMapper,
					Wrappers.<SysPermissionEntity>lambdaQuery()
						.eq(SysPermissionEntity::getPermissionCode, newPermissionCode)
						.ne(SysPermissionEntity::getId, existing.getId()),
					() -> new SystemBusinessException(SystemCommonResultCode.DATA_CODE_DUPLICATE, newPermissionCode));
		}

		SysPermissionConverter.INSTANCE.applyUpdateForm(form, existing);
		existing.setPermissionCode(newPermissionCode);
		super.updateById(existing);

		if (FieldChangeSupport.anyChanged(permissionCodeChanged, statusChanged)) {
			List<String> permissionCodes = FieldChangeSupport.renameCodes(oldPermissionCode, newPermissionCode);
			permissionInvalidationTrigger.submitByPermissionCodes(permissionCodes, "update");
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

		// 失效按权限编码，先查出存在行（不存在的 id 忽略）
		List<SysPermissionEntity> existing = listByIds(ids);
		if (CollUtil.isEmpty(existing)) {
			return;
		}

		Boolean status = form.getStatus();
		List<SysPermissionEntity> updates = existing.stream().map(row -> {
			SysPermissionEntity entity = new SysPermissionEntity();
			entity.setId(row.getId());
			entity.setStatus(status);
			return entity;
		}).toList();
		super.updateBatchById(updates);

		List<String> permissionCodes = existing.stream().map(SysPermissionEntity::getPermissionCode).toList();
		permissionInvalidationTrigger.submitByPermissionCodes(permissionCodes, "update");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void deleteById(Long id) {
		SysPermissionEntity existing = baseMapper.selectById(id);
		if (existing == null) {
			log.warn("delete permission not found: id={}", id);
			throw new SystemBusinessException(SystemCommonResultCode.DATA_NOT_EXIST);
		}

		String permissionCode = existing.getPermissionCode();
		permissionInvalidationTrigger.submitByPermissionCodes(List.of(permissionCode), "delete");

		List<String> boundRoleCodes = CollUtil.emptyIfNull(sysRolePermissionMapper.selectRoleCodesByPermissionId(id))
			.stream()
			.filter(CharSequenceUtil::isNotBlank)
			.distinct()
			.toList();
		if (CollUtil.isNotEmpty(boundRoleCodes)) {
			roleInvalidationTrigger.submitByRoleCodes(boundRoleCodes, "delete-permission");
		}

		super.removeById(id);
	}

}
