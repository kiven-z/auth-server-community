package com.auth.service.system.admin.service.admin.impl;

import cn.hutool.core.collection.CollUtil;
import com.auth.common.core.model.form.IdsEnableStatusForm;
import com.auth.common.core.utils.TreeParentIdUtil;
import com.auth.common.data.model.PageResponse;
import com.auth.module.security.contract.constants.PermissionConstant;
import com.auth.service.system.admin.convert.admin.ReferenceConverter;
import com.auth.service.system.admin.convert.admin.menu.SysMenuConverter;
import com.auth.service.system.admin.mapper.admin.menu.SysMenuMapper;
import com.auth.service.system.admin.mapper.admin.menu.SysMenuRoleMapper;
import com.auth.service.system.admin.mapper.authorization.MenuRoleBindingQueryMapper;
import com.auth.service.system.admin.model.entity.SysMenuEntity;
import com.auth.service.system.admin.model.form.menu.SysMenuAssignRoleForm;
import com.auth.service.system.admin.model.form.menu.SysMenuMoveForm;
import com.auth.service.system.admin.model.form.menu.SysMenuSaveForm;
import com.auth.service.system.admin.model.po.reference.RoleReferencePO;
import com.auth.service.system.admin.model.query.menu.SysMenuQuery;
import com.auth.service.system.admin.model.vo.menu.RouteNodeVO;
import com.auth.service.system.admin.model.vo.menu.SysMenuDetailVO;
import com.auth.service.system.admin.model.vo.menu.SysMenuListVO;
import com.auth.service.system.admin.model.vo.reference.ext.MenuAssignedRoleReferenceVO;
import com.auth.service.system.admin.service.admin.SysMenuService;
import com.auth.service.system.admin.support.grant.RbacReferenceChecker;
import com.auth.service.system.admin.support.menu.MenuReferenceChecker;
import com.auth.service.system.admin.support.menu.MenuWebRouteTreeAssembler;
import com.auth.service.system.authorization.dispatch.query.UserEffectiveCodesResolver;
import com.auth.service.system.authorization.dispatch.query.UserEffectiveCodesSnapshot;
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
 * 系统菜单服务实现
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Slf4j
@Service
public class SysMenuServiceImpl extends ServiceImpl<SysMenuMapper, SysMenuEntity> implements SysMenuService {

	private final AuditUserDisplayService auditUserDisplayService;

	private final UserEffectiveCodesResolver userEffectiveCodesResolver;

	private final MenuWebRouteTreeAssembler menuWebRouteTreeAssembler;

	private final MenuReferenceChecker menuReferenceChecker;

	private final SysMenuRoleMapper sysMenuRoleMapper;

	private final MenuRoleBindingQueryMapper menuRoleBindingQueryMapper;

	private final RbacReferenceChecker rbacReferenceChecker;

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(readOnly = true)
	public List<RouteNodeVO> listWebRoutes(Long userId) {
		if (userId == null) {
			return Collections.emptyList();
		}

		UserEffectiveCodesSnapshot codes = userEffectiveCodesResolver.resolve(userId).orElse(null);
		if (codes == null) {
			log.warn("Menu web route: effective codes unavailable userId={}", userId);
			return Collections.emptyList();
		}

		List<SysMenuEntity> menuList = list(
				Wrappers.lambdaQuery(SysMenuEntity.class).eq(SysMenuEntity::getStatus, Boolean.TRUE));

		List<String> roleCodes = codes.roleCodes();
		boolean isAdmin = PermissionConstant.isAdmin(userId, roleCodes);
		return isAdmin ? menuWebRouteTreeAssembler.buildAdminRoutes(menuList)
				: menuWebRouteTreeAssembler.buildRoutes(menuList, roleCodes);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(readOnly = true)
	public List<SysMenuListVO> listFlat(SysMenuQuery query) {
		List<SysMenuEntity> entities = baseMapper.selectListByQuery(query);
		List<SysMenuListVO> list = SysMenuConverter.INSTANCE.toListVoList(entities);

		auditUserDisplayService.enrichAuditUsernames(list, null, null);
		return list;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(readOnly = true)
	public PageResponse<SysMenuListVO> pageFlat(SysMenuQuery query) {
		Page<SysMenuEntity> pageParams = new Page<>(query.getPageIndex(), query.getPageSize());
		IPage<SysMenuEntity> entityPage = baseMapper.selectPageByQuery(pageParams, query);
		IPage<SysMenuListVO> result = entityPage.convert(SysMenuConverter.INSTANCE::toListVo);

		auditUserDisplayService.enrichAuditUsernames(result, null, null);
		return PageResponse.of(result);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(readOnly = true)
	public SysMenuDetailVO getDetail(Long id) {
		SysMenuEntity entity = menuReferenceChecker.getExisting(id);
		SysMenuDetailVO detail = SysMenuConverter.INSTANCE.toDetailVo(entity);
		detail.setBoundRoleCount(menuRoleBindingQueryMapper.countRolesByMenuId(id, null));
		auditUserDisplayService.enrichAuditUsernames(Collections.singletonList(detail), null, null);
		return detail;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(readOnly = true)
	public List<MenuAssignedRoleReferenceVO> listAssignedRoles(Long menuId) {
		List<RoleReferencePO> dtoList = sysMenuRoleMapper.selectAssignedRolesByMenuId(menuId);
		return ReferenceConverter.INSTANCE.toMenuAssignedRoleList(dtoList);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public Long create(SysMenuSaveForm form) {
		SysMenuEntity entity = SysMenuConverter.INSTANCE.toMenuEntity(form);
		entity.setParentId(Objects.requireNonNullElse(entity.getParentId(), 0L));
		save(entity);

		return entity.getId();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void update(SysMenuSaveForm form) {
		SysMenuEntity entity = SysMenuConverter.INSTANCE.toMenuEntity(form);
		entity.setParentId(Objects.requireNonNullElse(entity.getParentId(), 0L));
		updateById(entity);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void move(Long menuId, SysMenuMoveForm form) {
		SysMenuEntity existing = menuReferenceChecker.getExisting(menuId);
		long newParentId = menuReferenceChecker.normalizeAndRequireParent(form.getParentId());
		Long rawParentId = existing.getParentId();

		if (TreeParentIdUtil.normalize(rawParentId) == newParentId) {
			return;
		}

		menuReferenceChecker.requireMoveTargetValid(menuId, newParentId);
		existing.setParentId(newParentId);
		updateById(existing);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void batchUpdateStatus(IdsEnableStatusForm form) {
		Boolean status = form.getStatus();
		List<SysMenuEntity> list = form.getIds().stream().filter(Objects::nonNull).map(id -> {
			SysMenuEntity sysMenuEntity = new SysMenuEntity();
			sysMenuEntity.setId(id);
			sysMenuEntity.setStatus(status);
			return sysMenuEntity;
		}).toList();
		super.updateBatchById(list);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void deleteByIds(List<Long> ids) {
		if (CollUtil.isEmpty(ids)) {
			return;
		}

		List<Long> idsToDelete = ids.stream().filter(Objects::nonNull).toList();
		if (CollUtil.isEmpty(idsToDelete)) {
			return;
		}

		Long blockedParentId = baseMapper.selectFirstBlockedParentId(idsToDelete);
		if (blockedParentId != null) {
			log.warn("Tree has active children: id={}", blockedParentId);
			throw new SystemBusinessException(SystemCommonResultCode.TREE_HAS_ACTIVE_CHILDREN);
		}

		removeByIds(idsToDelete);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void replaceMenuRoles(Long menuId, SysMenuAssignRoleForm form) {
		List<Long> roleIds = CollUtil.emptyIfNull(form.getRoleIds()).stream().distinct().toList();
		rbacReferenceChecker.requireExistingEnabledRoleIds(roleIds, SystemCommonResultCode.GRANT_REFERENCE_INVALID);
		sysMenuRoleMapper.deleteByMenuId(menuId);

		if (CollUtil.isNotEmpty(roleIds)) {
			sysMenuRoleMapper.batchInsertMenuRoles(menuId, roleIds);
		}
	}

}
