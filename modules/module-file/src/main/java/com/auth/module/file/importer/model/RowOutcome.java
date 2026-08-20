package com.auth.module.file.importer.model;

import java.util.List;
import java.util.function.Function;

/**
 * 单行处理结果：成功携带值，失败携带错误列表
 *
 * @param <T> 成功时的值类型
 * @author Bunny
 */
public sealed interface RowOutcome<T> permits RowOutcome.Ok, RowOutcome.Err {

	/**
	 * 构建成功结果
	 * @param value 值
	 * @param <T> 值类型
	 * @return 成功结果
	 */
	static <T> RowOutcome<T> ok(T value) {
		return new Ok<>(value);
	}

	/**
	 * 构建失败结果
	 * @param error 行级错误
	 * @param <T> 值类型
	 * @return 失败结果
	 */
	static <T> RowOutcome<T> err(ImportRowError error) {
		return new Err<>(List.of(error));
	}

	/**
	 * 构建失败结果
	 * @param errors 行级错误列表
	 * @param <T> 值类型
	 * @return 失败结果
	 */
	static <T> RowOutcome<T> err(List<ImportRowError> errors) {
		return new Err<>(errors);
	}

	/**
	 * 是否成功
	 * @return 是否成功
	 */
	boolean ok();

	/**
	 * 成功时的值；失败时为 null
	 * @return 值
	 */
	T value();

	/**
	 * 失败时的错误列表；成功时为空列表
	 * @return 错误列表
	 */
	List<ImportRowError> errors();

	/**
	 * 成功时映射值；失败时保留原错误
	 * @param mapper 映射函数
	 * @param <U> 目标类型
	 * @return 映射后的结果
	 */
	default <U> RowOutcome<U> map(Function<T, U> mapper) {
		if (!ok()) {
			return new Err<>(errors());
		}
		return new Ok<>(mapper.apply(value()));
	}

	/**
	 * 成功结果
	 *
	 * @param value 成功值
	 * @param <T> 值类型
	 */
	record Ok<T>(T value) implements RowOutcome<T> {

		@Override
		public boolean ok() {
			return true;
		}

		@Override
		public List<ImportRowError> errors() {
			return List.of();
		}

	}

	/**
	 * 失败结果
	 *
	 * @param errors 错误列表
	 * @param <T> 值类型
	 */
	record Err<T>(List<ImportRowError> errors) implements RowOutcome<T> {

		public Err {
			errors = List.copyOf(errors);
		}

		@Override
		public boolean ok() {
			return false;
		}

		@Override
		public T value() {
			return null;
		}

	}

}
