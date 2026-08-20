package com.auth.service.system.admin.excel.dept;

import com.auth.module.file.importer.model.ImportRowError;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link DeptRowValidator} 单元测试
 *
 * @author Bunny
 */
@DisplayName("DeptRowValidator 部门导入校验")
class DeptRowValidatorTest {

	private static DeptParsedRow parsedRow(String parentDeptCode, String statusLabel) {
		return DeptParsedRow.builder()
			.parentDeptCode(parentDeptCode)
			.deptCode("RD")
			.deptName("研发部")
			.statusLabel(statusLabel)
			.orderNum(1)
			.remark("备注")
			.build();
	}

	@Test
	@DisplayName("文件内 deptCode 重复时返回重复错误")
	void validate_whenDeptCodeDuplicatedInFile_returnsDuplicateError() {
		DeptSheetImporter.Context context = DeptSheetImporter.Context.builder()
			.parentIdByCode(Map.of())
			.duplicateDeptCodesInFile(Set.of("RD"))
			.existingDeptCodes(Set.of())
			.build();

		List<ImportRowError> errors = DeptRowValidator.validate(parsedRow(null, "启用"), 1, context);

		assertThat(errors).hasSize(1);
		assertThat(errors.get(0).message()).contains("在文件内重复");
	}

	@Test
	@DisplayName("父部门编码不存在时返回引用不存在错误")
	void validate_whenParentDeptMissing_returnsReferenceNotFound() {
		DeptSheetImporter.Context context = DeptSheetImporter.Context.builder()
			.parentIdByCode(Map.of())
			.duplicateDeptCodesInFile(Set.of())
			.existingDeptCodes(Set.of())
			.build();

		List<ImportRowError> errors = DeptRowValidator.validate(parsedRow("MISSING", "启用"), 2, context);

		assertThat(errors).hasSize(1);
		assertThat(errors.get(0).message()).contains("parentDeptCode");
		assertThat(errors.get(0).message()).contains("不存在");
	}

	@Test
	@DisplayName("状态文案非法时返回无效值错误")
	void validate_whenStatusInvalid_returnsInvalidValue() {
		DeptSheetImporter.Context context = DeptSheetImporter.Context.builder()
			.parentIdByCode(Map.of("ROOT", 1L))
			.duplicateDeptCodesInFile(Set.of())
			.existingDeptCodes(Set.of())
			.build();

		List<ImportRowError> errors = DeptRowValidator.validate(parsedRow("ROOT", "未知状态"), 3, context);

		assertThat(errors).hasSize(1);
		assertThat(errors.get(0).message()).contains("status");
		assertThat(errors.get(0).message()).contains("无效");
	}

}
