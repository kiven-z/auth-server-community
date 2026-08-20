package com.auth.common.ip.format;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 测试{@link DefaultRegionFormatter}
 *
 * @author Bunny
 */
class DefaultRegionFormatterTest {

	private final RegionFormatter formatter = new DefaultRegionFormatter();

	@DisplayName("格式化应正确处理各种输入场景")
	@ParameterizedTest(name = "Format ''{0}'' should return ''{1}''")
	@CsvSource({ "中国|0|福建省|福州市|电信, 中国 福建省 福州市 电信", "0|0|0|0|0, ''", "中国|中国|福建省|福建省|福州市, 中国 福建省 福州市" })
	void format_variousInputs(String raw, String expected) {
		// Act
		String formatted = formatter.format(raw);

		// Assert
		assertEquals(expected, formatted);
	}

}
