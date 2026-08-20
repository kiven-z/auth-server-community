package com.auth.service.system.file.config.properties;

import lombok.Getter;
import lombok.Setter;

/**
 * S3 协议统一配置：所有支持 S3 兼容 API 的存储平台都通过这份配置驱动
 *
 * @author Bunny
 */
@Getter
@Setter
public class S3PlatformProfile {

	/**
	 * 服务端点，形如 http(s)://xxx；用于 SDK 请求路由与 URL 拼接
	 */
	private String endpoint;

	/**
	 * region 名称（AWS/腾讯/华为等真实 region；S3 SDK 强制非空）
	 */
	private String region;

	private String accessKey;

	private String secretKey;

	private String bucket;

	/**
	 * 对外访问域名（可选）。配置后优先生效（CDN/CNAME）；未配置时按 {@link #pathStyleAccess} 从 endpoint + bucket 推导
	 */
	private String publicUrl;

	/**
	 * 是否使用 path-style（endpoint/bucket/key）
	 * <ul>
	 * <li>MinIO 常用 true</li>
	 * <li>阿里云 OSS 用虚拟主机风格（false）</li>
	 * </ul>
	 */
	private boolean pathStyleAccess = true;

	/**
	 * 复制一份配置，避免 resolve 时改写 Spring 绑定的共享实例
	 * @return 浅拷贝
	 */
	public S3PlatformProfile copy() {
		S3PlatformProfile copy = new S3PlatformProfile();
		copy.endpoint = this.endpoint;
		copy.region = this.region;
		copy.accessKey = this.accessKey;
		copy.secretKey = this.secretKey;
		copy.bucket = this.bucket;
		copy.publicUrl = this.publicUrl;
		copy.pathStyleAccess = this.pathStyleAccess;
		return copy;
	}

}
