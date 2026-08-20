package com.auth.service.auth.model.value.login;

import lombok.Builder;
import lombok.Value;
import lombok.experimental.Accessors;

/**
 * 登录审计请求侧快照：在发布线程从 HTTP 请求解析后固化，供事件监听器持久化
 *
 * @author Bunny
 */
@Value
@Builder
@Accessors(fluent = true)
public class LoginAuditSnapshot {

	/**
	 * 客户端 IP
	 */
	String loginIp;

	/**
	 * 地域描述
	 */
	String loginRegion;

	/**
	 * 原始 User-Agent 头
	 */
	String userAgent;

	/**
	 * 设备类型
	 */
	String deviceType;

	/**
	 * 操作系统
	 */
	String osType;

	/**
	 * 浏览器
	 */
	String browserType;

	/**
	 * 无 HTTP 请求时的空快照（字段均为 null）。
	 * @return 空快照
	 */
	public static LoginAuditSnapshot empty() {
		return LoginAuditSnapshot.builder().build();
	}

}
