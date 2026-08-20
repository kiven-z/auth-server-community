package com.auth.service.system.authorization.config;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

/**
 * system 侧授权失效投递配置
 *
 * @author Bunny
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "auth.authorization")
@Configuration
@Validated
public class AuthorizationInvalidationProperties {

	/**
	 * 事务提交后是否同步 Feign 调用 auth（过渡方案；失败由 Outbox Job 补偿）
	 */
	@NotNull
	private Boolean syncDispatchEnabled = true;

	/**
	 * 补偿 Job 扫描间隔（毫秒）
	 */
	@Min(5000)
	@NotNull
	private Long pollIntervalMs = 30_000L;

	/**
	 * 失败后重试间隔基数（秒），实际为 base * 2^retryCount
	 */
	@Min(1)
	@NotNull
	private Integer retryBackoffSeconds = 30;

	/**
	 * 人工重试允许解锁 PROCESSING 的抢占超时（分钟） 超时或 force=true 时可重试
	 */
	@Min(1)
	@NotNull
	private Integer manualProcessingUnlockTimeoutMinutes = 5;

	/**
	 * SUCCESS Outbox 保留天数，超出后由清理 Job 物理删除
	 */
	@Min(1)
	@NotNull
	private Integer successRetentionDays = 90;

	/**
	 * 是否启用 SUCCESS Outbox 历史清理 Job
	 */
	@NotNull
	private Boolean successCleanupEnabled = false;

	/**
	 * SUCCESS Outbox 清理 Job Cron（默认每天 03:30）
	 */
	@NotBlank
	private String successCleanupCron = "0 30 3 * * ?";

}
