package com.auth.service.system.admin.excel.dept;

import com.auth.module.file.spreadsheet.ExcelExportWriter;
import com.auth.module.file.importer.model.SpreadsheetImportResult;
import com.auth.service.system.admin.mapper.admin.dept.SysDeptMapper;
import com.auth.service.system.admin.model.entity.SysDeptEntity;
import com.auth.service.system.admin.model.form.dept.SysDeptForm;
import com.auth.service.system.admin.service.admin.SysDeptService;
import com.auth.service.system.admin.support.dept.DeptLookupSupport;
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
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

/**
 * {@link DeptSheetImporter} 单元测试
 *
 * @author Bunny
 */
@DisplayName("DeptSheetImporter 部门导入")
@ExtendWith(MockitoExtension.class)
class DeptSheetImporterTest {

	@Mock
	private SysDeptMapper sysDeptMapper;

	@Mock
	private SysDeptService sysDeptService;

	@Mock
	private DeptLookupSupport deptLookupSupport;

	private DeptSheetImporter deptSheetImporter;

	private static SysDeptImportRow validRow(String parentDeptCode, String deptCode, String deptName) {
		SysDeptImportRow row = new SysDeptImportRow();
		row.setParentDeptCode(parentDeptCode);
		row.setDeptCode(deptCode);
		row.setDeptName(deptName);
		row.setStatusLabel("启用");
		row.setOrderNum(1);
		row.setRemark("备注");
		return row;
	}

	private static MockMultipartFile buildImportFile(List<SysDeptImportRow> rows) throws IOException {
		byte[] bytes = new ExcelExportWriter<>(SysDeptImportRow.class, "部门导入").write(rows);
		return new MockMultipartFile("file", "dept_import.xlsx",
				"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", bytes);
	}

	@BeforeEach
	void setUp() {
		deptSheetImporter = new DeptSheetImporter(sysDeptMapper, sysDeptService, deptLookupSupport);
	}

	@Test
	@DisplayName("合法行全部导入成功并批量调用 createBatchFromImport")
	void doImport_whenRowsValid_importsAllRows() throws IOException {
		MockMultipartFile file = buildImportFile(
				List.of(validRow("ROOT", "RD", "研发部"), validRow("ROOT", "OPS", "运维部")));
		when(deptLookupSupport.resolveIdsByCodes(anyList(), any())).thenReturn(Map.of("ROOT", 1L));
		when(sysDeptMapper.selectActiveByDeptCodes(List.of("RD", "OPS"))).thenReturn(List.of());

		SpreadsheetImportResult result = deptSheetImporter.doImport(file);

		assertThat(result.success()).isTrue();
		assertThat(result.importedCount()).isEqualTo(2);
		assertThat(result.errors()).isEmpty();
		@SuppressWarnings("unchecked")
		ArgumentCaptor<List<SysDeptForm>> formCaptor = ArgumentCaptor.forClass(List.class);
		verify(sysDeptService).createBatchFromImport(formCaptor.capture());
		assertThat(formCaptor.getValue()).extracting(SysDeptForm::getDeptCode).containsExactly("RD", "OPS");
	}

	@Test
	@DisplayName("缺少 deptCode 或 deptName 时返回行级错误且不落库")
	void doImport_whenRequiredFieldsMissing_returnsRowErrors() throws IOException {
		SysDeptImportRow missingCode = new SysDeptImportRow();
		missingCode.setDeptName("无编码部门");
		SysDeptImportRow missingName = new SysDeptImportRow();
		missingName.setDeptCode("NO_NAME");
		MockMultipartFile file = buildImportFile(List.of(missingCode, missingName));
		when(deptLookupSupport.resolveIdsByCodes(anyList(), any())).thenReturn(Map.of());
		when(sysDeptMapper.selectActiveByDeptCodes(List.of("NO_NAME"))).thenReturn(List.of());

		SpreadsheetImportResult result = deptSheetImporter.doImport(file);

		assertThat(result.success()).isFalse();
		assertThat(result.importedCount()).isZero();
		assertThat(result.errors()).hasSize(2);
		assertThat(result.errors().get(0).message()).contains("deptCode不能为空");
		assertThat(result.errors().get(1).message()).contains("deptName不能为空");
		verify(sysDeptService, never()).createBatchFromImport(any());
	}

	@Test
	@DisplayName("文件内 deptCode 重复时返回行级错误")
	void doImport_whenDeptCodeDuplicatedInFile_returnsDuplicateError() throws IOException {
		MockMultipartFile file = buildImportFile(
				List.of(validRow(null, "DUP_DEPT", "部门A"), validRow(null, "DUP_DEPT", "部门B")));
		when(deptLookupSupport.resolveIdsByCodes(anyList(), any())).thenReturn(Map.of());
		when(sysDeptMapper.selectActiveByDeptCodes(List.of("DUP_DEPT"))).thenReturn(List.of());

		SpreadsheetImportResult result = deptSheetImporter.doImport(file);

		assertThat(result.success()).isFalse();
		assertThat(result.errors()).hasSize(2);
		assertThat(result.errors()).allMatch(error -> error.message().contains("在文件内重复"));
		verify(sysDeptService, never()).createBatchFromImport(any());
	}

	@Test
	@DisplayName("deptCode 在库中已存在时返回行级错误")
	void doImport_whenDeptCodeExistsInDb_returnsExistsError() throws IOException {
		MockMultipartFile file = buildImportFile(List.of(validRow(null, "RD", "研发部")));
		when(deptLookupSupport.resolveIdsByCodes(anyList(), any())).thenReturn(Map.of());
		SysDeptEntity existing = new SysDeptEntity();
		existing.setDeptCode("RD");
		when(sysDeptMapper.selectActiveByDeptCodes(List.of("RD"))).thenReturn(List.of(existing));

		SpreadsheetImportResult result = deptSheetImporter.doImport(file);

		assertThat(result.success()).isFalse();
		assertThat(result.errors()).hasSize(1);
		assertThat(result.errors().get(0).message()).contains("已存在");
		verify(sysDeptService, never()).createBatchFromImport(any());
	}

	@Test
	@DisplayName("父部门编码不存在时返回行级错误")
	void doImport_whenParentDeptNotFound_returnsParentError() throws IOException {
		MockMultipartFile file = buildImportFile(List.of(validRow("MISSING", "NEW_DEPT", "新部门")));
		when(deptLookupSupport.resolveIdsByCodes(anyList(), any())).thenReturn(Map.of());
		when(sysDeptMapper.selectActiveByDeptCodes(List.of("NEW_DEPT"))).thenReturn(List.of());

		SpreadsheetImportResult result = deptSheetImporter.doImport(file);

		assertThat(result.success()).isFalse();
		assertThat(result.errors()).hasSize(1);
		assertThat(result.errors().get(0).message()).contains("parentDeptCode");
		verify(sysDeptService, never()).createBatchFromImport(any());
	}

}
