package com.auth.module.security.autoconfigure.audit.operationlog;

import cn.hutool.core.text.CharSequenceUtil;
import com.auth.common.core.model.response.Result;
import com.auth.module.security.contract.annotation.OperationLog;
import lombok.experimental.UtilityClass;

/**
 * 根据注解策略与调用结果生成操作日志中的响应消息摘要。
 *
 * @author Bunny
 */
@UtilityClass
public class OperationLogResponseMessageComposer {

	private static final int MAX_RESPONSE_MESSAGE = 255;

	/**
	 * @param meta 操作日志注解；可为 null（仅影响是否读取 Result 文案）
	 * @param result 控制器返回值
	 * @param failure 未捕获异常，非空表示失败
	 * @return 写入 response_message 的摘要，可为 null
	 */
	public static String compose(OperationLog meta, Object result, Throwable failure) {
		if (failure != null) {
			return CharSequenceUtil.subPre(failure.getMessage(), MAX_RESPONSE_MESSAGE);
		}
		if (meta == null || !meta.recordResultMessage()) {
			return null;
		}
		if (result instanceof Result<?> message && CharSequenceUtil.isNotBlank(message.getMessage())) {
			return CharSequenceUtil.subPre(message.getMessage(), MAX_RESPONSE_MESSAGE);
		}
		return null;
	}

}
