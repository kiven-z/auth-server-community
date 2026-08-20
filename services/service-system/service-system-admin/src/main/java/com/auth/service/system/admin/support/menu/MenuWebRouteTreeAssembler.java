package com.auth.service.system.admin.support.menu;

import cn.hutool.core.collection.CollUtil;
import com.auth.common.core.utils.TreeNodeProcessorUtil;
import com.auth.module.security.contract.constants.PermissionConstant;
import com.auth.service.system.admin.convert.admin.menu.MenuConvertContext;
import com.auth.service.system.admin.convert.admin.menu.MenuRouteMetaConverter;
import com.auth.service.system.admin.convert.admin.menu.MenuRouteTreeNode;
import com.auth.service.system.admin.mapper.admin.menu.SysMenuRoleMapper;
import com.auth.service.system.admin.model.entity.SysMenuEntity;
import com.auth.service.system.admin.model.po.menu.SysMenuRoleLinkRowPO;
import com.auth.service.system.admin.model.vo.menu.RouteMetaVO;
import com.auth.service.system.admin.model.vo.menu.RouteNodeVO;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 动态菜单路由树组装（用户可见裁剪、祖先补全、展示角色冒泡、排序、VO 映射）
 *
 * @author Bunny
 */
@Component
public class MenuWebRouteTreeAssembler {

	private final SysMenuRoleMapper sysMenuRoleMapper;

	public MenuWebRouteTreeAssembler(SysMenuRoleMapper sysMenuRoleMapper) {
		this.sysMenuRoleMapper = sysMenuRoleMapper;
	}

	/**
	 * 构建管理员可见的动态路由（全量启用菜单）
	 * @param menuList 启用菜单列表
	 * @return 路由树根列表
	 */
	public List<RouteNodeVO> buildAdminRoutes(List<SysMenuEntity> menuList) {
		if (CollUtil.isEmpty(menuList)) {
			return Collections.emptyList();
		}

		Map<Long, List<String>> roleMap = menuList.stream()
			.collect(Collectors.toMap(SysMenuEntity::getId, menu -> PermissionConstant.ADMIN_ROLES, (a, b) -> a));
		MenuConvertContext context = MenuConvertContext.builder().roleMap(roleMap).build();
		return buildRouteTree(menuList, context);
	}

	/**
	 * 构建当前用户可见的动态路由（直接可见 ∪ 祖先壳）
	 * @param menuList 启用菜单列表
	 * @param userRoleCodes 用户生效角色编码
	 * @return 路由树根列表
	 */
	public List<RouteNodeVO> buildRoutes(List<SysMenuEntity> menuList, Collection<String> userRoleCodes) {
		if (CollUtil.isEmpty(menuList)) {
			return Collections.emptyList();
		}

		List<Long> menuIds = menuList.stream().map(SysMenuEntity::getId).toList();
		Map<Long, List<String>> boundRoleMap = queryRoleMap(menuIds);
		MenuRouteVisibility visibility = MenuRouteVisibilityResolver.resolve(menuList, boundRoleMap, userRoleCodes);
		if (CollUtil.isEmpty(visibility.keepIds())) {
			return Collections.emptyList();
		}

		MenuRouteDisplayMeta displayMeta = MenuRouteDisplayRoleResolver.resolve(menuList, visibility, boundRoleMap,
				userRoleCodes);
		List<SysMenuEntity> visibleMenus = menuList.stream()
			.filter(menu -> visibility.keepIds().contains(menu.getId()))
			.toList();
		MenuConvertContext context = MenuConvertContext.builder()
			.roleMap(displayMeta.roleMap())
			.forcePublicAccessIds(displayMeta.forcePublicAccessIds())
			.build();
		return buildRouteTree(visibleMenus, context);
	}

	/**
	 * 建树并转为 VO
	 * @param menuList 待建树菜单
	 * @param context 转换上下文
	 * @return 路由树根列表
	 */
	private List<RouteNodeVO> buildRouteTree(List<SysMenuEntity> menuList, MenuConvertContext context) {
		List<MenuRouteTreeNode> nodeList = menuList.stream()
			.map(MenuRouteTreeNode::fromEntity)
			.sorted(Comparator.comparing(MenuRouteTreeNode::getMenuRank,
					Comparator.nullsLast(Comparator.naturalOrder())))
			.toList();
		List<MenuRouteTreeNode> treeNodes = TreeNodeProcessorUtil.process(nodeList);
		return convertTreeToVO(treeNodes, context);
	}

	/**
	 * 批量查询菜单角色关联
	 * @param menuIds 菜单 ID 集合
	 * @return menuId -> roleCode 列表
	 */
	private Map<Long, List<String>> queryRoleMap(List<Long> menuIds) {
		if (CollUtil.isEmpty(menuIds)) {
			return Collections.emptyMap();
		}

		List<SysMenuRoleLinkRowPO> menuRoles = sysMenuRoleMapper.selectActiveRoleLinksByMenuIds(menuIds);
		return menuRoles.stream()
			.collect(Collectors.groupingBy(SysMenuRoleLinkRowPO::getMenuId,
					Collectors.mapping(SysMenuRoleLinkRowPO::getRoleCode, Collectors.toList())));
	}

	/**
	 * 递归将树节点列表转为 VO 列表
	 * @param nodes 树节点列表
	 * @param context 转换上下文
	 * @return 路由 VO 列表
	 */
	private List<RouteNodeVO> convertTreeToVO(List<MenuRouteTreeNode> nodes, MenuConvertContext context) {
		return nodes.stream().map(node -> {
			SysMenuEntity menu = node.getMenu();
			RouteMetaVO routeMetaVO = MenuRouteMetaConverter.fromMenuEntity(menu, context);
			RouteNodeVO vo = new RouteNodeVO();
			vo.setPath(menu.getPath());
			vo.setName(menu.getName());
			vo.setRedirect(menu.getRedirect());
			vo.setComponent(menu.getComponent());
			vo.setMeta(routeMetaVO);
			vo.setChildren(Collections.emptyList());

			if (CollUtil.isNotEmpty(node.getChildren())) {
				List<RouteNodeVO> children = convertTreeToVO(node.getChildren(), context);
				vo.setChildren(children);
			}
			return vo;
		}).toList();
	}

}
