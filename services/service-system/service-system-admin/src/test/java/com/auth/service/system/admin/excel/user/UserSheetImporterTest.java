package com.auth.service.system.admin.excel.user;

import com.auth.module.file.spreadsheet.ExcelExportWriter;
import com.auth.module.file.importer.model.SpreadsheetImportResult;
import com.auth.service.system.admin.model.form.user.SysUserForm;
import com.auth.service.system.admin.model.po.user.UserBusinessKeysExisting;
import com.auth.service.system.admin.service.admin.SysUserService;
import com.auth.service.system.admin.support.user.UserReferenceChecker;
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
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * {@link UserSheetImporter} 单元测试
 *
 * @author Bunny
 */
@DisplayName("UserSheetImporter 用户导入")
@ExtendWith(MockitoExtension.class)
class UserSheetImporterTest {

	@Mock
	private UserReferenceChecker userReferenceChecker;

	@Mock
	private SysUserService sysUserService;

	private UserSheetImporter userSheetImporter;

	private static SysUserImportRow validRow(String username, String nickname) {
		SysUserImportRow row = new SysUserImportRow();
		row.setUsername(username);
		row.setNickname(nickname);
		row.setEmail(username + "@example.com");
		row.setPhone("1380000" + username.hashCode() % 10000);
		row.setStatusLabel("启用");
		row.setInitialPassword("Abcd1234");
		row.setGenderLabel("男");
		row.setBirthday("1990-01-01");
		return row;
	}

	private static MockMultipartFile buildImportFile(List<SysUserImportRow> rows) throws IOException {
		byte[] bytes = new ExcelExportWriter<>(SysUserImportRow.class, "用户导入").write(rows);
		return new MockMultipartFile("file", "user_import.xlsx",
				"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", bytes);
	}

	@BeforeEach
	void setUp() {
		userSheetImporter = new UserSheetImporter(userReferenceChecker, sysUserService);
		when(userReferenceChecker.findExistingBusinessKeys(any(), any(), any(), any(), isNull()))
			.thenReturn(UserBusinessKeysExisting.empty());
	}

	@Test
	@DisplayName("合法行全部导入成功并批量调用 createBatchFromImport")
	void doImport_whenRowsValid_importsAllRows() throws IOException {
		// 两行合法用户数据
		MockMultipartFile file = buildImportFile(List.of(validRow("zhangsan", "张三"), validRow("lisi", "李四")));

		SpreadsheetImportResult result = userSheetImporter.doImport(file);

		assertThat(result.success()).isTrue();
		assertThat(result.importedCount()).isEqualTo(2);
		assertThat(result.errors()).isEmpty();
		@SuppressWarnings("unchecked")
		ArgumentCaptor<List<SysUserForm>> formCaptor = ArgumentCaptor.forClass(List.class);
		verify(sysUserService).createBatchFromImport(formCaptor.capture());
		assertThat(formCaptor.getValue()).extracting(SysUserForm::getUsername).containsExactly("zhangsan", "lisi");
		verify(userReferenceChecker, times(1)).findExistingBusinessKeys(any(), any(), any(), any(), isNull());
	}

	@Test
	@DisplayName("缺少必填字段时返回行级错误且不落库")
	void doImport_whenRequiredFieldsMissing_returnsRowErrors() throws IOException {
		SysUserImportRow missingUsername = new SysUserImportRow();
		missingUsername.setNickname("无用户名");
		missingUsername.setEmail("a@example.com");
		missingUsername.setPhone("13800000001");
		missingUsername.setStatusLabel("启用");
		missingUsername.setInitialPassword("Abcd1234");
		MockMultipartFile file = buildImportFile(List.of(missingUsername));

		SpreadsheetImportResult result = userSheetImporter.doImport(file);

		assertThat(result.success()).isFalse();
		assertThat(result.importedCount()).isZero();
		assertThat(result.errors()).hasSize(1);
		assertThat(result.errors().get(0).message()).contains("username不能为空");
		verify(sysUserService, never()).createBatchFromImport(anyList());
	}

	@Test
	@DisplayName("文件内 username 重复时返回行级错误")
	void doImport_whenUsernameDuplicatedInFile_returnsDuplicateError() throws IOException {
		MockMultipartFile file = buildImportFile(List.of(validRow("dup_user", "用户A"), validRow("dup_user", "用户B")));

		SpreadsheetImportResult result = userSheetImporter.doImport(file);

		assertThat(result.success()).isFalse();
		assertThat(result.errors()).hasSize(2);
		assertThat(result.errors()).allMatch(error -> error.message().contains("在文件内重复"));
		verify(sysUserService, never()).createBatchFromImport(anyList());
	}

	@Test
	@DisplayName("username 在库中已存在时返回行级错误")
	void doImport_whenUsernameExistsInDb_returnsExistsError() throws IOException {
		MockMultipartFile file = buildImportFile(List.of(validRow("admin", "管理员")));
		when(userReferenceChecker.findExistingBusinessKeys(any(), any(), any(), any(), isNull()))
			.thenReturn(UserBusinessKeysExisting.builder()
				.usernames(Set.of("admin"))
				.emails(Set.of())
				.phones(Set.of())
				.employeeNos(Set.of())
				.build());

		SpreadsheetImportResult result = userSheetImporter.doImport(file);

		assertThat(result.success()).isFalse();
		assertThat(result.errors()).hasSize(1);
		assertThat(result.errors().get(0).message()).contains("已存在");
		verify(sysUserService, never()).createBatchFromImport(anyList());
	}

	@Test
	@DisplayName("状态文案无效时返回行级错误")
	void doImport_whenStatusInvalid_returnsFormatError() throws IOException {
		SysUserImportRow row = validRow("bad_status", "状态错误");
		row.setStatusLabel("未知状态");
		MockMultipartFile file = buildImportFile(List.of(row));

		SpreadsheetImportResult result = userSheetImporter.doImport(file);

		assertThat(result.success()).isFalse();
		assertThat(result.errors()).hasSize(1);
		assertThat(result.errors().get(0).message()).contains("status");
		verify(sysUserService, never()).createBatchFromImport(anyList());
	}

}
