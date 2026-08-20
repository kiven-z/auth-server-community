package com.auth.common.ip.format;

import cn.hutool.core.text.CharSequenceUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * 默认的区域格式化器用于ip2region输出
 *
 * @author Bunny
 */
public class DefaultRegionFormatter implements RegionFormatter {

	private static final String SPLITTER = "|";

	private static final String ZERO = "0";

	/**
	 * 格式化原始区域字符串
	 * @param rawRegion 原始区域字符串
	 * @return 格式化后的区域字符串
	 */
	@Override
	public String format(String rawRegion) {
		if (CharSequenceUtil.isBlank(rawRegion)) {
			return CharSequenceUtil.EMPTY;
		}

		// 将原始区域字符串按分隔符分割为数组
		String[] parts = CharSequenceUtil.splitToArray(rawRegion, SPLITTER);
		if (parts == null || parts.length == 0) {
			return CharSequenceUtil.EMPTY;
		}

		// 创建一个列表来存储保留的区域部分
		List<String> kept = new ArrayList<>(parts.length);
		String last = null;
		for (String part : parts) {
			// 如果区域部分为空或为0，则跳过
			boolean partIsBlank = CharSequenceUtil.isBlank(part) || ZERO.equals(part);
			// 如果上一个区域部分与当前区域部分相同，则跳过
			boolean lastSame = last != null && last.equals(part);
			if (partIsBlank || lastSame) {
				continue;
			}

			// 将区域部分添加到保留的列表中
			kept.add(part);
			last = part;
		}

		// 如果保留的列表为空，则返回空字符串
		if (kept.isEmpty()) {
			return CharSequenceUtil.EMPTY;
		}

		// 将保留的区域部分用空格连接起来
		return CharSequenceUtil.join(CharSequenceUtil.SPACE, kept);
	}

}
