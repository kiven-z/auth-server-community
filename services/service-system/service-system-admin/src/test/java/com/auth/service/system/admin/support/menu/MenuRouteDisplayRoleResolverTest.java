package com.auth.service.system.admin.support.menu;

import com.auth.service.system.admin.model.entity.SysMenuEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link MenuRouteDisplayRoleResolver} 单元测试
 */
@DisplayName("MenuRouteDisplayRoleResolver 展示角色冒泡")
class MenuRouteDisplayRoleResolverTest {

	private static SysMenuEntity menu(Long id, Long parentId) {
		SysMenuEntity entity = new SysMenuEntity();
		entity.setId(id);
		entity.setParentId(parentId);
		entity.setPublicAccess(Boolean.FALSE);
		return entity;
	}

	private static SysMenuEntity publicMenu(Long id) {
		SysMenuEntity entity = menu(id, 1L);
		entity.setPublicAccess(Boolean.TRUE);
		return entity;
	}

	@Test
	@DisplayName("祖先壳冒泡命中角色；直接可见保留库绑定")
	void resolveBubblesHitRolesToAncestorShells() {
		List<SysMenuEntity> menus = List.of(menu(1L, 0L), menu(2L, 1L), menu(3L, 2L));
		Map<Long, List<String>> bound = Map.of(3L, List.of("ROLE_X", "ROLE_Y"));
		MenuRouteVisibility visibility = new MenuRouteVisibility(Set.of(3L), Set.of(1L, 2L, 3L));

		MenuRouteDisplayMeta meta = MenuRouteDisplayRoleResolver.resolve(menus, visibility, bound, List.of("ROLE_X"));

		assertThat(meta.roleMap().get(3L)).containsExactly("ROLE_X", "ROLE_Y");
		assertThat(meta.roleMap().get(2L)).containsExactly("ROLE_X");
		assertThat(meta.roleMap().get(1L)).containsExactly("ROLE_X");
		assertThat(meta.forcePublicAccessIds()).isEmpty();
	}

	@Test
	@DisplayName("仅 publicAccess 子孙：祖先壳强制 publicAccess")
	void resolveForcesPublicAccessOnShellOfPublicLeaf() {
		List<SysMenuEntity> menus = List.of(menu(1L, 0L), publicMenu(2L));
		MenuRouteVisibility visibility = new MenuRouteVisibility(Set.of(2L), Set.of(1L, 2L));

		MenuRouteDisplayMeta meta = MenuRouteDisplayRoleResolver.resolve(menus, visibility, Map.of(), List.of());

		assertThat(meta.roleMap().get(2L)).isEmpty();
		assertThat(meta.roleMap().get(1L)).isEmpty();
		assertThat(meta.forcePublicAccessIds()).containsExactly(1L);
	}

	@Test
	@DisplayName("祖先壳同时有角色子孙时不强制 publicAccess")
	void resolveDoesNotForcePublicWhenRolesBubbled() {
		List<SysMenuEntity> menus = List.of(menu(1L, 0L), menu(2L, 1L), publicMenu(3L));
		Map<Long, List<String>> bound = Map.of(2L, List.of("ROLE_X"));
		MenuRouteVisibility visibility = new MenuRouteVisibility(Set.of(2L, 3L), Set.of(1L, 2L, 3L));

		MenuRouteDisplayMeta meta = MenuRouteDisplayRoleResolver.resolve(menus, visibility, bound, List.of("ROLE_X"));

		assertThat(meta.roleMap().get(1L)).containsExactly("ROLE_X");
		assertThat(meta.forcePublicAccessIds()).isEmpty();
	}

}
