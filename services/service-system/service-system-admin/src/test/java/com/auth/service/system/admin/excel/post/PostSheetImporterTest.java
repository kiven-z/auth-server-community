package com.auth.service.system.admin.excel.post;

import com.auth.module.file.spreadsheet.ExcelExportWriter;
import com.auth.module.file.importer.model.SpreadsheetImportResult;
import com.auth.service.system.admin.mapper.admin.post.SysPostMapper;
import com.auth.service.system.admin.model.form.post.SysPostForm;
import com.auth.service.system.admin.model.po.post.PostDeptCodePairPO;
import com.auth.service.system.admin.service.admin.SysPostService;
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
 * {@link PostSheetImporter} 单元测试
 *
 * @author Bunny
 */
@DisplayName("PostSheetImporter 岗位导入")
@ExtendWith(MockitoExtension.class)
class PostSheetImporterTest {

	@Mock
	private DeptLookupSupport deptLookupSupport;

	@Mock
	private SysPostMapper sysPostMapper;

	@Mock
	private SysPostService sysPostService;

	private PostSheetImporter postSheetImporter;

	private static SysPostImportRow validRow(String postCode, String postName) {
		SysPostImportRow row = new SysPostImportRow();
		row.setDeptCode("RD");
		row.setPostCode(postCode);
		row.setPostName(postName);
		row.setStatusLabel("启用");
		row.setOrderNum(1);
		row.setRemark("备注");
		return row;
	}

	private static MockMultipartFile buildImportFile(List<SysPostImportRow> rows) throws IOException {
		byte[] bytes = new ExcelExportWriter<>(SysPostImportRow.class, "岗位导入").write(rows);
		return new MockMultipartFile("file", "post_import.xlsx",
				"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", bytes);
	}

	@BeforeEach
	void setUp() {
		postSheetImporter = new PostSheetImporter(deptLookupSupport, sysPostMapper, sysPostService);
		when(sysPostMapper.selectAssignableDeptIds(anyList())).thenAnswer(invocation -> invocation.getArgument(0));
	}

	@Test
	@DisplayName("合法行全部导入成功并批量调用 createBatchFromImport")
	void doImport_whenRowsValid_importsAllRows() throws IOException {
		MockMultipartFile file = buildImportFile(List.of(validRow("DEV", "研发岗"), validRow("OPS", "运维岗")));
		when(deptLookupSupport.resolveIdsByCodes(anyList(), any())).thenReturn(Map.of("RD", 2L));
		when(sysPostMapper.selectReferenceByDeptPostPairs(anyList())).thenReturn(List.of());

		SpreadsheetImportResult result = postSheetImporter.doImport(file);

		assertThat(result.success()).isTrue();
		assertThat(result.importedCount()).isEqualTo(2);
		assertThat(result.errors()).isEmpty();
		@SuppressWarnings("unchecked")
		ArgumentCaptor<List<SysPostForm>> formCaptor = ArgumentCaptor.forClass(List.class);
		verify(sysPostService).createBatchFromImport(formCaptor.capture());
		assertThat(formCaptor.getValue()).extracting(SysPostForm::getPostCode).containsExactly("DEV", "OPS");
	}

	@Test
	@DisplayName("缺少 deptCode 或 postCode 时返回行级错误且不落库")
	void doImport_whenRequiredFieldsMissing_returnsRowErrors() throws IOException {
		SysPostImportRow missingDept = new SysPostImportRow();
		missingDept.setPostCode("DEV");
		missingDept.setPostName("研发");
		SysPostImportRow missingPostCode = new SysPostImportRow();
		missingPostCode.setDeptCode("RD");
		missingPostCode.setPostName("研发");
		MockMultipartFile file = buildImportFile(List.of(missingDept, missingPostCode));
		when(deptLookupSupport.resolveIdsByCodes(anyList(), any())).thenReturn(Map.of("RD", 2L));

		SpreadsheetImportResult result = postSheetImporter.doImport(file);

		assertThat(result.success()).isFalse();
		assertThat(result.importedCount()).isZero();
		assertThat(result.errors()).hasSize(2);
		verify(sysPostService, never()).createBatchFromImport(any());
	}

	@Test
	@DisplayName("文件内 (deptCode, postCode) 重复时返回行级错误")
	void doImport_whenCompositeKeyDuplicatedInFile_returnsDuplicateError() throws IOException {
		MockMultipartFile file = buildImportFile(List.of(validRow("DUP", "岗位A"), validRow("DUP", "岗位B")));
		when(deptLookupSupport.resolveIdsByCodes(anyList(), any())).thenReturn(Map.of("RD", 2L));
		when(sysPostMapper.selectReferenceByDeptPostPairs(anyList())).thenReturn(List.of());

		SpreadsheetImportResult result = postSheetImporter.doImport(file);

		assertThat(result.success()).isFalse();
		assertThat(result.errors()).hasSize(2);
		assertThat(result.errors()).allMatch(error -> error.message().contains("在文件内重复"));
		verify(sysPostService, never()).createBatchFromImport(any());
	}

	@Test
	@DisplayName("部门存在但不可分配时返回行级错误且不落库")
	void doImport_whenDeptExistsButNotAssignable_returnsRowError() throws IOException {
		MockMultipartFile file = buildImportFile(List.of(validRow("DEV", "研发岗")));
		when(deptLookupSupport.resolveIdsByCodes(anyList(), any())).thenReturn(Map.of("RD", 2L));
		when(sysPostMapper.selectAssignableDeptIds(List.of(2L))).thenReturn(List.of());
		when(sysPostMapper.selectReferenceByDeptPostPairs(anyList())).thenReturn(List.of());

		SpreadsheetImportResult result = postSheetImporter.doImport(file);

		assertThat(result.success()).isFalse();
		assertThat(result.importedCount()).isZero();
		assertThat(result.errors()).hasSize(1);
		assertThat(result.errors().get(0).message()).contains("不存在或不可用");
		verify(sysPostService, never()).createBatchFromImport(any());
	}

	@Test
	@DisplayName("(deptCode, postCode) 在库中已存在时返回行级错误")
	void doImport_whenCompositeKeyExistsInDb_returnsExistsError() throws IOException {
		MockMultipartFile file = buildImportFile(List.of(validRow("DEV", "研发岗")));
		when(deptLookupSupport.resolveIdsByCodes(anyList(), any())).thenReturn(Map.of("RD", 2L));
		PostDeptCodePairPO existing = new PostDeptCodePairPO();
		existing.setDeptId(2L);
		existing.setPostCode("DEV");
		when(sysPostMapper.selectReferenceByDeptPostPairs(anyList())).thenReturn(List.of(existing));

		SpreadsheetImportResult result = postSheetImporter.doImport(file);

		assertThat(result.success()).isFalse();
		assertThat(result.errors()).hasSize(1);
		assertThat(result.errors().get(0).message()).contains("已存在");
		verify(sysPostService, never()).createBatchFromImport(any());
	}

}
