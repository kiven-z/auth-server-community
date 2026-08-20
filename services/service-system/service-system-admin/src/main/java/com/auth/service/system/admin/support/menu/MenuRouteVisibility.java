package com.auth.service.system.admin.support.menu;

import java.util.Set;

/**
 * 菜单路由可见集（直接可见 + 保留集）
 *
 * @param directVisibleIds 角色命中或 publicAccess 的菜单 ID
 * @param keepIds 直接可见及其祖先（出树保留集）
 * @author Bunny
 */
public record MenuRouteVisibility(Set<Long> directVisibleIds, Set<Long> keepIds) {
}
