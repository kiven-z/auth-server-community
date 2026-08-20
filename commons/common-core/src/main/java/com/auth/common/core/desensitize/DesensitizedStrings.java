package com.auth.common.core.desensitize;

import cn.hutool.core.text.CharSequenceUtil;
import lombok.experimental.UtilityClass;

import java.util.Collections;

/**
 * 字符串脱敏工具，供 JSON 序列化与单测复用
 *
 * @author Bunny
 */
@UtilityClass
public class DesensitizedStrings {

	/**
	 * 按前后保留长度与掩码字符进行自定义脱敏
	 * @param value 原始值
	 * @param prefixLen 前缀保留长度
	 * @param suffixLen 后缀保留长度
	 * @param symbol 中间替换字符（通常一位，如 *）
	 * @return 脱敏后的字符串；空白输入返回空串；长度不足时返回原文
	 */
	public static String custom(String value, int prefixLen, int suffixLen, String symbol) {
		if (CharSequenceUtil.isBlank(value)) {
			return "";
		}
		if (value.length() <= prefixLen + suffixLen) {
			return value;
		}

		String mask = CharSequenceUtil.isBlank(symbol) ? "*" : symbol;
		String prefix = value.substring(0, prefixLen);
		String suffix = value.substring(value.length() - suffixLen);
		String middle = String.join("", Collections.nCopies(value.length() - prefixLen - suffixLen, mask));
		return prefix + middle + suffix;
	}

	/**
	 * 金额类展示字符串：以最后一个 . 为小数点，左侧原样保留，右侧将十进制数字替换为 *
	 * <p>
	 * 适用于 1,234.56 → 1,234.**；无小数点时返回原文
	 * </p>
	 * @param value 已格式化的金额字符串（如含千分位逗号）
	 * @return 脱敏后的字符串
	 */
	public static String maskDecimalDisplay(String value) {
		if (CharSequenceUtil.isBlank(value)) {
			return "";
		}
		int lastDot = value.lastIndexOf('.');
		if (lastDot < 0) {
			return value;
		}
		String intPart = value.substring(0, lastDot);
		String frac = value.substring(lastDot + 1);
		StringBuilder maskedFrac = new StringBuilder(frac.length());
		for (int i = 0; i < frac.length(); i++) {
			char c = frac.charAt(i);
			maskedFrac.append(Character.isDigit(c) ? '*' : c);
		}
		return intPart + "." + maskedFrac;
	}

}
