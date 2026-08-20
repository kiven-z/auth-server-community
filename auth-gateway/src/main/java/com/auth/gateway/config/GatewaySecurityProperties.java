package com.auth.gateway.config;

import com.auth.module.security.core.matcher.AntPathMatcher;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.List;

/**
 * 网关安全策略配置
 *
 * @author Bunny
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "auth.gateway.security")
@RefreshScope
@Component
@Validated
public class GatewaySecurityProperties {

	/**
	 * 是否开启严格校验模式
	 */
	private boolean strictEnabled = false;

	/**
	 * 严格校验路径（Ant 风格）
	 */
	private List<String> strictPatterns = new ArrayList<>();

	/**
	 * 当前路径是否命中严格校验策略
	 * @param path 请求路径
	 * @return 是否命中严格校验
	 */
	public boolean isStrictPath(String path) {
		return strictEnabled && AntPathMatcher.matchesAnyAnt(path, strictPatterns);
	}

}
