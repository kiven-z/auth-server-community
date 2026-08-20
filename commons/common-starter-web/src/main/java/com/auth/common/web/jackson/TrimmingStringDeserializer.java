package com.auth.common.web.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdScalarDeserializer;

import java.io.IOException;

/**
 * 修剪JSON字符串值的前后Unicode空白字符，使用{@link String#strip()}
 * <p>
 * JSONnull保持为null（不修剪）注意：换行符（U+00A0）不被{@link String#strip()}视为空白（与{@link Character#isWhitespace(int)}相同）
 * </p>
 *
 * @author Bunny
 */
public class TrimmingStringDeserializer extends StdScalarDeserializer<String> {

	/**
	 * 修剪JSON字符串值的前后Unicode空白字符，使用{@link String#strip()}
	 */
	public TrimmingStringDeserializer() {
		super(String.class);
	}

	@Override
	public String deserialize(JsonParser jsonParser, DeserializationContext ctx) throws IOException {
		String value = jsonParser.getValueAsString();
		if (value == null) {
			return null;
		}
		return value.strip();
	}

}
