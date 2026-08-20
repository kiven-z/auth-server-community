package com.auth.common.web.jackson;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * 翻译成中文： 测试 {@link TrimmingStringDeserializer} 的 JSON 字符串值的修剪功能
 */
class TrimmingStringDeserializerTest {

	private final ObjectMapper mapper = new ObjectMapper()
		.registerModule(new SimpleModule().addDeserializer(String.class, new TrimmingStringDeserializer()));

	@Test
	@DisplayName("JSON string values are stripped of leading and trailing whitespace (strip semantics)")
	void stripsWhitespace() throws Exception {
		// 测试 U+2003 EM SPACE 是空白字符，NBSP (U+00A0) 不是空白字符
		assertEquals("ab", mapper.readValue("\"\\u2003 ab \\u2003\"", String.class));
	}

	@Test
	@DisplayName("JSON null deserializes to null without NPE")
	void nullStaysNull() throws Exception {
		assertNull(mapper.readValue("null", String.class));
	}

	@Test
	@DisplayName("Inner spaces are preserved")
	void preservesInnerSpaces() throws Exception {
		assertEquals("a  b", mapper.readValue("\"  a  b  \"", String.class));
	}

}
