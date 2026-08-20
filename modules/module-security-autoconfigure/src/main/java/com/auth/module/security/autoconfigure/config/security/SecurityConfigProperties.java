package com.auth.module.security.autoconfigure.config.security;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;

import java.util.List;

/**
 * 安全配置属性（路径匹配与审计策略）；路径用于 Spring Security request matcher（如 Ant 风格）
 *
 * @author Bunny
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "auth.common.security")
@RefreshScope
public class SecurityConfigProperties {

	/**
	 * 总是允许的路径（匿名） 通常对应 permitAll 的请求匹配；命中该列表的请求不会触发认证
	 */
	private List<String> permitPaths = List.of();

	/**
	 * 审计策略
	 */
	private AuditPolicy auditPolicy = AuditPolicy.ALL_RECORD;

	/**
	 * 当前策略是否为全量审计（{@link AuditPolicy#ALL_RECORD}）
	 */
	public boolean isFullAudit() {
		return AuditPolicy.ALL_RECORD.equals(this.auditPolicy);
	}

}
