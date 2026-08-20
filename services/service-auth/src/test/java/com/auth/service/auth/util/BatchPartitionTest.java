package com.auth.service.auth.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link BatchPartition} 单元测试
 */
@DisplayName("BatchPartition 集合分片")
class BatchPartitionTest {

	@Test
	@DisplayName("Long ID 应按批次切片")
	void partitionIds_shouldSplitByBatchSize() {
		List<Long> ids = IntStream.rangeClosed(1, 5).mapToObj(Long::valueOf).toList();

		List<List<Long>> batches = BatchPartition.partitionIds(ids, 2);

		assertEquals(3, batches.size());
		assertEquals(List.of(1L, 2L), batches.get(0));
		assertEquals(List.of(3L, 4L), batches.get(1));
		assertEquals(List.of(5L), batches.get(2));
	}

	@Test
	@DisplayName("String 应过滤 blank 后分片")
	void partitionStrings_shouldTrimAndSplit() {
		List<String> values = Arrays.asList("A", " A ", "", "B", "B", null);

		List<List<String>> batches = BatchPartition.partitionStrings(values, 2);

		assertEquals(1, batches.size());
		assertEquals(List.of("A", "B"), batches.get(0));
	}

	@Test
	@DisplayName("非法批次大小返回空列表")
	void partitionIds_invalidBatchSize_shouldReturnEmpty() {
		assertTrue(BatchPartition.partitionIds(List.of(1L), 0).isEmpty());
		assertTrue(BatchPartition.partitionStrings(List.of("A"), -1).isEmpty());
	}

}
