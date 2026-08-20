package com.auth.service.auth.util;

import cn.hutool.core.collection.CollUtil;
import lombok.experimental.UtilityClass;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * 集合分批切片工具，用于规避 SQL IN 子句过长。
 *
 * @author Bunny
 */
@UtilityClass
public class BatchPartition {

	/**
	 * 过滤 null、去重后按固定批次大小切片。
	 * @param ids 待处理 ID 集合
	 * @param batchSize 每批大小，须 &gt; 0
	 * @return 分批结果；batchSize <= 0 或集合为空时返回空列表
	 */
	public static List<List<Long>> partitionIds(Collection<Long> ids, int batchSize) {
		if (CollUtil.isEmpty(ids)) {
			return List.of();
		}

		// 过滤 null、去重后按固定批次大小切片
		List<Long> list = ids.stream().filter(Objects::nonNull).distinct().toList();
		return partition(list, batchSize);
	}

	/**
	 * 过滤 blank、去重后按固定批次大小切片。
	 * @param values 待处理字符串集合
	 * @param batchSize 每批大小，须 &gt; 0
	 * @return 分批结果；batchSize <= 0 或集合为空时返回空列表
	 */
	public static List<List<String>> partitionStrings(Collection<String> values, int batchSize) {
		// 如果集合为空，则返回空列表
		if (CollUtil.isEmpty(values)) {
			return List.of();
		}

		// 过滤 null、去重后按固定批次大小切片
		List<String> normalized = values.stream()
			.filter(Objects::nonNull)
			.map(String::trim)
			.filter(value -> !value.isEmpty())
			.distinct()
			.toList();
		return partition(normalized, batchSize);
	}

	/**
	 * 按固定批次大小切片。
	 * @param normalized 待处理集合
	 * @param batchSize 每批大小，须 &gt; 0
	 * @return 分批结果；batchSize <= 0 或集合为空时返回空列表
	 */
	private static <T> List<List<T>> partition(List<T> normalized, int batchSize) {
		if (batchSize <= 0 || CollUtil.isEmpty(normalized)) {
			return List.of();
		}

		List<List<T>> batches = new ArrayList<>();
		for (int index = 0; index < normalized.size(); index += batchSize) {
			int end = Math.min(index + batchSize, normalized.size());
			batches.add(normalized.subList(index, end));
		}
		return batches;
	}

}
