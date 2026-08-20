package com.auth.service.system.admin.excel.user;

import com.auth.module.file.importer.AbstractSheetImporter;
import com.auth.module.file.importer.model.ImportRowError;
import com.auth.module.file.importer.model.RowOutcome;
import com.auth.service.system.admin.excel.ImportRowSupport;
import com.auth.service.system.admin.model.form.user.SysUserForm;
import com.auth.service.system.admin.model.po.user.UserBusinessKeysExisting;
import com.auth.service.system.admin.service.admin.SysUserService;
import com.auth.service.system.admin.support.user.UserReferenceChecker;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.experimental.Accessors;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

/**
 * 用户导入器：Extract → Validate → Map 三阶段编排。
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Component
public class UserSheetImporter extends AbstractSheetImporter<SysUserImportRow, SysUserForm, UserSheetImporter.Context> {

	private final UserReferenceChecker userReferenceChecker;

	private final SysUserService sysUserService;

	@Override
	protected Class<SysUserImportRow> rowType() {
		return SysUserImportRow.class;
	}

	@Override
	protected Context prepareContext(List<SysUserImportRow> rows) {
		// 检测文件内重复的字段值
		Set<String> usernamesInFile = ImportRowSupport.duplicatesInFile(rows, SysUserImportRow::getUsername);
		Set<String> emailsInFile = ImportRowSupport.duplicatesInFile(rows, SysUserImportRow::getEmail);
		Set<String> phonesInFile = ImportRowSupport.duplicatesInFile(rows, SysUserImportRow::getPhone);
		Set<String> employeeNosInFile = ImportRowSupport.duplicatesInFile(rows, SysUserImportRow::getEmployeeNo);

		// 提取文件内不重复的字段值
		List<String> usernameCandidates = ImportRowSupport.collectDistinct(rows, SysUserImportRow::getUsername);
		List<String> emailCandidates = ImportRowSupport.collectDistinct(rows, SysUserImportRow::getEmail);
		List<String> phoneCandidates = ImportRowSupport.collectDistinct(rows, SysUserImportRow::getPhone);
		List<String> employeeNoCandidates = ImportRowSupport.collectDistinct(rows, SysUserImportRow::getEmployeeNo);

		// 查询库中已存在的字段值
		UserBusinessKeysExisting existing = userReferenceChecker.findExistingBusinessKeys(
				Set.copyOf(usernameCandidates), Set.copyOf(emailCandidates), Set.copyOf(phoneCandidates),
				Set.copyOf(employeeNoCandidates), null);

		return Context.builder()
			.duplicateUsernamesInFile(usernamesInFile)
			.duplicateEmailsInFile(emailsInFile)
			.duplicatePhonesInFile(phonesInFile)
			.duplicateEmployeeNosInFile(employeeNosInFile)
			.existingUsernames(existing.usernames())
			.existingEmails(existing.emails())
			.existingPhones(existing.phones())
			.existingEmployeeNos(existing.employeeNos())
			.build();
	}

	@Override
	protected SysUserForm convertRow(SysUserImportRow row, int rowNum, List<ImportRowError> errors, Context context) {
		RowOutcome<UserParsedRow> extracted = UserRowExtractor.extract(row, rowNum);
		if (!extracted.ok()) {
			errors.addAll(extracted.errors());
			return null;
		}

		UserParsedRow parsed = extracted.value();
		List<ImportRowError> violations = UserRowValidator.validate(parsed, rowNum, context);
		if (!violations.isEmpty()) {
			errors.addAll(violations);
			return null;
		}

		SysUserForm form = new SysUserForm();
		form.setUsername(parsed.username());
		form.setNickname(parsed.nickname());
		form.setEmail(parsed.email());
		form.setPhone(parsed.phone());
		form.setEmployeeNo(parsed.employeeNo());
		form.setInitialPassword(parsed.initialPassword());
		form.setStatus(parsed.status());
		form.setGender(parsed.gender());
		form.setBirthday(parsed.birthday());
		form.setRemark(parsed.remark());
		return form;
	}

	@Override
	protected void saveBatch(List<SysUserForm> forms) {
		sysUserService.createBatchFromImport(forms);
	}

	@Value
	@Builder
	@Accessors(fluent = true)
	public static class Context {

		Set<String> duplicateUsernamesInFile;

		Set<String> duplicateEmailsInFile;

		Set<String> duplicatePhonesInFile;

		Set<String> duplicateEmployeeNosInFile;

		Set<String> existingUsernames;

		Set<String> existingEmails;

		Set<String> existingPhones;

		Set<String> existingEmployeeNos;

	}

}
