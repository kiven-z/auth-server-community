package com.auth.service.system.admin.support.menu;

import com.auth.service.system.admin.model.entity.SysMenuEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link MenuRouteVisibilityResolver} 单元测试
 */
@DisplayName("MenuRouteVisibilityResolver 可见集")
class MenuRouteVisibilityResolverTest {

	private static SysMenuEntity menu(Long id, Long parentId) {
		SysMenuEntity entity = new SysMenuEntity();
		entity.setId(id);
		entity.setParentId(parentId);
		entity.setPublicAccess(Boolean.FALSE);
		return entity;
	}

	private static SysMenuEntity publicMenu(Long id, Long parentId) {
		SysMenuEntity entity = menu(id, parentId);
		entity.setPublicAccess(Boolean.TRUE);
		return entity;
	}

	@Test
	@DisplayName("仅叶子绑定角色：保留叶子与全部祖先")
	void resolveKeepsAncestorsWhenLeafBound() {
		List<SysMenuEntity> menus = List.of(menu(1L, 0L), menu(2L, 1L), menu(3L, 2L));
		Map<Long, List<String>> bound = Map.of(3L, List.of("ROLE_X"));

		MenuRouteVisibility visibility = MenuRouteVisibilityResolver.resolve(menus, bound, List.of("ROLE_X"));

		assertThat(visibility.directVisibleIds()).containsExactly(3L);
		assertThat(visibility.keepIds()).containsExactlyInAnyOrder(1L, 2L, 3L);
	}

	@Test
	@DisplayName("父未绑定且无可见子孙：父不在保留集")
	void resolveExcludesUnrelatedParent() {
		List<SysMenuEntity> menus = List.of(menu(1L, 0L), menu(2L, 1L), menu(9L, 0L));
		Map<Long, List<String>> bound = Map.of(2L, List.of("ROLE_X"));

		MenuRouteVisibility visibility = MenuRouteVisibilityResolver.resolve(menus, bound, List.of("ROLE_X"));

		assertThat(visibility.keepIds()).containsExactlyInAnyOrder(1L, 2L);
		assertThat(visibility.keepIds()).doesNotContain(9L);
	}

	@Test
	@DisplayName("同父兄弟：只保留可见支路")
	void resolveKeepsOnlyVisibleSiblingBranch() {
		List<SysMenuEntity> menus = List.of(menu(1L, 0L), menu(2L, 1L), menu(3L, 1L));
		Map<Long, List<String>> bound = Map.of(2L, List.of("ROLE_X"));

		MenuRouteVisibility visibility = MenuRouteVisibilityResolver.resolve(menus, bound, List.of("ROLE_X"));

		assertThat(visibility.keepIds()).containsExactlyInAnyOrder(1L, 2L);
		assertThat(visibility.keepIds()).doesNotContain(3L);
	}

	@Test
	@DisplayName("publicAccess 节点：自身与祖先进入保留集")
	void resolveKeepsPublicAccessAndAncestors() {
		List<SysMenuEntity> menus = List.of(menu(1L, 0L), publicMenu(2L, 1L));

		MenuRouteVisibility visibility = MenuRouteVisibilityResolver.resolve(menus, Map.of(), List.of());

		assertThat(visibility.directVisibleIds()).containsExactly(2L);
		assertThat(visibility.keepIds()).containsExactlyInAnyOrder(1L, 2L);
	}

	@Test
	@DisplayName("用户无角色：仅 publicAccess 子树")
	void resolveWithEmptyUserRolesOnlyPublicAccess() {
		List<SysMenuEntity> menus = List.of(menu(1L, 0L), menu(2L, 1L), publicMenu(3L, 0L));
		Map<Long, List<String>> bound = Map.of(2L, List.of("ROLE_X"));

		MenuRouteVisibility visibility = MenuRouteVisibilityResolver.resolve(menus, bound, List.of());

		assertThat(visibility.directVisibleIds()).containsExactly(3L);
		assertThat(visibility.keepIds()).containsExactly(3L);
	}

	@Test
	@DisplayName("空菜单列表：返回空集")
	void resolveEmptyMenus() {
		MenuRouteVisibility visibility = MenuRouteVisibilityResolver.resolve(List.of(), Map.of(), Set.of());

		assertThat(visibility.directVisibleIds()).isEmpty();
		assertThat(visibility.keepIds()).isEmpty();
	}

}
