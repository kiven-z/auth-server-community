package com.auth.service.system.message.support.template;

import com.auth.common.core.utils.JsonSupport;
import com.auth.service.system.message.exception.MessageException;
import com.auth.service.system.message.model.vo.template.MessageTemplateRequireFieldRow;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static com.auth.service.system.common.exception.code.SystemCommonResultCode.*;
import static org.assertj.core.api.Assertions.*;

/**
 * {@link MessageTemplateRequireFieldsJsonSupport} 单元测试
 *
 * @author Bunny
 */
@DisplayName("MessageTemplateRequireFieldsJsonSupport 模板必填变量")
class MessageTemplateRequireFieldsJsonSupportTest {

	private static MessageTemplateRequireFieldRow row(String key, JsonNode exampleValue) {
		MessageTemplateRequireFieldRow row = new MessageTemplateRequireFieldRow();
		row.setKey(key);
		row.setExampleValue(exampleValue);
		return row;
	}

	@Test
	@DisplayName("toJson/parse：往返后保留 key 与 exampleValue")
	void toJson_parse_roundTrip() {
		// 序列化后再解析，字段应保持一致
		MessageTemplateRequireFieldRow row = row("code", JsonSupport.readTree("\"123456\""));

		String json = MessageTemplateRequireFieldsJsonSupport.toJson(List.of(row));
		List<MessageTemplateRequireFieldRow> parsed = MessageTemplateRequireFieldsJsonSupport.parse(json);

		assertThat(parsed).hasSize(1);
		assertThat(parsed.get(0).getKey()).isEqualTo("code");
		assertThat(parsed.get(0).getExampleValue().asText()).isEqualTo("123456");
	}

	@Test
	@DisplayName("parse：空白 JSON 返回空列表")
	void parse_blank_returnsEmpty() {
		assertThat(MessageTemplateRequireFieldsJsonSupport.parse("")).isEmpty();
		assertThat(MessageTemplateRequireFieldsJsonSupport.parse(null)).isEmpty();
	}

	@Test
	@DisplayName("parse：非法 JSON 抛出 DATA_INVALID")
	void parse_invalidJson_throws() {
		assertThatThrownBy(() -> MessageTemplateRequireFieldsJsonSupport.parse("{bad"))
			.isInstanceOf(MessageException.class)
			.extracting(ex -> ((MessageException) ex).getResultCode())
			.isEqualTo(DATA_INVALID);
	}

	@Test
	@DisplayName("parse：缺少 exampleValue 仍可解析（读路径不强制写入校验）")
	void parse_missingExampleValue_returnsRows() {
		// 历史/半成品数据应能被详情读出，不应在 parse 阶段抛 PARAM_REQUIRED
		List<MessageTemplateRequireFieldRow> parsed = MessageTemplateRequireFieldsJsonSupport
			.parse("[{\"key\":\"code\",\"description\":\"验证码\"}]");

		assertThat(parsed).hasSize(1);
		assertThat(parsed.get(0).getKey()).isEqualTo("code");
		assertThat(parsed.get(0).getExampleValue()).isNull();
	}

	@Test
	@DisplayName("validate：缺少 exampleValue 抛出 PARAM_REQUIRED")
	void validate_missingExampleValue_throws() {
		MessageTemplateRequireFieldRow row = new MessageTemplateRequireFieldRow();
		row.setKey("code");
		List<MessageTemplateRequireFieldRow> rows = List.of(row);

		assertThatThrownBy(() -> MessageTemplateRequireFieldsJsonSupport.validate(rows))
			.isInstanceOf(MessageException.class)
			.extracting(ex -> ((MessageException) ex).getResultCode())
			.isEqualTo(PARAM_REQUIRED);
	}

	@Test
	@DisplayName("validate：重复 key 抛出 PARAM_DUPLICATE")
	void validate_duplicateKey_throws() {
		List<MessageTemplateRequireFieldRow> rows = List.of(row("code", JsonSupport.readTree("\"1\"")),
				row("code", JsonSupport.readTree("\"2\"")));

		assertThatThrownBy(() -> MessageTemplateRequireFieldsJsonSupport.validate(rows))
			.isInstanceOf(MessageException.class)
			.extracting(ex -> ((MessageException) ex).getResultCode())
			.isEqualTo(PARAM_DUPLICATE);
	}

	@Test
	@DisplayName("assertVariablesPresent：缺失抛异常，齐全则通过")
	void assertVariablesPresent_missingAndComplete() {
		List<MessageTemplateRequireFieldRow> requireFields = List.of(row("code", JsonSupport.readTree("\"1\"")),
				row("name", JsonSupport.readTree("\"n\"")));
		Map<String, Object> incomplete = Map.of("code", "ok");
		List<MessageTemplateRequireFieldRow> completeFields = List.of(row("code", JsonSupport.readTree("\"1\"")));
		Map<String, Object> complete = Map.of("code", "123");

		// 缺少 name 时应抛出业务异常
		assertThatThrownBy(() -> MessageTemplateRequireFieldsJsonSupport.assertVariablesPresent(requireFields,
				incomplete, PARAM_REQUIRED, "email"))
			.isInstanceOf(MessageException.class)
			.extracting(ex -> ((MessageException) ex).getResultCode())
			.isEqualTo(PARAM_REQUIRED);

		// 变量齐全时不应抛异常
		assertThatCode(() -> MessageTemplateRequireFieldsJsonSupport.assertVariablesPresent(completeFields, complete,
				PARAM_REQUIRED, "email"))
			.doesNotThrowAnyException();
	}

	@Test
	@DisplayName("toExampleModel：用 exampleValue 组装预览模型")
	void toExampleModel_buildsModel() {
		Map<String, Object> model = MessageTemplateRequireFieldsJsonSupport.toExampleModel(
				List.of(row("code", JsonSupport.readTree("\"123456\"")), row("count", JsonSupport.readTree("3"))));

		assertThat(model).containsEntry("code", "123456").containsEntry("count", 3);
	}

}
