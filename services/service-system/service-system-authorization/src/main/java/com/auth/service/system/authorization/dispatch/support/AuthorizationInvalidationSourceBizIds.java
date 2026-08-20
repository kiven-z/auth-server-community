package com.auth.service.system.authorization.dispatch.support;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.IdUtil;
import lombok.experimental.UtilityClass;

/**
 * Outbox source_biz_id：追踪号，完整业务键在 payload
 * <p>
 * 格式：operation:xxxxxxxx（8 位短 UUID）
 *
 * @author Bunny
 */
@UtilityClass
public final class AuthorizationInvalidationSourceBizIds {

	private static final int SHORT_UUID_LENGTH = 8;

	/**
	 * 生成带操作前缀的短追踪号
	 * @param operation 操作动词，空白时回退为 invalidate
	 * @return 如 update:a3f2c1b0
	 */
	public static String of(String operation) {
		String op = CharSequenceUtil.blankToDefault(operation, "invalidate");
		return op + ":" + IdUtil.fastSimpleUUID().substring(0, SHORT_UUID_LENGTH);
	}

}
