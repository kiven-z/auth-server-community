package com.auth.service.system.admin.excel.role;

import com.auth.module.file.spreadsheet.ExcelExportWriter;
import com.auth.module.file.importer.model.SpreadsheetImportResult;
import com.auth.service.system.admin.mapper.admin.role.SysRoleMapper;
import com.auth.service.system.admin.model.form.role.SysRoleForm;
import com.auth.service.system.admin.model.po.reference.RoleReferencePO;
import com.auth.service.system.admin.service.admin.SysRoleService;
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
 * {@link RoleSheetImporter} 单元测试
 *
 * @author Bunny
 */
@DisplayName("RoleSheetImporter 角色导入")
@ExtendWith(MockitoExtension.class)
class RoleSheetImporterTest {

	@Mock
	private SysRoleMapper sysRoleMapper;

	@Mock
	private SysRoleService sysRoleService;

	private RoleSheetImporter roleSheetImporter;

	private static SysRoleImportRow validRow(String roleCode, String roleName) {
		SysRoleImportRow row = new SysRoleImportRow();
		row.setRoleCode(roleCode);
		row.setRoleName(roleName);
		row.setStatusLabel("启用");
		row.setOrderNum(1);
		row.setRemark("备注");
		return row;
	}

	private static MockMultipartFile buildImportFile(List<SysRoleImportRow> rows) throws IOException {
		byte[] bytes = new ExcelExportWriter<>(SysRoleImportRow.class, "角色导入").write(rows);
		return new MockMultipartFile("file", "role_import.xlsx",
				"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", bytes);
	}

	@BeforeEach
	void setUp() {
		roleSheetImporter = new RoleSheetImporter(sysRoleMapper, sysRoleService);
	}

	@Test
	@DisplayName("合法行全部导入成功并批量调用 createBatchFromImport")
	void doImport_whenRowsValid_importsAllRows() throws IOException {
		// 两行合法角色数据
		MockMultipartFile file = buildImportFile(List.of(validRow("AUDITOR", "审计员"), validRow("OPERATOR", "操作员")));
		when(sysRoleMapper.selectReferenceByRoleCodes(anyList())).thenReturn(List.of());

		SpreadsheetImportResult result = roleSheetImporter.doImport(file);

		assertThat(result.success()).isTrue();
		assertThat(result.importedCount()).isEqualTo(2);
		assertThat(result.errors()).isEmpty();
		@SuppressWarnings("unchecked")
		ArgumentCaptor<List<SysRoleForm>> formCaptor = ArgumentCaptor.forClass(List.class);
		verify(sysRoleService).createBatchFromImport(formCaptor.capture());
		assertThat(formCaptor.getValue()).extracting(SysRoleForm::getRoleCode).containsExactly("AUDITOR", "OPERATOR");
	}

	@Test
	@DisplayName("缺少 roleCode 或 roleName 时返回行级错误且不落库")
	void doImport_whenRequiredFieldsMissing_returnsRowErrors() throws IOException {
		SysRoleImportRow missingCode = new SysRoleImportRow();
		missingCode.setRoleName("无编码角色");
		SysRoleImportRow missingName = new SysRoleImportRow();
		missingName.setRoleCode("NO_NAME");
		MockMultipartFile file = buildImportFile(List.of(missingCode, missingName));
		when(sysRoleMapper.selectReferenceByRoleCodes(List.of("NO_NAME"))).thenReturn(List.of());

		SpreadsheetImportResult result = roleSheetImporter.doImport(file);

		assertThat(result.success()).isFalse();
		assertThat(result.importedCount()).isZero();
		assertThat(result.errors()).hasSize(2);
		assertThat(result.errors().get(0).message()).contains("roleCode不能为空");
		assertThat(result.errors().get(1).message()).contains("roleName不能为空");
		verify(sysRoleService, never()).createBatchFromImport(any());
	}

	@Test
	@DisplayName("roleCode 格式不符合约定时返回行级错误")
	void doImport_whenRoleCodeInvalid_returnsFormatError() throws IOException {
		MockMultipartFile file = buildImportFile(List.of(validRow("admin", "小写编码")));
		when(sysRoleMapper.selectReferenceByRoleCodes(List.of("admin"))).thenReturn(List.of());

		SpreadsheetImportResult result = roleSheetImporter.doImport(file);

		assertThat(result.success()).isFalse();
		assertThat(result.errors()).hasSize(1);
		assertThat(result.errors().get(0).message()).contains("格式不符合约定");
		verify(sysRoleService, never()).createBatchFromImport(any());
	}

	@Test
	@DisplayName("文件内 roleCode 重复时返回行级错误")
	void doImport_whenRoleCodeDuplicatedInFile_returnsDuplicateError() throws IOException {
		MockMultipartFile file = buildImportFile(List.of(validRow("DUP_ROLE", "角色A"), validRow("DUP_ROLE", "角色B")));
		when(sysRoleMapper.selectReferenceByRoleCodes(List.of("DUP_ROLE"))).thenReturn(List.of());

		SpreadsheetImportResult result = roleSheetImporter.doImport(file);

		assertThat(result.success()).isFalse();
		assertThat(result.errors()).hasSize(2);
		assertThat(result.errors()).allMatch(error -> error.message().contains("在文件内重复"));
		verify(sysRoleService, never()).createBatchFromImport(any());
	}

	@Test
	@DisplayName("roleCode 在库中已存在时返回行级错误")
	void doImport_whenRoleCodeExistsInDb_returnsExistsError() throws IOException {
		MockMultipartFile file = buildImportFile(List.of(validRow("ADMIN", "管理员")));
		RoleReferencePO existing = new RoleReferencePO();
		existing.setRoleCode("ADMIN");
		when(sysRoleMapper.selectReferenceByRoleCodes(List.of("ADMIN"))).thenReturn(List.of(existing));

		SpreadsheetImportResult result = roleSheetImporter.doImport(file);

		assertThat(result.success()).isFalse();
		assertThat(result.errors()).hasSize(1);
		assertThat(result.errors().get(0).message()).contains("已存在");
		verify(sysRoleService, never()).createBatchFromImport(any());
	}

}
