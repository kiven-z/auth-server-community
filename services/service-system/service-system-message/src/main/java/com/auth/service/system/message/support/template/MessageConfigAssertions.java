package com.auth.service.system.message.support.template;

import cn.hutool.core.text.CharSequenceUtil;
import com.auth.service.system.message.exception.MessageException;
import lombok.experimental.UtilityClass;

import static com.auth.service.system.common.exception.code.SystemCommonResultCode.PARAM_REQUIRED;

/**
 * 消息模块配置断言工具
 *
 * @author Bunny
 */
@UtilityClass
public class MessageConfigAssertions {

	/**
	 * 断言字段不能为空
	 * @param value 字段值
	 * @param fieldName 字段名称
	 */
	public static void assertNotBlank(String value, String fieldName) {
		if (CharSequenceUtil.isBlank(value)) {
			throw new MessageException(PARAM_REQUIRED, fieldName);
		}
	}

	/**
	 * 断言字段不能为 null
	 * @param value 字段值
	 * @param fieldName 字段名称
	 */
	public static void assertNotNull(Object value, String fieldName) {
		if (value == null) {
			throw new MessageException(PARAM_REQUIRED, fieldName);
		}
	}

}
