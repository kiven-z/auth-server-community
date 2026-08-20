package com.auth.module.security.autoconfigure.config.user;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.validation.annotation.Validated;

/**
 * 在 Nacos 中进行配置刷新 文件名：common-user-config.yaml 分组：AUTH_COMMON
 *
 * @author Bunny
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "auth.common.user-config")
@RefreshScope
@Validated
public class UserConfigProperties {

	/**
	 * 历史密码保留天数
	 */
	@Max(365)
	@Min(1)
	@NotNull
	private Integer passwordHistoryRetentionDays = 30;

	/**
	 * 历史密码检查次数
	 */
	@Max(20)
	@Min(1)
	@NotNull
	private Integer passwordHistoryCheckCount = 5;

	/**
	 * 最大密码尝试次数
	 */
	@Max(30)
	@Min(3)
	@NotNull
	private Integer maxPasswordAttempts = 6;

	/**
	 * 最大会话数
	 */
	@Max(100)
	@Min(1)
	@NotNull
	private Integer maxSessionCount = 3;

	/**
	 * 会话限制策略
	 */
	private SessionLimitStrategy sessionLimitStrategy = SessionLimitStrategy.EVICT_OLDEST;

}
