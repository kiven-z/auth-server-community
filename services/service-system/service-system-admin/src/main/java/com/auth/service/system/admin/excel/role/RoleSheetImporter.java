package com.auth.service.system.admin.excel.role;

import com.auth.module.file.importer.AbstractSheetImporter;
import com.auth.module.file.importer.model.ImportRowError;
import com.auth.module.file.importer.model.RowOutcome;
import com.auth.service.system.admin.excel.EnableStatus;
import com.auth.service.system.admin.excel.EnableStatusLabels;
import com.auth.service.system.admin.excel.ImportRowSupport;
import com.auth.service.system.admin.mapper.admin.role.SysRoleMapper;
import com.auth.service.system.admin.model.form.role.SysRoleForm;
import com.auth.service.system.admin.model.po.reference.RoleReferencePO;
import com.auth.service.system.admin.service.admin.SysRoleService;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.experimental.Accessors;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 角色导入器：Extract → Validate → Map 三阶段编排。
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Component
public class RoleSheetImporter extends AbstractSheetImporter<SysRoleImportRow, SysRoleForm, RoleSheetImporter.Context> {

	private final SysRoleMapper sysRoleMapper;

	private final SysRoleService sysRoleService;

	@Override
	protected Class<SysRoleImportRow> rowType() {
		return SysRoleImportRow.class;
	}

	@Override
	protected Context prepareContext(List<SysRoleImportRow> rows) {
		ImportRowSupport.CodeUniqueness uniqueness = ImportRowSupport.prepareCodeUniqueness(rows,
				SysRoleImportRow::getRoleCode,
				codes -> sysRoleMapper.selectReferenceByRoleCodes(codes)
					.stream()
					.map(RoleReferencePO::getRoleCode)
					.collect(Collectors.toSet()));
		return Context.builder()
			.duplicateRoleCodesInFile(uniqueness.duplicatesInFile())
			.existingRoleCodes(uniqueness.existing())
			.build();
	}

	@Override
	protected SysRoleForm convertRow(SysRoleImportRow row, int rowNum, List<ImportRowError> errors, Context context) {
		RowOutcome<RoleParsedRow> extracted = RoleRowExtractor.extract(row, rowNum);
		if (!extracted.ok()) {
			errors.addAll(extracted.errors());
			return null;
		}

		RoleParsedRow parsed = extracted.value();
		List<ImportRowError> violations = RoleRowValidator.validate(parsed, rowNum, context);
		if (!violations.isEmpty()) {
			errors.addAll(violations);
			return null;
		}

		SysRoleForm form = new SysRoleForm();
		form.setRoleCode(parsed.roleCode());
		form.setRoleName(parsed.roleName());
		form.setStatus(EnableStatusLabels.parseImport(parsed.statusLabel()) == EnableStatus.ENABLED);
		form.setOrderNum(parsed.orderNum());
		form.setRemark(parsed.remark());
		return form;
	}

	@Override
	protected void saveBatch(List<SysRoleForm> forms) {
		sysRoleService.createBatchFromImport(forms);
	}

	@Value
	@Builder
	@Accessors(fluent = true)
	public static class Context {

		Set<String> duplicateRoleCodesInFile;

		Set<String> existingRoleCodes;

	}

}