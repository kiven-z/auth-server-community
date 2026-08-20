package com.auth.service.auth.model.value.authorization;

import com.auth.module.platform.persistence.model.UserEntity;
import lombok.Builder;
import lombok.Value;
import lombok.experimental.Accessors;

/**
 * 授权上下文用户只读快照（与持久化实体、登录域 LoginAccount 解耦）
 *
 * @author Bunny
 */
@Value
@Builder
@Accessors(fluent = true)
public class UserAccountSnapshot {

	/**
	 * 用户 ID
	 */
	Long id;

	/**
	 * 用户名
	 */
	String username;

	/**
	 * 权限版本
	 */
	Long permVersion;

	/**
	 * 从持久化用户实体构建授权快照
	 * @param user 用户实体
	 * @return 授权快照
	 */
	public static UserAccountSnapshot from(UserEntity user) {
		return UserAccountSnapshot.builder()
			.id(user.getId())
			.username(user.getUsername())
			.permVersion(user.getPermVersion())
			.build();
	}

}
