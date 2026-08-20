package com.auth.service.system.admin.excel.permission;

import com.auth.module.file.importer.AbstractSheetImporter;
import com.auth.module.file.importer.model.ImportRowError;
import com.auth.module.file.importer.model.RowOutcome;
import com.auth.service.system.admin.excel.EnableStatus;
import com.auth.service.system.admin.excel.EnableStatusLabels;
import com.auth.service.system.admin.excel.ImportRowSupport;
import com.auth.service.system.admin.mapper.admin.permission.SysPermissionMapper;
import com.auth.service.system.admin.model.form.permission.SysPermissionForm;
import com.auth.service.system.admin.model.po.reference.PermissionReferencePO;
import com.auth.service.system.admin.service.admin.SysPermissionService;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.experimental.Accessors;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 权限导入器：Extract → Validate → Map 三阶段编排。
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Component
public class PermissionSheetImporter
		extends AbstractSheetImporter<SysPermissionImportRow, SysPermissionForm, PermissionSheetImporter.Context> {

	private final SysPermissionMapper sysPermissionMapper;

	private final SysPermissionService sysPermissionService;

	@Override
	protected Class<SysPermissionImportRow> rowType() {
		return SysPermissionImportRow.class;
	}

	@Override
	protected Context prepareContext(List<SysPermissionImportRow> rows) {
		ImportRowSupport.CodeUniqueness uniqueness = ImportRowSupport.prepareCodeUniqueness(rows,
				SysPermissionImportRow::getPermissionCode,
				codes -> sysPermissionMapper.selectReferenceByPermissionCodes(codes)
					.stream()
					.map(PermissionReferencePO::getPermissionCode)
					.collect(Collectors.toSet()));

		return Context.builder()
			.duplicatePermissionCodesInFile(uniqueness.duplicatesInFile())
			.existingPermissionCodes(uniqueness.existing())
			.build();
	}

	@Override
	protected SysPermissionForm convertRow(SysPermissionImportRow row, int rowNum, List<ImportRowError> errors,
			Context context) {
		RowOutcome<PermissionParsedRow> extracted = PermissionRowExtractor.extract(row, rowNum);
		if (!extracted.ok()) {
			errors.addAll(extracted.errors());
			return null;
		}

		PermissionParsedRow parsed = extracted.value();
		List<ImportRowError> violations = PermissionRowValidator.validate(parsed, rowNum, context);
		if (!violations.isEmpty()) {
			errors.addAll(violations);
			return null;
		}

		SysPermissionForm form = new SysPermissionForm();
		form.setPermissionCode(parsed.permissionCode());
		form.setPermissionName(parsed.permissionName());
		form.setStatus(EnableStatusLabels.parseImport(parsed.statusLabel()) == EnableStatus.ENABLED);
		form.setOrderNum(parsed.orderNum());
		form.setRemark(parsed.remark());
		return form;
	}

	@Override
	protected void saveBatch(List<SysPermissionForm> forms) {
		sysPermissionService.createBatchFromImport(forms);
	}

	@Value
	@Builder
	@Accessors(fluent = true)
	public static class Context {

		Set<String> duplicatePermissionCodesInFile;

		Set<String> existingPermissionCodes;

	}

}
