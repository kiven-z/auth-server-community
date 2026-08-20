package com.auth.service.system.admin.support.menu;

import com.auth.module.security.contract.constants.PermissionConstant;
import com.auth.service.system.admin.mapper.admin.menu.SysMenuRoleMapper;
import com.auth.service.system.admin.model.entity.SysMenuEntity;
import com.auth.service.system.admin.model.po.menu.SysMenuRoleLinkRowPO;
import com.auth.service.system.admin.model.vo.menu.RouteNodeVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

/**
 * {@link MenuWebRouteTreeAssembler} 单元测试
 */
@DisplayName("MenuWebRouteTreeAssembler 动态路由树")
@ExtendWith(MockitoExtension.class)
class MenuWebRouteTreeAssemblerTest {

	@Mock
	private SysMenuRoleMapper sysMenuRoleMapper;

	private MenuWebRouteTreeAssembler assembler;

	private static SysMenuEntity menu(Long id, Long parentId, String name) {
		SysMenuEntity entity = new SysMenuEntity();
		entity.setId(id);
		entity.setParentId(parentId);
		entity.setName(name);
		entity.setPath("/" + name);
		entity.setTitle(name);
		entity.setMenuRank(id.intValue());
		entity.setPublicAccess(Boolean.FALSE);
		entity.setShowLink(Boolean.TRUE);
		return entity;
	}

	private static SysMenuRoleLinkRowPO link(Long menuId) {
		SysMenuRoleLinkRowPO row = new SysMenuRoleLinkRowPO();
		row.setMenuId(menuId);
		row.setRoleCode("ROLE_X");
		return row;
	}

	@BeforeEach
	void setUp() {
		assembler = new MenuWebRouteTreeAssembler(sysMenuRoleMapper);
	}

	@Test
	@DisplayName("仅子级绑定：出树含祖先壳且祖先 meta.roles 冒泡命中角色")
	void buildRoutesIncludesAncestorShellsWithBubbledRoles() {
		List<SysMenuEntity> menus = List.of(menu(1L, 0L, "system"), menu(2L, 1L, "menu"), menu(3L, 2L, "list"));
		when(sysMenuRoleMapper.selectActiveRoleLinksByMenuIds(anyList())).thenReturn(List.of(link(3L)));

		List<RouteNodeVO> roots = assembler.buildRoutes(menus, List.of("ROLE_X"));

		assertThat(roots).hasSize(1);
		RouteNodeVO system = roots.get(0);
		assertThat(system.getName()).isEqualTo("system");
		assertThat(system.getMeta().getRoles()).containsExactly("ROLE_X");
		assertThat(system.getChildren()).hasSize(1);
		RouteNodeVO menuNode = system.getChildren().get(0);
		assertThat(menuNode.getName()).isEqualTo("menu");
		assertThat(menuNode.getMeta().getRoles()).containsExactly("ROLE_X");
		assertThat(menuNode.getChildren()).hasSize(1);
		assertThat(menuNode.getChildren().get(0).getName()).isEqualTo("list");
		assertThat(menuNode.getChildren().get(0).getMeta().getRoles()).containsExactly("ROLE_X");
	}

	@Test
	@DisplayName("不可见兄弟不出现在树中")
	void buildRoutesOmitsUnauthorizedSibling() {
		List<SysMenuEntity> menus = List.of(menu(1L, 0L, "root"), menu(2L, 1L, "visible"), menu(3L, 1L, "hidden"));
		when(sysMenuRoleMapper.selectActiveRoleLinksByMenuIds(anyList())).thenReturn(List.of(link(2L)));

		List<RouteNodeVO> roots = assembler.buildRoutes(menus, List.of("ROLE_X"));

		assertThat(roots).hasSize(1);
		assertThat(roots.get(0).getChildren()).extracting(RouteNodeVO::getName).containsExactly("visible");
	}

	@Test
	@DisplayName("无命中角色时返回空树")
	void buildRoutesReturnsEmptyWhenNoHit() {
		List<SysMenuEntity> menus = List.of(menu(1L, 0L, "root"), menu(2L, 1L, "child"));
		when(sysMenuRoleMapper.selectActiveRoleLinksByMenuIds(anyList())).thenReturn(List.of(link(2L)));

		assertThat(assembler.buildRoutes(menus, List.of("ROLE_OTHER"))).isEmpty();
	}

	@Test
	@DisplayName("管理员路径：全量树且 roles 为 ADMIN_ROLES")
	void buildAdminRoutesKeepsFullTree() {
		List<SysMenuEntity> menus = List.of(menu(1L, 0L, "root"), menu(2L, 1L, "child"));

		List<RouteNodeVO> roots = assembler.buildAdminRoutes(menus);

		assertThat(roots).hasSize(1);
		assertThat(roots.get(0).getMeta().getRoles()).isEqualTo(PermissionConstant.ADMIN_ROLES);
		assertThat(roots.get(0).getChildren()).hasSize(1);
	}

}
