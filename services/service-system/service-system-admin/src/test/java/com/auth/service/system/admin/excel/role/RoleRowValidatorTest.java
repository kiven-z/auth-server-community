package com.auth.service.system.admin.excel.role;

import com.auth.module.file.importer.model.ImportRowError;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link RoleRowValidator} 单元测试
 *
 * @author Bunny
 */
@DisplayName("RoleRowValidator 角色导入校验")
class RoleRowValidatorTest {

	private static RoleParsedRow parsedRow(String roleCode, String statusLabel) {
		return RoleParsedRow.builder()
			.roleCode(roleCode)
			.roleName("管理员")
			.statusLabel(statusLabel)
			.orderNum(1)
			.remark("备注")
			.build();
	}

	@Test
	@DisplayName("文件内 roleCode 重复时返回重复错误")
	void validate_whenRoleCodeDuplicatedInFile_returnsDuplicateError() {
		RoleSheetImporter.Context context = RoleSheetImporter.Context.builder()
			.duplicateRoleCodesInFile(Set.of("ADMIN"))
			.existingRoleCodes(Set.of())
			.build();

		List<ImportRowError> errors = RoleRowValidator.validate(parsedRow("ADMIN", "启用"), 1, context);

		assertThat(errors).hasSize(1);
		assertThat(errors.get(0).message()).contains("在文件内重复");
	}

	@Test
	@DisplayName("roleCode 格式非法时返回格式错误")
	void validate_whenRoleCodeInvalid_returnsFormatError() {
		RoleSheetImporter.Context context = RoleSheetImporter.Context.builder()
			.duplicateRoleCodesInFile(Set.of())
			.existingRoleCodes(Set.of())
			.build();

		List<ImportRowError> errors = RoleRowValidator.validate(parsedRow("admin", "启用"), 2, context);

		assertThat(errors).hasSize(1);
		assertThat(errors.get(0).message()).contains("格式不符合约定");
	}

	@Test
	@DisplayName("状态文案非法时返回无效值错误")
	void validate_whenStatusInvalid_returnsInvalidValue() {
		RoleSheetImporter.Context context = RoleSheetImporter.Context.builder()
			.duplicateRoleCodesInFile(Set.of())
			.existingRoleCodes(Set.of())
			.build();

		List<ImportRowError> errors = RoleRowValidator.validate(parsedRow("ADMIN", "非法"), 3, context);

		assertThat(errors).hasSize(1);
		assertThat(errors.get(0).message()).contains("status");
		assertThat(errors.get(0).message()).contains("无效");
	}

}
