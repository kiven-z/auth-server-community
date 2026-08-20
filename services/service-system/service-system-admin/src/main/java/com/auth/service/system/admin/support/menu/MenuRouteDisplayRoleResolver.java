package com.auth.service.system.admin.support.menu;

import cn.hutool.core.collection.CollUtil;
import com.auth.service.system.admin.model.entity.SysMenuEntity;
import lombok.experimental.UtilityClass;

import java.util.*;
import java.util.stream.Collectors;

import static com.auth.common.core.utils.TreeParentIdUtil.ROOT_PARENT_ID;

/**
 * 为出树节点计算展示用 roles / publicAccess（祖先壳冒泡，不写库）
 *
 * @author Bunny
 */
@UtilityClass
public class MenuRouteDisplayRoleResolver {

	/**
	 * 计算展示 meta
	 * @param menus 启用菜单（含未保留节点亦可，按 visibility 过滤）
	 * @param visibility 可见集
	 * @param boundRoleMap 菜单绑定角色（库表）
	 * @param userRoleCodes 用户生效角色编码
	 * @return 展示用 roleMap 与需强制 publicAccess 的祖先壳
	 */
	public static MenuRouteDisplayMeta resolve(List<SysMenuEntity> menus, MenuRouteVisibility visibility,
			Map<Long, List<String>> boundRoleMap, Collection<String> userRoleCodes) {
		if (visibility == null || CollUtil.isEmpty(visibility.keepIds())) {
			return new MenuRouteDisplayMeta(Map.of(), Set.of());
		}

		Set<String> userRoles = normalizeUserRoles(userRoleCodes);
		Map<Long, List<String>> rolesByMenu = boundRoleMap == null ? Map.of() : boundRoleMap;
		Map<Long, SysMenuEntity> menuById = indexMenus(menus);
		Map<Long, Long> parentById = indexParents(menuById);

		BubbleState bubble = bubbleFromDirectVisible(visibility, menuById, parentById, rolesByMenu, userRoles);
		return assembleDisplayMeta(visibility, rolesByMenu, bubble);
	}

	private static Set<String> normalizeUserRoles(Collection<String> userRoleCodes) {
		if (CollUtil.isEmpty(userRoleCodes)) {
			return Set.of();
		}
		return userRoleCodes.stream().filter(Objects::nonNull).collect(Collectors.toUnmodifiableSet());
	}

	private static Map<Long, SysMenuEntity> indexMenus(List<SysMenuEntity> menus) {
		if (CollUtil.isEmpty(menus)) {
			return Map.of();
		}
		return menus.stream().collect(Collectors.toMap(SysMenuEntity::getId, m -> m, (a, b) -> a));
	}

	private static Map<Long, Long> indexParents(Map<Long, SysMenuEntity> menuById) {
		return menuById.values()
			.stream()
			.collect(Collectors.toMap(SysMenuEntity::getId,
					m -> Objects.requireNonNullElse(m.getParentId(), ROOT_PARENT_ID), (a, b) -> a));
	}

	private static BubbleState bubbleFromDirectVisible(MenuRouteVisibility visibility,
			Map<Long, SysMenuEntity> menuById, Map<Long, Long> parentById, Map<Long, List<String>> rolesByMenu,
			Set<String> userRoles) {
		Map<Long, LinkedHashSet<String>> bubbledRoles = new HashMap<>();
		Set<Long> publicDescendantShells = new HashSet<>();

		for (Long directId : visibility.directVisibleIds()) {
			SysMenuEntity direct = menuById.get(directId);
			boolean publicLeaf = direct != null && Boolean.TRUE.equals(direct.getPublicAccess());
			List<String> hitRoles = intersectRoles(rolesByMenu.getOrDefault(directId, List.of()), userRoles);
			bubbleOntoAncestors(directId, visibility, parentById, hitRoles, publicLeaf, bubbledRoles,
					publicDescendantShells);
		}
		return new BubbleState(bubbledRoles, publicDescendantShells);
	}

	private static void bubbleOntoAncestors(Long directId, MenuRouteVisibility visibility, Map<Long, Long> parentById,
			List<String> hitRoles, boolean publicLeaf, Map<Long, LinkedHashSet<String>> bubbledRoles,
			Set<Long> publicDescendantShells) {
		Long cursor = parentById.get(directId);
		while (cursor != null && cursor != ROOT_PARENT_ID && visibility.keepIds().contains(cursor)) {
			if (!visibility.directVisibleIds().contains(cursor)) {
				applyBubble(cursor, hitRoles, publicLeaf, bubbledRoles, publicDescendantShells);
			}
			cursor = parentById.get(cursor);
		}
	}

	private static void applyBubble(Long ancestorId, List<String> hitRoles, boolean publicLeaf,
			Map<Long, LinkedHashSet<String>> bubbledRoles, Set<Long> publicDescendantShells) {
		if (!hitRoles.isEmpty()) {
			bubbledRoles.computeIfAbsent(ancestorId, id -> new LinkedHashSet<>()).addAll(hitRoles);
		}
		if (publicLeaf) {
			publicDescendantShells.add(ancestorId);
		}
	}

	private static MenuRouteDisplayMeta assembleDisplayMeta(MenuRouteVisibility visibility,
			Map<Long, List<String>> rolesByMenu, BubbleState bubble) {
		Map<Long, List<String>> displayRoles = new HashMap<>();
		Set<Long> forcePublicAccessIds = new HashSet<>();

		for (Long menuId : visibility.keepIds()) {
			if (visibility.directVisibleIds().contains(menuId)) {
				displayRoles.put(menuId, List.copyOf(rolesByMenu.getOrDefault(menuId, List.of())));
				continue;
			}
			LinkedHashSet<String> bubbled = bubble.bubbledRoles().getOrDefault(menuId, new LinkedHashSet<>());
			displayRoles.put(menuId, List.copyOf(bubbled));
			if (bubbled.isEmpty() && bubble.publicDescendantShells().contains(menuId)) {
				forcePublicAccessIds.add(menuId);
			}
		}

		return new MenuRouteDisplayMeta(Collections.unmodifiableMap(displayRoles),
				Collections.unmodifiableSet(forcePublicAccessIds));
	}

	private static List<String> intersectRoles(List<String> bound, Set<String> userRoles) {
		if (CollUtil.isEmpty(bound) || userRoles.isEmpty()) {
			return List.of();
		}
		List<String> hit = new ArrayList<>();
		for (String role : bound) {
			if (userRoles.contains(role)) {
				hit.add(role);
			}
		}
		return hit;
	}

	private record BubbleState(Map<Long, LinkedHashSet<String>> bubbledRoles, Set<Long> publicDescendantShells) {
	}

}
