package com.auth.service.system.admin.support.menu;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 动态路由展示用 meta（不写库）
 *
 * @param roleMap 菜单 ID → 展示角色编码
 * @param forcePublicAccessIds 祖先壳需标记登录可见的菜单 ID
 * @author Bunny
 */
public record MenuRouteDisplayMeta(Map<Long, List<String>> roleMap, Set<Long> forcePublicAccessIds) {
}
