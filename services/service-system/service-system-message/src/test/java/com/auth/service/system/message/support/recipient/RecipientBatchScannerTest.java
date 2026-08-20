package com.auth.service.system.message.support.recipient;

import com.auth.service.system.message.exception.MessageException;
import com.auth.service.system.message.mapper.RecipientUserMapper;
import com.auth.service.system.message.model.enums.RecipientScopeType;
import com.auth.service.system.message.model.value.recipient.RecipientScope;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.LongStream;

import static com.auth.service.system.message.exception.MessageResultCode.IN_APP_RECIPIENT_SCOPE_INVALID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * {@link RecipientBatchScanner} 单元测试
 *
 * @author Bunny
 */
@DisplayName("RecipientBatchScanner 接收人分批扫描")
@ExtendWith(MockitoExtension.class)
class RecipientBatchScannerTest {

	@Mock
	private RecipientUserMapper recipientUserMapper;

	@InjectMocks
	private RecipientBatchScanner scanner;

	@Test
	@DisplayName("USER：按候选 ID 游标过滤")
	void scan_user_shouldQueryEnabledAfter() {
		when(recipientUserMapper.selectEnabledUserIdsAfter(List.of(1L, 2L), 0L, 100)).thenReturn(List.of(1L));
		when(recipientUserMapper.selectEnabledUserIdsAfter(List.of(1L, 2L), 1L, 100)).thenReturn(List.of());

		List<Long> collected = new ArrayList<>();
		scanner.scan(RecipientScope.builder().type(RecipientScopeType.USER).ids(List.of(1L, 2L)).build(), 100,
				collected::addAll);

		assertThat(collected).containsExactly(1L);
	}

	@Test
	@DisplayName("USER：多批游标扫描")
	void scan_user_shouldConsumeMultipleBatches() {
		when(recipientUserMapper.selectEnabledUserIdsAfter(List.of(1L, 2L, 3L), 0L, 2)).thenReturn(List.of(1L, 2L));
		when(recipientUserMapper.selectEnabledUserIdsAfter(List.of(1L, 2L, 3L), 2L, 2)).thenReturn(List.of(3L));
		when(recipientUserMapper.selectEnabledUserIdsAfter(List.of(1L, 2L, 3L), 3L, 2)).thenReturn(List.of());

		List<List<Long>> batches = new ArrayList<>();
		scanner.scan(RecipientScope.builder().type(RecipientScopeType.USER).ids(List.of(1L, 2L, 3L)).build(), 2,
				batches::add);

		assertThat(batches).containsExactly(List.of(1L, 2L), List.of(3L));
	}

	@Test
	@DisplayName("POST：按岗位游标展开")
	void scan_post_shouldQueryByPostIdsAfter() {
		when(recipientUserMapper.selectUserIdsByPostIdsAfter(List.of(10L), 0L, 50)).thenReturn(List.of(100L, 101L));
		when(recipientUserMapper.selectUserIdsByPostIdsAfter(List.of(10L), 101L, 50)).thenReturn(List.of());

		List<Long> collected = new ArrayList<>();
		scanner.scan(RecipientScope.builder().type(RecipientScopeType.POST).ids(List.of(10L)).build(), 50,
				collected::addAll);

		assertThat(collected).containsExactly(100L, 101L);
	}

	@Test
	@DisplayName("DEPT：默认含子部门游标")
	void scan_dept_shouldIncludeChildrenByDefault() {
		when(recipientUserMapper.selectUserIdsByDeptIdsWithChildrenAfter(List.of(3L), 0L, 50))
			.thenReturn(List.of(200L));
		when(recipientUserMapper.selectUserIdsByDeptIdsWithChildrenAfter(List.of(3L), 200L, 50)).thenReturn(List.of());

		List<Long> collected = new ArrayList<>();
		scanner.scan(RecipientScope.builder().type(RecipientScopeType.DEPT).ids(List.of(3L)).build(), 50,
				collected::addAll);

		assertThat(collected).containsExactly(200L);
		verify(recipientUserMapper, never()).selectUserIdsByDeptIdsAfter(anyCollection(), anyLong(), anyInt());
	}

	@Test
	@DisplayName("DEPT：includeChildren=false 仅当前部门")
	void scan_dept_shouldExcludeChildrenWhenDisabled() {
		when(recipientUserMapper.selectUserIdsByDeptIdsAfter(List.of(3L), 0L, 50)).thenReturn(List.of(201L));
		when(recipientUserMapper.selectUserIdsByDeptIdsAfter(List.of(3L), 201L, 50)).thenReturn(List.of());

		List<Long> collected = new ArrayList<>();
		scanner.scan(
				RecipientScope.builder().type(RecipientScopeType.DEPT).ids(List.of(3L)).includeChildren(false).build(),
				50, collected::addAll);

		assertThat(collected).containsExactly(201L);
		verify(recipientUserMapper, never()).selectUserIdsByDeptIdsWithChildrenAfter(anyCollection(), anyLong(),
				anyInt());
	}

	@Test
	@DisplayName("USER ids 为空：抛范围非法")
	void scan_user_shouldRejectEmptyIds() {
		RecipientScope scope = RecipientScope.builder().type(RecipientScopeType.USER).build();
		assertThatThrownBy(() -> scanner.scan(scope, 10, batch -> {
		})).isInstanceOf(MessageException.class)
			.extracting(ex -> ((MessageException) ex).getResultCode())
			.isEqualTo(IN_APP_RECIPIENT_SCOPE_INVALID);
		verify(recipientUserMapper, never()).selectEnabledUserIdsAfter(anyCollection(), anyLong(), anyInt());
	}

	@Test
	@DisplayName("scope 为空：抛范围非法")
	void scan_shouldRejectNullScope() {
		assertThatThrownBy(() -> scanner.scan(null, 10, batch -> {
		})).isInstanceOf(MessageException.class)
			.extracting(ex -> ((MessageException) ex).getResultCode())
			.isEqualTo(IN_APP_RECIPIENT_SCOPE_INVALID);
	}

	@Test
	@DisplayName("首批为空：consumer 不被调用")
	void scan_emptyFirstBatch_shouldNotInvokeConsumer() {
		when(recipientUserMapper.selectEnabledUserIdsAfter(List.of(1L), 0L, 10)).thenReturn(List.of());

		AtomicInteger calls = new AtomicInteger();
		scanner.scan(RecipientScope.builder().type(RecipientScopeType.USER).ids(List.of(1L)).build(), 10,
				batch -> calls.incrementAndGet());

		assertThat(calls.get()).isZero();
	}

	@Test
	@DisplayName("2500 用户按 1000 分批：共 3 批且游标递增")
	void scan_user_shouldFetchInThreeBatchesWithAdvancingCursor() {
		// 模拟 2500 个启用用户，验证游标按 lastUserId 推进
		List<Long> candidateIds = LongStream.rangeClosed(1, 2500).boxed().toList();
		when(recipientUserMapper.selectEnabledUserIdsAfter(candidateIds, 0L, 1000))
			.thenReturn(candidateIds.subList(0, 1000));
		when(recipientUserMapper.selectEnabledUserIdsAfter(candidateIds, 1000L, 1000))
			.thenReturn(candidateIds.subList(1000, 2000));
		when(recipientUserMapper.selectEnabledUserIdsAfter(candidateIds, 2000L, 1000))
			.thenReturn(candidateIds.subList(2000, 2500));
		when(recipientUserMapper.selectEnabledUserIdsAfter(candidateIds, 2500L, 1000)).thenReturn(List.of());

		List<Long> collected = new ArrayList<>();
		scanner.scan(RecipientScope.builder().type(RecipientScopeType.USER).ids(candidateIds).build(), 1000,
				collected::addAll);

		assertThat(collected).hasSize(2500);
		assertThat(collected.get(0)).isEqualTo(1L);
		assertThat(collected.get(2499)).isEqualTo(2500L);
		verify(recipientUserMapper).selectEnabledUserIdsAfter(candidateIds, 0L, 1000);
		verify(recipientUserMapper).selectEnabledUserIdsAfter(candidateIds, 1000L, 1000);
		verify(recipientUserMapper).selectEnabledUserIdsAfter(candidateIds, 2000L, 1000);
		verify(recipientUserMapper).selectEnabledUserIdsAfter(candidateIds, 2500L, 1000);
	}

	@Test
	@DisplayName("batchSize 非法：抛 IllegalArgumentException")
	void scan_shouldRejectNonPositiveBatchSize() {
		RecipientScope scope = RecipientScope.builder().type(RecipientScopeType.USER).ids(List.of(1L)).build();
		assertThatThrownBy(() -> scanner.scan(scope, 0, batch -> {
		})).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("batchSize must be positive");
	}

}
