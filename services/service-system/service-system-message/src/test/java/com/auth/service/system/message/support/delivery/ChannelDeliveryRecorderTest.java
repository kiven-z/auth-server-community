package com.auth.service.system.message.support.delivery;

import com.auth.module.message.api.channel.MessageChannel;
import com.auth.service.system.message.mapper.MessageChannelDeliveryMapper;
import com.auth.service.system.message.model.enums.MessageDeliveryStatus;
import com.auth.service.system.message.model.value.delivery.ChannelDeliveryResultUpdate;
import com.auth.service.system.message.model.value.delivery.TargetSendOutcome;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Method;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * {@link ChannelDeliveryRecorder} 单元测试
 *
 * @author Bunny
 */
@DisplayName("ChannelDeliveryRecorder 渠道投递记录")
@ExtendWith(MockitoExtension.class)
class ChannelDeliveryRecorderTest {

	private static final Long TASK_ID = 1001L;

	private static final Instant SENT_AT = LocalDateTime.of(2026, 7, 18, 12, 0).toInstant(java.time.ZoneOffset.UTC);

	@Mock
	private MessageChannelDeliveryMapper deliveryMapper;

	@Captor
	private ArgumentCaptor<List<ChannelDeliveryResultUpdate>> updateCaptor;

	private ChannelDeliveryRecorder recorder;

	/**
	 * 断言方法声明为独立短事务
	 * @param method 待检查方法
	 */
	private static void assertRequiresNew(Method method) {
		Transactional transactional = method.getAnnotation(Transactional.class);
		assertThat(transactional).isNotNull();
		assertThat(transactional.propagation()).isEqualTo(Propagation.REQUIRES_NEW);
		assertThat(transactional.rollbackFor()).contains(Exception.class);
	}

	@BeforeEach
	void setUp() {
		recorder = new ChannelDeliveryRecorder();
		ReflectionTestUtils.setField(recorder, "baseMapper", deliveryMapper);
	}

	@Test
	@DisplayName("写库方法使用 REQUIRES_NEW，避免外层事务回滚抹掉外部发送回执")
	void writeMethods_shouldUseRequiresNewPropagation() throws NoSuchMethodException {
		// 锁定事务边界：pending / outcomes / failed 均独立短事务提交
		assertRequiresNew(ChannelDeliveryRecorder.class.getMethod("recordPending", Long.class, MessageChannel.class,
				Collection.class));
		assertRequiresNew(ChannelDeliveryRecorder.class.getMethod("recordOutcomes", Long.class, MessageChannel.class,
				List.class));
		assertRequiresNew(ChannelDeliveryRecorder.class.getMethod("recordFailed", Long.class, MessageChannel.class,
				Collection.class, String.class, String.class, Instant.class));
	}

	@Test
	@DisplayName("recordOutcomes：空参数不调用 Mapper")
	void recordOutcomes_shouldSkipWhenInvalidParams() {
		recorder.recordOutcomes(null, MessageChannel.EMAIL, List.of());
		recorder.recordOutcomes(TASK_ID, null, List.of());
		recorder.recordOutcomes(TASK_ID, MessageChannel.EMAIL, null);

		verify(deliveryMapper, never()).batchUpdateResult(any(), any(), anyList());
	}

	@Test
	@DisplayName("recordOutcomes：过滤空目标后批量回写混合成败结果")
	void recordOutcomes_shouldBatchUpdateMixedOutcomes() {
		when(deliveryMapper.batchUpdateResult(eq(TASK_ID), eq(MessageChannel.EMAIL.name()), anyList())).thenReturn(2);

		List<TargetSendOutcome> outcomes = new ArrayList<>();
		outcomes.add(TargetSendOutcome.success("a@example.com", "provider-1", SENT_AT));
		outcomes.add(TargetSendOutcome.failure("b@example.com", "SMTP_ERROR", "send failed", SENT_AT));
		outcomes.add(null);
		outcomes.add(TargetSendOutcome.success(" ", "ignored", SENT_AT));

		recorder.recordOutcomes(TASK_ID, MessageChannel.EMAIL, outcomes);

		verify(deliveryMapper).batchUpdateResult(eq(TASK_ID), eq(MessageChannel.EMAIL.name()), updateCaptor.capture());
		List<ChannelDeliveryResultUpdate> updates = updateCaptor.getValue();
		assertThat(updates).hasSize(2);

		ChannelDeliveryResultUpdate successUpdate = updates.get(0);
		assertThat(successUpdate.getTargetValue()).isEqualTo("a@example.com");
		assertThat(successUpdate.getStatus()).isEqualTo(MessageDeliveryStatus.SUCCESS.name());
		assertThat(successUpdate.getProviderMsgId()).isEqualTo("provider-1");
		assertThat(successUpdate.getErrorCode()).isNull();
		assertThat(successUpdate.getErrorMessage()).isNull();
		assertThat(successUpdate.getSentAt()).isEqualTo(SENT_AT);

		ChannelDeliveryResultUpdate failedUpdate = updates.get(1);
		assertThat(failedUpdate.getTargetValue()).isEqualTo("b@example.com");
		assertThat(failedUpdate.getStatus()).isEqualTo(MessageDeliveryStatus.FAILED.name());
		assertThat(failedUpdate.getProviderMsgId()).isNull();
		assertThat(failedUpdate.getErrorCode()).isEqualTo("SMTP_ERROR");
		assertThat(failedUpdate.getErrorMessage()).isEqualTo("send failed");
		assertThat(failedUpdate.getSentAt()).isEqualTo(SENT_AT);
	}

	@Test
	@DisplayName("recordOutcomes：超过批次大小时分片调用 Mapper")
	void recordOutcomes_shouldSplitWhenExceedingBatchSize() {
		when(deliveryMapper.batchUpdateResult(eq(TASK_ID), eq(MessageChannel.SMS.name()), anyList())).thenReturn(500);

		List<TargetSendOutcome> outcomes = new ArrayList<>();
		IntStream.range(0, 501)
			.forEach(index -> outcomes
				.add(TargetSendOutcome.success("1380013" + String.format("%04d", index), "msg-" + index, SENT_AT)));

		recorder.recordOutcomes(TASK_ID, MessageChannel.SMS, outcomes);

		verify(deliveryMapper, times(2)).batchUpdateResult(eq(TASK_ID), eq(MessageChannel.SMS.name()),
				updateCaptor.capture());
		List<List<ChannelDeliveryResultUpdate>> chunks = updateCaptor.getAllValues();
		assertThat(chunks.get(0)).hasSize(500);
		assertThat(chunks.get(1)).hasSize(1);
	}

	@Test
	@DisplayName("recordFailed：整批目标写入相同失败信息")
	void recordFailed_shouldBatchUpdateSameFailureForAllTargets() {
		when(deliveryMapper.batchUpdateResult(eq(TASK_ID), eq(MessageChannel.EMAIL.name()), anyList())).thenReturn(2);

		recorder.recordFailed(TASK_ID, MessageChannel.EMAIL, List.of("a@example.com", "b@example.com"), "SMTP_ERROR",
				"send failed", SENT_AT);

		verify(deliveryMapper).batchUpdateResult(eq(TASK_ID), eq(MessageChannel.EMAIL.name()), updateCaptor.capture());
		List<ChannelDeliveryResultUpdate> updates = updateCaptor.getValue();
		assertThat(updates).hasSize(2).allSatisfy(update -> {
			assertThat(update.getStatus()).isEqualTo(MessageDeliveryStatus.FAILED.name());
			assertThat(update.getErrorCode()).isEqualTo("SMTP_ERROR");
			assertThat(update.getErrorMessage()).isEqualTo("send failed");
			assertThat(update.getSentAt()).isEqualTo(SENT_AT);
		});
	}

}
