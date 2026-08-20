package com.auth.service.system.message.support.inapp;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * {@link InAppComposeDispatchTrigger} 单元测试
 *
 * @author Bunny
 */
@DisplayName("InAppComposeDispatchTrigger 派发触发")
@ExtendWith(MockitoExtension.class)
class InAppComposeDispatchTriggerTest {

	@Mock
	private InAppComposeDispatcher dispatcher;

	private AtomicBoolean submittedToExecutor;

	private InAppComposeDispatchTrigger trigger;

	@BeforeEach
	void setUp() {
		submittedToExecutor = new AtomicBoolean(false);
		Executor executor = runnable -> {
			submittedToExecutor.set(true);
			runnable.run();
		};
		trigger = new InAppComposeDispatchTrigger(dispatcher, executor);
	}

	@Test
	@DisplayName("无事务：提交到线程池并执行")
	void dispatchAfterCommit_shouldSubmitToExecutor() {
		// 无事务时应立即提交线程池
		trigger.dispatchAfterCommit(9L);

		assertThat(submittedToExecutor).isTrue();
		verify(dispatcher).execute(9L);
	}

	@Test
	@DisplayName("执行失败：吞掉异常不向外抛出")
	void dispatchAfterCommit_shouldSwallowDispatchException() {
		// 派发失败只记日志，避免影响提交后回调
		org.mockito.Mockito.doThrow(new IllegalStateException("boom")).when(dispatcher).execute(9L);

		trigger.dispatchAfterCommit(9L);

		verify(dispatcher).execute(9L);
		verify(dispatcher, never()).execute(8L);
	}

}
