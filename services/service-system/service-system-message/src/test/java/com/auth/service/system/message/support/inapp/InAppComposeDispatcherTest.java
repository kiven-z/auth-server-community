package com.auth.service.system.message.support.inapp;

import com.auth.common.core.constants.BatchSizes;
import com.auth.service.system.message.exception.MessageException;
import com.auth.service.system.message.mapper.InAppMessageMapper;
import com.auth.service.system.message.model.entity.InAppMessageEntity;
import com.auth.service.system.message.model.entity.InAppMessageRecipientEntity;
import com.auth.service.system.message.model.enums.InAppMessageStatus;
import com.auth.service.system.message.model.enums.RecipientScopeType;
import com.auth.service.system.message.model.value.recipient.RecipientScope;
import com.auth.service.system.message.service.admin.InAppMessageRecipientWriteService;
import com.auth.service.system.message.support.recipient.RecipientBatchScanner;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.function.Consumer;

import static com.auth.service.system.message.exception.MessageResultCode.IN_APP_SEND_TASK_NOT_FOUND;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * {@link InAppComposeDispatcher} 单元测试
 *
 * @author Bunny
 */
@DisplayName("InAppComposeDispatcher 群发任务执行")
@ExtendWith(MockitoExtension.class)
class InAppComposeDispatcherTest {

	@Mock
	private InAppMessageMapper inAppMessageMapper;

	@Mock
	private RecipientBatchScanner recipientBatchScanner;

	@Mock
	private InAppMessageRecipientWriteService inAppMessageRecipientWriteService;

	private InAppComposeDispatcher dispatcher;

	/**
	 * 构造待派发写扩散任务快照
	 * @param id 任务 ID
	 * @return 任务实体
	 */
	private static InAppMessageEntity pendingTask(Long id) {
		InAppMessageEntity task = new InAppMessageEntity();
		task.setId(id);
		task.setRecipientScopeType(RecipientScopeType.DEPT.name());
		task.setRecipientScopeJson("{\"ids\":[1],\"includeChildren\":true}");
		task.setRecipientScopeType(RecipientScopeType.USER.name());
		task.setStatus(InAppMessageStatus.PENDING.name());
		return task;
	}

	/**
	 * CAS 抢占成功（PENDING → SENDING）
	 * @param taskId 任务 ID
	 */
	private void stubClaimSuccess(Long taskId) {
		when(inAppMessageMapper.updateStatusCas(taskId, InAppMessageStatus.PENDING.name(),
				InAppMessageStatus.SENDING.name()))
			.thenReturn(1);
	}

	@BeforeEach
	void setUp() {
		dispatcher = new InAppComposeDispatcher(inAppMessageMapper, recipientBatchScanner,
				inAppMessageRecipientWriteService);
	}

	@Test
	@DisplayName("抢占失败：不执行扫描与写入")
	void execute_shouldSkipWhenMarkSendingFails() {
		when(inAppMessageMapper.updateStatusCas(1L, InAppMessageStatus.PENDING.name(),
				InAppMessageStatus.SENDING.name()))
			.thenReturn(0);

		dispatcher.execute(1L);

		verify(inAppMessageMapper, never()).selectById(any());
		verify(recipientBatchScanner, never()).scan(any(), anyInt(), any());
		verify(inAppMessageRecipientWriteService, never()).insertBatch(any());
	}

	@Test
	@DisplayName("任务不存在：抛任务不存在")
	void execute_shouldThrowWhenTaskMissing() {
		stubClaimSuccess(2L);
		when(inAppMessageMapper.selectById(2L)).thenReturn(null);

		assertThatThrownBy(() -> dispatcher.execute(2L)).isInstanceOf(MessageException.class)
			.extracting(ex -> ((MessageException) ex).getResultCode())
			.isEqualTo(IN_APP_SEND_TASK_NOT_FOUND);

		verify(recipientBatchScanner, never()).scan(any(), anyInt(), any());
	}

	@Test
	@DisplayName("成功：分批写入 inbox，终态以收件箱 COUNT 为准，中间不做进度累加")
	void execute_shouldDeliverBatchesAndFinishSuccess() {
		stubClaimSuccess(10L);
		when(inAppMessageMapper.selectById(10L)).thenReturn(pendingTask(10L));
		doAnswer(invocation -> {
			Consumer<List<Long>> consumer = invocation.getArgument(2);
			consumer.accept(List.of(1001L, 1002L));
			consumer.accept(List.of(1003L));
			return null;
		}).when(recipientBatchScanner).scan(any(RecipientScope.class), eq(BatchSizes.SIZE_1000), any());
		when(inAppMessageRecipientWriteService.insertBatch(any())).thenAnswer(invocation -> {
			List<?> rows = invocation.getArgument(0);
			return rows.size();
		});
		when(inAppMessageRecipientWriteService.countByMessageId(10L)).thenReturn(3);

		dispatcher.execute(10L);

		@SuppressWarnings("unchecked")
		ArgumentCaptor<List<InAppMessageRecipientEntity>> rowsCaptor = ArgumentCaptor.forClass(List.class);
		verify(inAppMessageRecipientWriteService, times(2)).insertBatch(rowsCaptor.capture());
		assertThat(rowsCaptor.getAllValues().get(0)).extracting(InAppMessageRecipientEntity::getUserId)
			.containsExactly(1001L, 1002L);
		assertThat(rowsCaptor.getAllValues().get(1)).extracting(InAppMessageRecipientEntity::getUserId)
			.containsExactly(1003L);
		assertThat(rowsCaptor.getAllValues().get(0).get(0).getMessageId()).isEqualTo(10L);
		assertThat(rowsCaptor.getAllValues().get(0).get(0).getIsRead()).isFalse();
		assertThat(rowsCaptor.getAllValues().get(0).get(0).getIsDeleted()).isFalse();

		verify(inAppMessageMapper).finishTask(10L, InAppMessageStatus.SUCCESS.name(), 3);
	}

	@Test
	@DisplayName("补发幂等：本批全被 IGNORE 时终态仍按已投递 COUNT 标 SUCCESS")
	void execute_shouldFinishSuccessWhenAllRowsIgnoredButInboxHasRows() {
		stubClaimSuccess(14L);
		when(inAppMessageMapper.selectById(14L)).thenReturn(pendingTask(14L));
		doAnswer(invocation -> {
			Consumer<List<Long>> consumer = invocation.getArgument(2);
			consumer.accept(List.of(1L, 2L));
			return null;
		}).when(recipientBatchScanner).scan(any(), anyInt(), any());
		when(inAppMessageRecipientWriteService.insertBatch(any())).thenReturn(0);
		when(inAppMessageRecipientWriteService.countByMessageId(14L)).thenReturn(2);

		dispatcher.execute(14L);

		verify(inAppMessageMapper).finishTask(14L, InAppMessageStatus.SUCCESS.name(), 2);
	}

	@Test
	@DisplayName("无接收人：标记 NO_RECIPIENTS 且不抛异常（业务终态，非失败）")
	void execute_shouldMarkNoRecipientsWhenScopeYieldsZero() {
		stubClaimSuccess(11L);
		when(inAppMessageMapper.selectById(11L)).thenReturn(pendingTask(11L));
		doAnswer(invocation -> null).when(recipientBatchScanner).scan(any(), anyInt(), any());
		when(inAppMessageRecipientWriteService.countByMessageId(11L)).thenReturn(0);

		dispatcher.execute(11L);

		verify(inAppMessageRecipientWriteService, never()).insertBatch(any());
		verify(inAppMessageMapper).finishTask(11L, InAppMessageStatus.NO_RECIPIENTS.name(), 0);
	}

	@Test
	@DisplayName("中途失败且收件箱已有投递：标记 PARTIAL")
	void execute_shouldMarkPartialWhenBatchFailsAfterSuccess() {
		stubClaimSuccess(12L);
		when(inAppMessageMapper.selectById(12L)).thenReturn(pendingTask(12L));
		doAnswer(invocation -> {
			Consumer<List<Long>> consumer = invocation.getArgument(2);
			consumer.accept(List.of(1L));
			consumer.accept(List.of(2L));
			return null;
		}).when(recipientBatchScanner).scan(any(), anyInt(), any());
		when(inAppMessageRecipientWriteService.insertBatch(any())).thenReturn(1)
			.thenThrow(new RuntimeException("db down"));
		when(inAppMessageRecipientWriteService.countByMessageId(12L)).thenReturn(1);

		assertThatThrownBy(() -> dispatcher.execute(12L)).isInstanceOf(RuntimeException.class).hasMessage("db down");

		verify(inAppMessageMapper).finishTask(12L, InAppMessageStatus.PARTIAL.name(), 1);
	}

	@Test
	@DisplayName("还原 DEPT 范围并传入 Scanner")
	void execute_shouldRestoreDeptScope() {
		stubClaimSuccess(13L);
		InAppMessageEntity task = pendingTask(13L);
		task.setRecipientScopeType(RecipientScopeType.DEPT.name());
		task.setRecipientScopeJson("{\"ids\":[3],\"includeChildren\":false}");
		when(inAppMessageMapper.selectById(13L)).thenReturn(task);
		doAnswer(invocation -> {
			Consumer<List<Long>> consumer = invocation.getArgument(2);
			consumer.accept(List.of(9L));
			return null;
		}).when(recipientBatchScanner).scan(any(), anyInt(), any());
		when(inAppMessageRecipientWriteService.insertBatch(any())).thenReturn(1);
		when(inAppMessageRecipientWriteService.countByMessageId(13L)).thenReturn(1);

		dispatcher.execute(13L);

		ArgumentCaptor<RecipientScope> scopeCaptor = ArgumentCaptor.forClass(RecipientScope.class);
		verify(recipientBatchScanner).scan(scopeCaptor.capture(), eq(BatchSizes.SIZE_1000), any());
		assertThat(scopeCaptor.getValue().getType()).isEqualTo(RecipientScopeType.DEPT);
		assertThat(scopeCaptor.getValue().safeIds()).containsExactly(3L);
		assertThat(scopeCaptor.getValue().includeChildrenOrDefault()).isFalse();
	}

}
