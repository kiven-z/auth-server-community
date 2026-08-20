package com.auth.module.security.autoconfigure.audit;

import com.auth.common.core.model.response.Result;
import com.auth.module.security.autoconfigure.audit.operationlog.OperationLogResponseMessageComposer;
import com.auth.module.security.contract.annotation.OperationLog;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

/**
 * {@link OperationLogResponseMessageComposer} 单元测试
 *
 * @author Bunny
 */
@DisplayName("OperationLogResponseMessageComposer")
@ExtendWith(MockitoExtension.class)
class OperationLogResponseMessageComposerTest {

	@Mock
	private OperationLog operationLogMeta;

	@Test
	@DisplayName("存在异常时应返回异常消息摘要")
	void compose_shouldPreferFailureMessage() {
		// 失败路径优先于 Result 文案
		String msg = OperationLogResponseMessageComposer.compose(null, new Result<>(),
				new IllegalStateException("bad"));
		assertEquals("bad", msg);
	}

	@Test
	@DisplayName("无异常且 meta 为 null 时应返回 null")
	void compose_shouldReturnNullWhenNoFailureAndNoMeta() {
		assertNull(OperationLogResponseMessageComposer.compose(null, new Result<>(), null));
	}

	@Test
	@DisplayName("recordResultMessage 为 false 时不应读取 Result.message")
	void compose_shouldRespectRecordResultMessageOff() {
		when(operationLogMeta.recordResultMessage()).thenReturn(false);
		Result<String> body = new Result<>();
		body.setMessage("ignored");
		assertNull(OperationLogResponseMessageComposer.compose(operationLogMeta, body, null));
	}

	@Test
	@DisplayName("recordResultMessage 为 true 且 Result 含 message 时应返回该文案")
	void compose_shouldTakeResultMessageWhenEnabled() {
		when(operationLogMeta.recordResultMessage()).thenReturn(true);
		Result<String> body = new Result<>();
		body.setMessage("done");
		assertEquals("done", OperationLogResponseMessageComposer.compose(operationLogMeta, body, null));
	}

}
