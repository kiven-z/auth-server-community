package com.auth.service.system.file.config.properties.platform;

import lombok.Getter;
import lombok.Setter;

/**
 * 阿里云 OSS 存储配置
 *
 * @author Bunny
 */
@Getter
@Setter
public class AliyunOssStorageProperties {

	private String endpoint;

	private String accessKeyId;

	private String accessKeySecret;

	private String bucket;

	/**
	 * 配置后优先生效（CDN/CNAME）；未配置时按 virtual-hosted 从 endpoint + bucket 推导
	 */
	private String publicUrl;

}
