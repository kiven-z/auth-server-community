package com.auth.service.system.admin.support.menu;

import cn.hutool.core.collection.CollUtil;
import com.auth.service.system.admin.model.entity.SysMenuEntity;
import lombok.experimental.UtilityClass;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 解析用户可见菜单 ID（直接可见 + 祖先闭包）
 *
 * @author Bunny
 */
@UtilityClass
public class MenuRouteVisibilityResolver {

	private static final long ROOT_PARENT_ID = 0L;

	/**
	 * 计算直接可见集与出树保留集
	 * @param menus 启用菜单
	 * @param boundRoleMap 菜单绑定角色（库表）
	 * @param userRoleCodes 用户生效角色编码
	 * @return 可见集；menus 为空时两组均为空
	 */
	public static MenuRouteVisibility resolve(List<SysMenuEntity> menus, Map<Long, List<String>> boundRoleMap,
			Collection<String> userRoleCodes) {
		if (CollUtil.isEmpty(menus)) {
			return new MenuRouteVisibility(Set.of(), Set.of());
		}

		Set<String> userRoles = normalizeUserRoles(userRoleCodes);
		Map<Long, List<String>> rolesByMenu = boundRoleMap == null ? Map.of() : boundRoleMap;
		Map<Long, Long> parentById = indexParents(menus);

		Set<Long> directVisible = collectDirectVisible(menus, rolesByMenu, userRoles);
		Set<Long> keep = expandWithAncestors(directVisible, parentById);
		return new MenuRouteVisibility(Collections.unmodifiableSet(directVisible), Collections.unmodifiableSet(keep));
	}

	private static Set<String> normalizeUserRoles(Collection<String> userRoleCodes) {
		if (CollUtil.isEmpty(userRoleCodes)) {
			return Set.of();
		}
		return userRoleCodes.stream().filter(Objects::nonNull).collect(Collectors.toUnmodifiableSet());
	}

	private static Map<Long, Long> indexParents(List<SysMenuEntity> menus) {
		return menus.stream()
			.collect(Collectors.toMap(SysMenuEntity::getId,
					m -> Objects.requireNonNullElse(m.getParentId(), ROOT_PARENT_ID), (a, b) -> a));
	}

	private static Set<Long> collectDirectVisible(List<SysMenuEntity> menus, Map<Long, List<String>> rolesByMenu,
			Set<String> userRoles) {
		Set<Long> directVisible = new HashSet<>();
		for (SysMenuEntity menu : menus) {
			if (isDirectlyVisible(menu, rolesByMenu, userRoles)) {
				directVisible.add(menu.getId());
			}
		}
		return directVisible;
	}

	private static Set<Long> expandWithAncestors(Set<Long> directVisible, Map<Long, Long> parentById) {
		Set<Long> keep = new HashSet<>(directVisible);
		Set<Long> existingIds = parentById.keySet();
		for (Long menuId : directVisible) {
			addAncestors(menuId, parentById, existingIds, keep);
		}
		return keep;
	}

	/**
	 * 沿父链向上收录祖先；遇到已收录节点即停止（其祖先此前已走过）。
	 */
	private static void addAncestors(Long menuId, Map<Long, Long> parentById, Set<Long> existingIds, Set<Long> keep) {
		Long cursor = parentById.get(menuId);
		while (cursor != null && cursor != ROOT_PARENT_ID && existingIds.contains(cursor)) {
			if (!keep.add(cursor)) {
				return;
			}
			cursor = parentById.get(cursor);
		}
	}

	private static boolean isDirectlyVisible(SysMenuEntity menu, Map<Long, List<String>> rolesByMenu,
			Set<String> userRoles) {
		if (Boolean.TRUE.equals(menu.getPublicAccess())) {
			return true;
		}
		List<String> bound = rolesByMenu.getOrDefault(menu.getId(), List.of());
		if (bound.isEmpty() || userRoles.isEmpty()) {
			return false;
		}
		return bound.stream().anyMatch(userRoles::contains);
	}

}
