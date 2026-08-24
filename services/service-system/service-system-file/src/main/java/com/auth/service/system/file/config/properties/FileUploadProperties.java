package com.auth.service.system.file.config.properties;

import com.auth.module.file.api.model.enums.StoragePlatformEnum;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * 文件上传配置
 *
 * @author Bunny
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "auth.file")
@Configuration
@Validated
public class FileUploadProperties {

	/**
	 * 默认上传平台
	 */
	@NotNull(message = "默认上传平台不能为空")
	private StoragePlatformEnum defaultPlatform = StoragePlatformEnum.MINIO;

	/**
	 * S3 协议统一配置表（按平台枚举名）；缺省平台在使用时校验失败
	 */
	private Map<StoragePlatformEnum, S3PlatformProfile> platforms = new EnumMap<>(StoragePlatformEnum.class);

	/**
	 * 临时访问地址过期秒数
	 */
	@Min(value = 1, message = "临时访问时间不合法")
	@NotNull(message = "临时访问地址过期秒数不能为空")
	private Integer expireSeconds = 300;

	/**
	 * 允许预览的内容类型白名单
	 * <p>
	 * List<{@link MediaType}>
	 * </p>
	 */
	private List<String> allowedContentTypes = List.of("image/*", "application/pdf");

}
