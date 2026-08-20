package com.auth.service.system.admin.convert.admin.menu;

import com.auth.service.system.admin.model.entity.SysMenuEntity;
import com.auth.service.system.admin.model.vo.menu.RouteMetaVO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link MenuRouteMetaConverter} 映射校验
 */
class MenuRouteMetaConverterTest {

	@Test
	@DisplayName("从菜单与上下文组装 meta，含 rank、roles")
	void fromMenuEntity_mapsFieldsAndContext() {
		// 组装含排序与过渡动画的菜单实体
		SysMenuEntity menu = new SysMenuEntity();
		menu.setId(10L);
		menu.setTitle("系统管理");
		menu.setIcon("ri:settings-3-line");
		menu.setMenuRank(99);
		menu.setTransitionName("fade");

		MenuConvertContext context = MenuConvertContext.builder().roleMap(Map.of(10L, List.of("admin"))).build();

		RouteMetaVO meta = MenuRouteMetaConverter.fromMenuEntity(menu, context);

		// 校验基础字段、角色与过渡动画映射
		assertThat(meta.getTitle()).isEqualTo("系统管理");
		assertThat(meta.getRank()).isEqualTo(99);
		assertThat(meta.getRoles()).containsExactly("admin");
		assertThat(meta.getPublicAccess()).isFalse();
		assertThat(meta.getTransition()).isNotNull();
		assertThat(meta.getTransition().getName()).contains("fade");
	}

	@Test
	@DisplayName("菜单 ID 无上下文条目时使用空角色列表")
	void fromMenuEntity_usesEmptyListsWhenContextMissing() {
		// 上下文无该菜单角色条目
		SysMenuEntity menu = new SysMenuEntity();
		menu.setId(1L);
		MenuConvertContext context = MenuConvertContext.builder().roleMap(Map.of()).build();

		RouteMetaVO meta = MenuRouteMetaConverter.fromMenuEntity(menu, context);

		// 空角色列表保持不可见语义，由前端 isOneOfArray 处理
		assertThat(meta.getRoles()).isEmpty();
		assertThat(meta.getPublicAccess()).isFalse();
	}

	@Test
	@DisplayName("publicAccess 为 true 时映射为 true")
	void fromMenuEntity_mapsPublicAccessTrue() {
		// 开启登录可见
		SysMenuEntity menu = new SysMenuEntity();
		menu.setId(2L);
		menu.setPublicAccess(Boolean.TRUE);
		MenuConvertContext context = MenuConvertContext.builder().roleMap(Map.of()).build();

		RouteMetaVO meta = MenuRouteMetaConverter.fromMenuEntity(menu, context);

		assertThat(meta.getPublicAccess()).isTrue();
		assertThat(meta.getRoles()).isEmpty();
	}

	@Test
	@DisplayName("publicAccess 为 false 时映射为 false")
	void fromMenuEntity_mapsPublicAccessFalse() {
		// 显式关闭登录可见
		SysMenuEntity menu = new SysMenuEntity();
		menu.setId(3L);
		menu.setPublicAccess(Boolean.FALSE);
		MenuConvertContext context = MenuConvertContext.builder().roleMap(Map.of()).build();

		RouteMetaVO meta = MenuRouteMetaConverter.fromMenuEntity(menu, context);

		assertThat(meta.getPublicAccess()).isFalse();
	}

	@Test
	@DisplayName("publicAccess 为 null 时映射为 false")
	void fromMenuEntity_mapsNullPublicAccessToFalse() {
		// 未设置时默认按角色校验
		SysMenuEntity menu = new SysMenuEntity();
		menu.setId(4L);
		menu.setPublicAccess(null);
		MenuConvertContext context = MenuConvertContext.builder().roleMap(Map.of()).build();

		RouteMetaVO meta = MenuRouteMetaConverter.fromMenuEntity(menu, context);

		assertThat(meta.getPublicAccess()).isFalse();
	}

	@Test
	@DisplayName("祖先壳 forcePublicAccessIds 强制 publicAccess")
	void fromMenuEntity_forcesPublicAccessFromContext() {
		SysMenuEntity menu = new SysMenuEntity();
		menu.setId(5L);
		menu.setPublicAccess(Boolean.FALSE);
		MenuConvertContext context = MenuConvertContext.builder()
			.roleMap(Map.of())
			.forcePublicAccessIds(Set.of(5L))
			.build();

		RouteMetaVO meta = MenuRouteMetaConverter.fromMenuEntity(menu, context);

		assertThat(meta.getPublicAccess()).isTrue();
	}

}
