package com.auth.common.ip.format;

/**
 * 区域格式化器用于ip2region原始文本
 *
 * @author Bunny
 */
public interface RegionFormatter {

	/**
	 * 格式化原始区域文本为人性化的字符串
	 * @param rawRegion 数据库中的原始区域
	 * @return 格式化后的区域字符串，永远不会为空
	 */
	String format(String rawRegion);

}
