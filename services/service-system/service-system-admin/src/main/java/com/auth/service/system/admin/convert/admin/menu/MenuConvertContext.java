package com.auth.service.system.admin.convert.admin.menu;

import lombok.Builder;
import lombok.Value;
import lombok.experimental.Accessors;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 路由 meta 转换上下文（展示用角色与祖先壳 publicAccess）
 *
 * @author Bunny
 */
@Value
@Builder
@Accessors(fluent = true)
public class MenuConvertContext {

	/**
	 * 菜单 ID -> 展示角色编码
	 */
	Map<Long, List<String>> roleMap;

	/**
	 * 祖先壳需强制 publicAccess 的菜单 ID（仅 VO，不写库）
	 */
	@Builder.Default
	Set<Long> forcePublicAccessIds = Collections.emptySet();

}
