package com.auth.service.system.admin.excel;

import com.auth.module.file.importer.model.ImportErrorCode;
import com.auth.module.file.importer.model.ImportErrors;
import com.auth.module.file.importer.model.ImportRowError;
import com.auth.module.file.importer.model.ImportRowValidator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link ImportRowSupport} 单元测试
 *
 * @author Bunny
 */
@DisplayName("ImportRowSupport 导入行工具")
class ImportRowSupportTest {

	private static final int ROW_NUM = 3;

	@Test
	@DisplayName("valid \"启用\" 校验通过且可解析为 ENABLED")
	void requireEnableStatus_whenEnabled_returnsValidatorOk() {
		// 合法启用文案应通过校验，且不产生错误
		List<ImportRowError> errors = new ArrayList<>();
		ImportRowValidator v = ImportRowValidator.of(ROW_NUM, errors);

		ImportRowValidator result = ImportRowSupport.requireEnableStatus(v, EnableStatusLabels.ENABLED);

		assertThat(result).isSameAs(v);
		assertThat(v.ok()).isTrue();
		assertThat(EnableStatusLabels.parseImport(EnableStatusLabels.ENABLED)).isEqualTo(EnableStatus.ENABLED);
		assertThat(errors).isEmpty();
	}

	@Test
	@DisplayName("valid \"禁用\" 校验通过且可解析为 DISABLED")
	void requireEnableStatus_whenDisabled_returnsValidatorOk() {
		// 合法禁用文案应通过校验，且不产生错误
		List<ImportRowError> errors = new ArrayList<>();
		ImportRowValidator v = ImportRowValidator.of(ROW_NUM, errors);

		ImportRowValidator result = ImportRowSupport.requireEnableStatus(v, EnableStatusLabels.DISABLED);

		assertThat(result).isSameAs(v);
		assertThat(v.ok()).isTrue();
		assertThat(EnableStatusLabels.parseImport(EnableStatusLabels.DISABLED)).isEqualTo(EnableStatus.DISABLED);
		assertThat(errors).isEmpty();
	}

	@Test
	@DisplayName("invalid value fails with status invalidValue error")
	void requireEnableStatus_whenInvalidValue_failsWithInvalidValueError() {
		// 未知状态文案应记录 INVALID_VALUE 且字段为 status
		List<ImportRowError> errors = new ArrayList<>();
		ImportRowValidator v = ImportRowValidator.of(ROW_NUM, errors);
		String invalidLabel = "正常";

		ImportRowValidator result = ImportRowSupport.requireEnableStatus(v, invalidLabel);

		assertThat(result).isSameAs(v);
		assertThat(v.ok()).isFalse();
		assertThat(errors).hasSize(1);
		ImportRowError error = errors.get(0);
		assertThat(error.code()).isEqualTo(ImportErrorCode.INVALID_VALUE);
		assertThat(error.args()).containsExactly("status", invalidLabel);
		assertThat(error.message()).isEqualTo(ImportErrors.invalidValue(ROW_NUM, "status", invalidLabel).message());
	}

	@Test
	@DisplayName("already-failed validator does not add second error")
	void requireEnableStatus_whenValidatorAlreadyFailed_doesNotAddSecondError() {
		// validator 已失败时不再追加 status 相关错误
		List<ImportRowError> errors = new ArrayList<>();
		ImportRowValidator v = ImportRowValidator.of(ROW_NUM, errors);
		v.fail(ImportErrors.required(ROW_NUM, "roleCode"));

		ImportRowValidator result = ImportRowSupport.requireEnableStatus(v, "任意文案");

		assertThat(result).isSameAs(v);
		assertThat(errors).hasSize(1);
		assertThat(errors.get(0).code()).isEqualTo(ImportErrorCode.REQUIRED);
	}

}
