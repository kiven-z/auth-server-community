package com.auth.service.system.file.storage.core.capability;

/**
 * 图片样式 URL 能力
 *
 * @author Bunny
 */
public interface ImageStyleUrlCapability extends StoragePlatformCapability {

	/**
	 * 拼接图片样式访问地址
	 * @param publicUrl 原始公开访问地址
	 * @param style 样式名（由平台管控台预先定义）
	 * @return 带样式参数的访问地址
	 */
	String buildStyleUrl(String publicUrl, String style);

}
