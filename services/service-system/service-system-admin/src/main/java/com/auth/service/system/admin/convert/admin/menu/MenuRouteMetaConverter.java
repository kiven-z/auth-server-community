package com.auth.service.system.admin.convert.admin.menu;

import cn.hutool.core.text.CharSequenceUtil;
import com.auth.service.system.admin.model.entity.SysMenuEntity;
import com.auth.service.system.admin.model.vo.menu.RouteMetaVO;
import com.auth.service.system.admin.model.vo.menu.RouteTransitionVO;
import lombok.experimental.UtilityClass;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 路由 meta VO 构建（菜单实体 + 角色上下文）。
 *
 * @author Bunny
 */
@UtilityClass
public class MenuRouteMetaConverter {

	/**
	 * 由菜单实体与关联上下文生成路由 meta。
	 * @param menu 菜单实体
	 * @param context 角色聚合上下文
	 * @return 路由 meta；menu 为 null 时返回 null
	 */
	public static RouteMetaVO fromMenuEntity(SysMenuEntity menu, MenuConvertContext context) {
		if (menu == null) {
			return null;
		}

		RouteMetaVO meta = new RouteMetaVO();
		meta.setTitle(menu.getTitle());
		meta.setIcon(menu.getIcon());
		meta.setShowLink(menu.getShowLink());
		Long menuId = menu.getId();
		// null 视为 false；祖先壳可由上下文强制为登录可见
		boolean publicAccess = Boolean.TRUE.equals(menu.getPublicAccess())
				|| (context.forcePublicAccessIds() != null && context.forcePublicAccessIds().contains(menuId));
		meta.setPublicAccess(publicAccess);
		meta.setRank(menu.getMenuRank());
		meta.setExtraIcon(menu.getExtraIcon());
		meta.setShowParent(menu.getShowParent());
		meta.setKeepAlive(menu.getKeepAlive());
		meta.setFrameSrc(menu.getFrameSrc());
		meta.setFrameLoading(menu.getFrameLoading());
		meta.setHiddenTag(menu.getHiddenTag());
		meta.setDynamicLevel(menu.getDynamicLevel());
		meta.setActivePath(menu.getActivePath());
		meta.setFixedTag(menu.getFixedTag());

		Map<Long, List<String>> roleMap = context.roleMap() == null ? Collections.emptyMap() : context.roleMap();
		meta.setRoles(roleMap.getOrDefault(menuId, Collections.emptyList()));
		meta.setTransition(transitionFromMenuEntity(menu));
		return meta;
	}

	/**
	 * 由菜单实体生成 meta.transition（animate.css 使用 animate__ 前缀）。
	 * @param entity 菜单
	 * @return 过渡 VO，字段均可为空
	 */
	private static RouteTransitionVO transitionFromMenuEntity(SysMenuEntity entity) {
		if (entity == null) {
			return null;
		}
		RouteTransitionVO vo = new RouteTransitionVO();
		String transitionName = entity.getTransitionName();
		vo.setName(CharSequenceUtil.isBlank(transitionName) ? transitionName
				: "animate__animated animate__" + transitionName + " ");

		String enterTransition = entity.getEnterTransition();
		vo.setEnterTransition(
				CharSequenceUtil.isBlank(enterTransition) ? enterTransition : "animate__" + enterTransition);

		String leaveTransition = entity.getLeaveTransition();
		vo.setLeaveTransition(
				CharSequenceUtil.isBlank(leaveTransition) ? leaveTransition : "animate__" + leaveTransition);

		boolean blank = CharSequenceUtil.isBlank(vo.getName()) && CharSequenceUtil.isBlank(vo.getEnterTransition())
				&& CharSequenceUtil.isBlank(vo.getLeaveTransition());
		return blank ? null : vo;
	}

}
