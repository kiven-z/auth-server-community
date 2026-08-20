package com.auth.service.system.message.support.template;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.auth.common.core.utils.JsonSupport;
import com.auth.service.system.common.exception.code.SystemResultCode;
import com.auth.service.system.message.exception.MessageException;
import com.auth.service.system.message.model.vo.template.MessageTemplateRequireFieldRow;
import lombok.experimental.UtilityClass;

import java.util.*;

import static com.auth.service.system.common.exception.code.SystemCommonResultCode.*;

/**
 * 消息模板必填变量声明：序列化、解析与发送前完整性校验
 *
 * @author Bunny
 */
@UtilityClass
public class MessageTemplateRequireFieldsJsonSupport {

	/**
	 * 将变量列表序列化为 JSON 字符串（写入数据库；先跑写入级校验）
	 * @param rows 变量列表
	 * @return JSON 字符串
	 */
	public static String toJson(List<MessageTemplateRequireFieldRow> rows) {
		if (rows == null) {
			throw new MessageException(DATA_INVALID, "require_fields");
		}
		validate(rows);
		try {
			return JsonSupport.toJson(rows);
		}
		catch (IllegalArgumentException e) {
			throw new MessageException(DATA_INVALID, e.getMessage());
		}
	}

	/**
	 * 解析必填变量声明 JSON
	 * @param json 必填变量声明 JSON
	 * @return 必填变量声明列表
	 */
	public static List<MessageTemplateRequireFieldRow> parse(String json) {
		if (CharSequenceUtil.isBlank(json)) {
			return Collections.emptyList();
		}
		try {
			MessageTemplateRequireFieldRow[] fieldRows = JsonSupport.fromJson(json,
					MessageTemplateRequireFieldRow[].class);
			return Arrays.asList(fieldRows);
		}
		catch (IllegalArgumentException e) {
			throw new MessageException(DATA_INVALID, e.getMessage());
		}
	}

	/**
	 * 写入级业务校验：key 非空且唯一、exampleValue 非空
	 * @param rows 变量列表
	 */
	public static void validate(List<MessageTemplateRequireFieldRow> rows) {
		if (CollUtil.isEmpty(rows)) {
			return;
		}
		Set<String> keys = new HashSet<>();
		for (MessageTemplateRequireFieldRow row : rows) {
			if (row == null || CharSequenceUtil.isBlank(row.getKey())) {
				throw new MessageException(PARAM_REQUIRED, "Variable key");
			}
			if (row.getExampleValue() == null || row.getExampleValue().isNull()) {
				throw new MessageException(PARAM_REQUIRED, row.getKey() + " exampleValue");
			}
			if (!keys.add(row.getKey())) {
				throw new MessageException(PARAM_DUPLICATE, "Variable key", row.getKey());
			}
		}
	}

	/**
	 * 校验发送变量是否齐全
	 * @param requireFields 已解析的必填变量声明
	 * @param variables 发送变量
	 * @param missingCode 变量缺失时的业务码
	 * @param channel 渠道名（写入 i18n 占位 {0}）
	 */
	public static void assertVariablesPresent(List<MessageTemplateRequireFieldRow> requireFields,
			Map<String, Object> variables, SystemResultCode missingCode, String channel) {
		if (CollUtil.isEmpty(requireFields)) {
			return;
		}
		Map<String, Object> safeVariables = Objects.requireNonNullElse(variables, Map.of());
		List<String> missingKeys = requireFields.stream()
			.map(MessageTemplateRequireFieldRow::getKey)
			.filter(key -> !safeVariables.containsKey(key))
			.toList();
		if (CollUtil.isNotEmpty(missingKeys)) {
			throw new MessageException(missingCode, channel, String.join(", ", missingKeys));
		}
	}

	/**
	 * 用各字段的 exampleValue 组装预览/示例数据模型
	 * @param rows 变量列表
	 * @return 预览/示例数据模型
	 */
	public static Map<String, Object> toExampleModel(List<MessageTemplateRequireFieldRow> rows) {
		if (rows == null) {
			throw new MessageException(DATA_INVALID, "require_fields");
		}
		validate(rows);
		Map<String, Object> model = new LinkedHashMap<>();
		for (MessageTemplateRequireFieldRow row : rows) {
			model.put(row.getKey(), JsonSupport.fromJson(row.getExampleValue().toString(), Object.class));
		}
		return model;
	}

}
