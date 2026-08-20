package com.auth.service.system.admin.excel.permission;

import com.auth.module.file.spreadsheet.ExcelExportWriter;
import com.auth.module.file.importer.model.SpreadsheetImportResult;
import com.auth.service.system.admin.mapper.admin.permission.SysPermissionMapper;
import com.auth.service.system.admin.model.form.permission.SysPermissionForm;
import com.auth.service.system.admin.model.po.reference.PermissionReferencePO;
import com.auth.service.system.admin.service.admin.SysPermissionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

/**
 * {@link PermissionSheetImporter} 单元测试
 *
 * @author Bunny
 */
@DisplayName("PermissionSheetImporter 权限导入")
@ExtendWith(MockitoExtension.class)
class PermissionSheetImporterTest {

	@Mock
	private SysPermissionMapper sysPermissionMapper;

	@Mock
	private SysPermissionService sysPermissionService;

	private PermissionSheetImporter permissionSheetImporter;

	private static SysPermissionImportRow validRow(String permissionCode, String permissionName) {
		SysPermissionImportRow row = new SysPermissionImportRow();
		row.setPermissionCode(permissionCode);
		row.setPermissionName(permissionName);
		row.setStatusLabel("启用");
		row.setOrderNum(1);
		row.setRemark("备注");
		return row;
	}

	private static MockMultipartFile buildImportFile(List<SysPermissionImportRow> rows) throws IOException {
		byte[] bytes = new ExcelExportWriter<>(SysPermissionImportRow.class, "权限导入").write(rows);
		return new MockMultipartFile("file", "permission_import.xlsx",
				"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", bytes);
	}

	@BeforeEach
	void setUp() {
		permissionSheetImporter = new PermissionSheetImporter(sysPermissionMapper, sysPermissionService);
	}

	@Test
	@DisplayName("合法行全部导入成功并批量调用 createBatchFromImport")
	void doImport_whenRowsValid_importsAllRows() throws IOException {
		// 两行合法权限数据
		MockMultipartFile file = buildImportFile(
				List.of(validRow("sys:audit:query", "审计查询"), validRow("sys:audit:export", "审计导出")));
		when(sysPermissionMapper.selectReferenceByPermissionCodes(anyList())).thenReturn(List.of());

		SpreadsheetImportResult result = permissionSheetImporter.doImport(file);

		assertThat(result.success()).isTrue();
		assertThat(result.importedCount()).isEqualTo(2);
		assertThat(result.errors()).isEmpty();
		@SuppressWarnings("unchecked")
		ArgumentCaptor<List<SysPermissionForm>> formCaptor = ArgumentCaptor.forClass(List.class);
		verify(sysPermissionService).createBatchFromImport(formCaptor.capture());
		assertThat(formCaptor.getValue()).extracting(SysPermissionForm::getPermissionCode)
			.containsExactly("sys:audit:query", "sys:audit:export");
	}

	@Test
	@DisplayName("缺少 permissionCode 或 permissionName 时返回行级错误且不落库")
	void doImport_whenRequiredFieldsMissing_returnsRowErrors() throws IOException {
		SysPermissionImportRow missingCode = new SysPermissionImportRow();
		missingCode.setPermissionName("无编码权限");
		SysPermissionImportRow missingName = new SysPermissionImportRow();
		missingName.setPermissionCode("sys:dept:query");
		MockMultipartFile file = buildImportFile(List.of(missingCode, missingName));
		when(sysPermissionMapper.selectReferenceByPermissionCodes(List.of("sys:dept:query"))).thenReturn(List.of());

		SpreadsheetImportResult result = permissionSheetImporter.doImport(file);

		assertThat(result.success()).isFalse();
		assertThat(result.importedCount()).isZero();
		assertThat(result.errors()).hasSize(2);
		assertThat(result.errors().get(0).message()).contains("permissionCode不能为空");
		assertThat(result.errors().get(1).message()).contains("permissionName不能为空");
		verify(sysPermissionService, never()).createBatchFromImport(any());
	}

	@Test
	@DisplayName("permissionCode 格式不符合约定时返回行级错误")
	void doImport_whenPermissionCodeInvalid_returnsFormatError() throws IOException {
		MockMultipartFile file = buildImportFile(List.of(validRow("ADMIN", "大写编码")));
		when(sysPermissionMapper.selectReferenceByPermissionCodes(List.of("ADMIN"))).thenReturn(List.of());

		SpreadsheetImportResult result = permissionSheetImporter.doImport(file);

		assertThat(result.success()).isFalse();
		assertThat(result.errors()).hasSize(1);
		assertThat(result.errors().get(0).message()).contains("格式不符合约定");
		verify(sysPermissionService, never()).createBatchFromImport(any());
	}

	@Test
	@DisplayName("文件内 permissionCode 重复时返回行级错误")
	void doImport_whenPermissionCodeDuplicatedInFile_returnsDuplicateError() throws IOException {
		MockMultipartFile file = buildImportFile(
				List.of(validRow("sys:dept:query", "权限A"), validRow("sys:dept:query", "权限B")));
		when(sysPermissionMapper.selectReferenceByPermissionCodes(List.of("sys:dept:query"))).thenReturn(List.of());

		SpreadsheetImportResult result = permissionSheetImporter.doImport(file);

		assertThat(result.success()).isFalse();
		assertThat(result.errors()).hasSize(2);
		assertThat(result.errors()).allMatch(error -> error.message().contains("在文件内重复"));
		verify(sysPermissionService, never()).createBatchFromImport(any());
	}

	@Test
	@DisplayName("permissionCode 在库中已存在时返回行级错误")
	void doImport_whenPermissionCodeExistsInDb_returnsExistsError() throws IOException {
		MockMultipartFile file = buildImportFile(List.of(validRow("sys:dept:query", "部门查询")));
		PermissionReferencePO existing = new PermissionReferencePO();
		existing.setPermissionCode("sys:dept:query");
		when(sysPermissionMapper.selectReferenceByPermissionCodes(List.of("sys:dept:query")))
			.thenReturn(List.of(existing));

		SpreadsheetImportResult result = permissionSheetImporter.doImport(file);

		assertThat(result.success()).isFalse();
		assertThat(result.errors()).hasSize(1);
		assertThat(result.errors().get(0).message()).contains("已存在");
		verify(sysPermissionService, never()).createBatchFromImport(any());
	}

}
