package com.auth.common.core.desensitize;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * {@link DesensitizedStrings} 纯函数行为测试
 */
class DesensitizedStringsTest {

	@Test
	@DisplayName("maskDecimalDisplay：标准千分位与小数")
	void maskDecimalDisplayFormatted() {
		assertEquals("1,234.**", DesensitizedStrings.maskDecimalDisplay("1,234.56"));
	}

	@Test
	@DisplayName("maskDecimalDisplay：小数中含非数字字符时保留该字符")
	void maskDecimalDisplayKeepsNonDigitInFraction() {
		assertEquals("0.**a", DesensitizedStrings.maskDecimalDisplay("0.12a"));
	}

	@Test
	@DisplayName("maskDecimalDisplay：无小数点返回原文")
	void maskDecimalDisplayNoDot() {
		assertEquals("99", DesensitizedStrings.maskDecimalDisplay("99"));
	}

}
