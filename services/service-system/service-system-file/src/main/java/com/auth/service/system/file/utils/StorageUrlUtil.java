package com.auth.service.system.file.utils;

import cn.hutool.core.text.CharSequenceUtil;
import com.auth.service.system.file.config.properties.S3PlatformProfile;
import lombok.experimental.UtilityClass;

import java.net.URI;

/**
 * 存储访问地址工具类
 *
 * @author Bunny
 */
@UtilityClass
public class StorageUrlUtil {

	private static final String HTTP_PREFIX = "http://";

	private static final String HTTPS_PREFIX = "https://";

	/**
	 * 拼接公共访问地址和对象路径（自动裁剪首尾斜杠，避免出现 //）
	 * @param publicUrl 访问地址
	 * @param objectPath 对象路径
	 * @return 拼接后的 URL
	 */
	public static String concatPublicUrl(String publicUrl, String objectPath) {
		String base = CharSequenceUtil.removeSuffix(publicUrl, "/");
		String path = CharSequenceUtil.removePrefix(objectPath, "/");
		return base + "/" + path;
	}

	/**
	 * 规范化 endpoint 为绝对 HTTP(S) 基础地址；缺协议时默认补 https
	 * @param endpoint 原始 endpoint
	 * @return 标准化后的基础地址
	 */
	public static String normalizeHttpBaseUrl(String endpoint) {
		String value = CharSequenceUtil.removeSuffix(endpoint, "/");
		if (CharSequenceUtil.isBlank(value)) {
			return value;
		}
		if (CharSequenceUtil.startWithIgnoreCase(value, HTTP_PREFIX)
				|| CharSequenceUtil.startWithIgnoreCase(value, HTTPS_PREFIX)) {
			return value;
		}
		return HTTPS_PREFIX + value;
	}

	/**
	 * 按平台配置解析对象公开访问地址
	 * <ul>
	 * <li>有 publicUrl：CDN/CNAME 等对外域名优先</li>
	 * <li>path-style：{endpoint/{bucket}/{key}}</li>
	 * <li>virtual-hosted：{scheme://{bucket}.{host}/{key}}</li>
	 * </ul>
	 * @param profile 已规范化的 S3 协议配置
	 * @param bucket 存储桶（可空，空则用 profile.bucket）
	 * @param objectKey 对象键
	 * @return 公开访问 URL
	 */
	public static String resolveObjectUrl(S3PlatformProfile profile, String bucket, String objectKey) {
		if (CharSequenceUtil.isNotBlank(profile.getPublicUrl())) {
			return concatPublicUrl(profile.getPublicUrl(), objectKey);
		}

		String endpoint = normalizeHttpBaseUrl(profile.getEndpoint());
		String resolvedBucket = CharSequenceUtil.blankToDefault(bucket, profile.getBucket());
		if (profile.isPathStyleAccess()) {
			return concatPublicUrl(endpoint + "/" + resolvedBucket, objectKey);
		}

		String virtualHostedBaseUrl = toVirtualHostedBaseUrl(endpoint, resolvedBucket);
		return concatPublicUrl(virtualHostedBaseUrl, objectKey);
	}

	/**
	 * 将区域 endpoint 转为虚拟主机风格基址：bucket.endpoint-host
	 * @param endpoint 绝对 endpoint
	 * @param bucket 存储桶名
	 * @return 虚拟主机风格基址
	 */
	private static String toVirtualHostedBaseUrl(String endpoint, String bucket) {
		URI uri = URI.create(endpoint);
		String scheme = CharSequenceUtil.blankToDefault(uri.getScheme(), "https");
		String authority = uri.getAuthority();
		if (CharSequenceUtil.isBlank(authority)) {
			return scheme + "://" + bucket;
		}
		return scheme + "://" + bucket + "." + authority;
	}

}
