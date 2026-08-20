package com.auth.common.data.support;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link BusinessKeyAssert} 单元测试。
 *
 * @author Bunny
 */
@DisplayName("BusinessKeyAssert 业务键唯一性断言")
@ExtendWith(MockitoExtension.class)
class BusinessKeyAssertTest {

	@Mock
	private BaseMapper<TestEntity> entityMapper;

	@Test
	@DisplayName("requireAbsent：无冲突记录时不抛异常")
	void requireAbsentPassesWhenCountZero() {
		when(entityMapper.selectCount(any())).thenReturn(0L);

		BusinessKeyAssert.requireAbsent(entityMapper,
				Wrappers.<TestEntity>lambdaQuery().eq(TestEntity::getCode, "ADMIN"),
				() -> new IllegalStateException("duplicate"));

		verify(entityMapper).selectCount(any());
	}

	@Test
	@DisplayName("requireAbsent：存在冲突记录时抛出调用方提供的异常")
	void requireAbsentThrowsWhenCountPositive() {
		when(entityMapper.selectCount(any())).thenReturn(1L);
		var duplicateQuery = Wrappers.<TestEntity>lambdaQuery().eq(TestEntity::getCode, "ADMIN");

		assertThatThrownBy(() -> BusinessKeyAssert.requireAbsent(entityMapper, duplicateQuery,
				() -> new IllegalStateException("duplicate")))
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("duplicate");
	}

	@Test
	@DisplayName("requireDistinct：空列表返回空结果")
	void requireDistinctReturnsEmptyWhenInputEmpty() {
		assertThat(BusinessKeyAssert.<String>requireDistinct(List.of(), IllegalStateException::new)).isEmpty();
		assertThat(BusinessKeyAssert.<String>requireDistinct(null, IllegalStateException::new)).isEmpty();
	}

	@Test
	@DisplayName("requireDistinct：无重复时返回去重列表")
	void requireDistinctReturnsDistinctValues() {
		List<String> distinct = BusinessKeyAssert.requireDistinct(List.of("a", "b", "c"), IllegalStateException::new);

		assertThat(distinct).containsExactlyInAnyOrder("a", "b", "c");
	}

	@Test
	@DisplayName("requireDistinct：请求内重复时 fail-fast 抛出调用方异常")
	void requireDistinctThrowsOnFirstDuplicate() {
		ThrowingCallable executable = () -> BusinessKeyAssert.requireDistinct(List.of("a", "b", "a"),
				key -> new IllegalStateException("dup:" + key));
		assertThatThrownBy(executable).isInstanceOf(IllegalStateException.class).hasMessage("dup:a");
	}

	@Test
	@DisplayName("requireDistinctBy：按提取键校验重复并返回去重键列表")
	void requireDistinctByReturnsDistinctKeys() {
		record Item(String code, int seq) {
		}

		List<String> distinct = BusinessKeyAssert.requireDistinctBy(List.of(new Item("x", 1), new Item("y", 2)),
				Item::code, item -> new IllegalStateException(item.code()));

		assertThat(distinct).containsExactlyInAnyOrder("x", "y");
	}

	@Test
	@DisplayName("requireDistinctBy：重复条目时抛出调用方异常")
	void requireDistinctByThrowsOnDuplicateKey() {
		record Item(String code) {
		}
		Item duplicate = new Item("dup");

		ThrowingCallable executable = () -> BusinessKeyAssert.requireDistinctBy(
				List.of(new Item("a"), duplicate, duplicate), Item::code,
				item -> new IllegalStateException("item:" + item.code()));
		assertThatThrownBy(executable).isInstanceOf(IllegalStateException.class).hasMessage("item:dup");
	}

	private static class TestEntity {

		private String code;

		String getCode() {
			return code;
		}

	}

}
