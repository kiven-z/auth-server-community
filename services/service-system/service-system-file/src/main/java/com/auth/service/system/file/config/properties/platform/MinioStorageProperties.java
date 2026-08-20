package com.auth.service.system.file.config.properties.platform;

import lombok.Getter;
import lombok.Setter;

/**
 * MinIO 存储配置
 *
 * @author Bunny
 */
@Getter
@Setter
public class MinioStorageProperties {

	private String endpoint;

	private String accessKey;

	private String secretKey;

	private String bucket;

	/**
	 * 对外访问域名（可选）。配置后优先生效；未配置时按 path-style 从 endpoint + bucket 推导
	 */
	private String publicUrl;

}
