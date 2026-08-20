package com.auth.service.system.admin.excel.permission;

import com.auth.module.file.importer.model.ImportRowError;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link PermissionRowValidator} 单元测试
 *
 * @author Bunny
 */
@DisplayName("PermissionRowValidator 权限导入校验")
class PermissionRowValidatorTest {

	private static PermissionParsedRow parsedRow(String permissionCode, String statusLabel) {
		return PermissionParsedRow.builder()
			.permissionCode(permissionCode)
			.permissionName("部门查询")
			.statusLabel(statusLabel)
			.orderNum(1)
			.remark("备注")
			.build();
	}

	@Test
	@DisplayName("文件内 permissionCode 重复时返回重复错误")
	void validate_whenPermissionCodeDuplicatedInFile_returnsDuplicateError() {
		PermissionSheetImporter.Context context = PermissionSheetImporter.Context.builder()
			.duplicatePermissionCodesInFile(Set.of("sys:dept:query"))
			.existingPermissionCodes(Set.of())
			.build();

		List<ImportRowError> errors = PermissionRowValidator.validate(parsedRow("sys:dept:query", "启用"), 1, context);

		assertThat(errors).hasSize(1);
		assertThat(errors.get(0).message()).contains("在文件内重复");
	}

	@Test
	@DisplayName("permissionCode 格式非法时返回格式错误")
	void validate_whenPermissionCodeInvalid_returnsFormatError() {
		PermissionSheetImporter.Context context = PermissionSheetImporter.Context.builder()
			.duplicatePermissionCodesInFile(Set.of())
			.existingPermissionCodes(Set.of())
			.build();

		List<ImportRowError> errors = PermissionRowValidator.validate(parsedRow("ADMIN", "启用"), 2, context);

		assertThat(errors).hasSize(1);
		assertThat(errors.get(0).message()).contains("格式不符合约定");
	}

	@Test
	@DisplayName("状态文案非法时返回无效值错误")
	void validate_whenStatusInvalid_returnsInvalidValue() {
		PermissionSheetImporter.Context context = PermissionSheetImporter.Context.builder()
			.duplicatePermissionCodesInFile(Set.of())
			.existingPermissionCodes(Set.of())
			.build();

		List<ImportRowError> errors = PermissionRowValidator.validate(parsedRow("sys:dept:query", "非法"), 3, context);

		assertThat(errors).hasSize(1);
		assertThat(errors.get(0).message()).contains("status");
		assertThat(errors.get(0).message()).contains("无效");
	}

}
