package com.auth.service.system.file.config.properties;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

/**
 * 文件回收站过期清理配置
 *
 * @author Bunny
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "auth.file.recycle-cleanup")
@Configuration
@Validated
public class FileRecycleCleanupProperties {

	/**
	 * 是否启用过期回收站清理
	 */
	@NotNull
	private Boolean enabled = true;

	/**
	 * 逻辑删除后保留天数
	 */
	@Min(1)
	@NotNull
	private Integer retentionDays = 90;

	/**
	 * 清理任务 Cron（默认每天凌晨 3 点）
	 */
	@NotBlank
	private String cron = "0 0 3 * * ?";

	/**
	 * 单次 Job 最多批次数，防止积压拖垮实例
	 */
	@Min(1)
	@NotNull
	private Integer maxRounds = 20;

}
