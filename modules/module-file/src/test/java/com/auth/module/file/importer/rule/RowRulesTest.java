package com.auth.module.file.importer.rule;

import com.auth.module.file.importer.model.ImportRowError;
import lombok.Value;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static com.auth.module.file.importer.model.ImportErrorCode.INVALID_FORMAT;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link RowRules} 单元测试
 *
 * @author Bunny
 */
@DisplayName("RowRules 规则组合")
class RowRulesTest {

	private static final int ROW_NUM = 5;

	private static ImportRowError error(String tag) {
		return com.auth.module.file.importer.model.ImportErrors.of(ROW_NUM, INVALID_FORMAT, "field", tag);
	}

	@Test
	@DisplayName("applyFailFast 遇到首个错误即停止")
	void applyFailFast_stopsAtFirstError() {
		RowRule<Parsed, Context> first = (parsed, rowNum, ctx) -> List.of(error("first"));
		RowRule<Parsed, Context> second = (parsed, rowNum, ctx) -> List.of(error("second"));

		List<ImportRowError> errors = RowRules.applyFailFast(List.of(first, second), new Parsed("x"), ROW_NUM,
				new Context());

		assertThat(errors).hasSize(1);
		assertThat(errors.get(0).message()).contains("first");
	}

	@Test
	@DisplayName("unique 文件内重复时返回 duplicate 错误")
	void unique_whenDuplicateInFile_returnsError() {
		RowRule<Parsed, Context> rule = RowRules.unique("username", Parsed::getFieldValue, ctx -> Set.of("admin"),
				ctx -> Set.of());

		List<ImportRowError> errors = rule.check(new Parsed("admin"), ROW_NUM, new Context());

		assertThat(errors).hasSize(1);
		assertThat(errors.get(0).message()).contains("在文件内重复");
	}

	@Test
	@DisplayName("lengthBetween 超出范围时返回长度错误")
	void lengthBetween_whenOutOfRange_returnsError() {
		RowRule<Parsed, ?> rule = RowRules.lengthBetween("password", Parsed::getFieldValue, 8, 18);

		List<ImportRowError> errors = rule.check(new Parsed("short"), ROW_NUM, null);

		assertThat(errors).hasSize(1);
		assertThat(errors.get(0).message()).contains("长度须在");
	}

	@Value
	private static class Parsed {

		String fieldValue;

	}

	private static final class Context {

	}

}
