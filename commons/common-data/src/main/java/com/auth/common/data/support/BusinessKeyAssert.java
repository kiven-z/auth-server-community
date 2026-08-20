package com.auth.common.data.support;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import lombok.experimental.UtilityClass;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * 业务键（编码等）在单次请求内及库内的唯一性断言
 *
 * @author Bunny
 */
@UtilityClass
public class BusinessKeyAssert {

	/**
	 * 唯一性冲突时由调用方提供的异常工厂抛出运行时异常
	 * @param mapper 数据访问 Mapper
	 * @param uniquenessWrapper 唯一性查询条件
	 * @param onDuplicate 存在冲突记录时的异常供应器
	 * @param <T> 实体类型
	 */
	public static <T> void requireAbsent(BaseMapper<T> mapper, Wrapper<T> uniquenessWrapper,
			Supplier<? extends RuntimeException> onDuplicate) {
		if (mapper.selectCount(uniquenessWrapper) > 0) {
			throw onDuplicate.get();
		}
	}

	/**
	 * 同一请求批次内业务键不得重复；遇首个重复项 fail-fast 抛出调用方异常。
	 * @param values 待校验业务键列表
	 * @param onDuplicate 重复键及对应异常工厂
	 * @param <K> 业务键类型
	 * @return 去重后的不可变列表，供后续 IN 查询
	 */
	public static <K> List<K> requireDistinct(List<K> values, Function<K, ? extends RuntimeException> onDuplicate) {
		if (values == null || values.isEmpty()) {
			return List.of();
		}
		Set<K> seen = new HashSet<>();
		for (K value : values) {
			if (!seen.add(value)) {
				throw onDuplicate.apply(value);
			}
		}
		return List.copyOf(seen);
	}

	/**
	 * 按提取出的业务键校验同一请求批次内不得重复；遇首个重复项 fail-fast 抛出调用方异常。
	 * @param values 待校验条目
	 * @param keyExtractor 业务键提取函数
	 * @param onDuplicate 重复条目及对应异常工厂
	 * @param <T> 条目类型
	 * @param <K> 业务键类型
	 * @return 去重后的不可变业务键列表，供后续 IN 查询
	 */
	public static <T, K> List<K> requireDistinctBy(List<T> values, Function<T, K> keyExtractor,
			Function<T, ? extends RuntimeException> onDuplicate) {
		if (values == null || values.isEmpty()) {
			return List.of();
		}
		Set<K> seen = new HashSet<>();
		for (T value : values) {
			K key = keyExtractor.apply(value);
			if (!seen.add(key)) {
				throw onDuplicate.apply(value);
			}
		}
		return List.copyOf(seen);
	}

}
