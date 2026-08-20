package com.auth.service.system.authorization.dispatch.query;

import lombok.Builder;
import lombok.Value;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.Objects;

/**
 * 用户生效角色码与权限码快照（供 admin 动态菜单等只读场景）
 *
 * @author Bunny
 */
@Value
@Accessors(fluent = true)
public class UserEffectiveCodesSnapshot {

	/**
	 * 角色码
	 */
	List<String> roleCodes;

	/**
	 * 权限码
	 */
	List<String> permissionCodes;

	/**
	 * 构建快照并规范化列表字段，避免 null 向外泄漏。
	 * @param roleCodes 角色码
	 * @param permissionCodes 权限码
	 */
	@Builder
	public UserEffectiveCodesSnapshot(List<String> roleCodes, List<String> permissionCodes) {
		this.roleCodes = List.copyOf(Objects.requireNonNullElse(roleCodes, List.of()));
		this.permissionCodes = List.copyOf(Objects.requireNonNullElse(permissionCodes, List.of()));
	}

}
