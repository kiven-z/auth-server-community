package com.auth.service.system.admin.excel.dept;

import com.auth.module.file.importer.AbstractSheetImporter;
import com.auth.module.file.importer.model.ImportRowError;
import com.auth.module.file.importer.model.RowOutcome;
import com.auth.service.system.admin.excel.EnableStatus;
import com.auth.service.system.admin.excel.EnableStatusLabels;
import com.auth.service.system.admin.excel.ImportRowSupport;
import com.auth.service.system.admin.mapper.admin.dept.SysDeptMapper;
import com.auth.service.system.admin.model.entity.SysDeptEntity;
import com.auth.service.system.admin.model.form.dept.SysDeptForm;
import com.auth.service.system.admin.service.admin.SysDeptService;
import com.auth.service.system.admin.support.dept.DeptLookupSupport;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.experimental.Accessors;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static com.auth.common.core.utils.TreeParentIdUtil.ROOT_PARENT_ID;

/**
 * 部门导入器：Extract → Validate → Map 三阶段编排。
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Component
public class DeptSheetImporter extends AbstractSheetImporter<SysDeptImportRow, SysDeptForm, DeptSheetImporter.Context> {

	private final SysDeptMapper sysDeptMapper;

	private final SysDeptService sysDeptService;

	private final DeptLookupSupport deptLookupSupport;

	@Override
	protected Class<SysDeptImportRow> rowType() {
		return SysDeptImportRow.class;
	}

	@Override
	protected Context prepareContext(List<SysDeptImportRow> rows) {
		Map<String, Long> parentIdByCode = deptLookupSupport.resolveIdsByCodes(rows,
				SysDeptImportRow::getParentDeptCode);
		ImportRowSupport.CodeUniqueness uniqueness = ImportRowSupport.prepareCodeUniqueness(rows,
				SysDeptImportRow::getDeptCode,
				codes -> sysDeptMapper.selectActiveByDeptCodes(codes)
					.stream()
					.map(SysDeptEntity::getDeptCode)
					.collect(Collectors.toSet()));
		return Context.builder()
			.parentIdByCode(parentIdByCode)
			.duplicateDeptCodesInFile(uniqueness.duplicatesInFile())
			.existingDeptCodes(uniqueness.existing())
			.build();
	}

	@Override
	protected SysDeptForm convertRow(SysDeptImportRow row, int rowNum, List<ImportRowError> errors, Context context) {
		RowOutcome<DeptParsedRow> extracted = DeptRowExtractor.extract(row, rowNum);
		if (!extracted.ok()) {
			errors.addAll(extracted.errors());
			return null;
		}

		DeptParsedRow parsed = extracted.value();
		List<ImportRowError> violations = DeptRowValidator.validate(parsed, rowNum, context);
		if (!violations.isEmpty()) {
			errors.addAll(violations);
			return null;
		}

		Long parentId = resolveParentId(parsed.parentDeptCode(), context);
		SysDeptForm form = new SysDeptForm();
		form.setParentId(Objects.requireNonNullElse(parentId, ROOT_PARENT_ID));
		form.setDeptCode(parsed.deptCode());
		form.setDeptName(parsed.deptName());
		form.setStatus(EnableStatusLabels.parseImport(parsed.statusLabel()) == EnableStatus.ENABLED);
		form.setOrderNum(parsed.orderNum());
		form.setRemark(parsed.remark());
		return form;
	}

	@Override
	protected void saveBatch(List<SysDeptForm> forms) {
		sysDeptService.createBatchFromImport(forms);
	}

	private Long resolveParentId(String parentDeptCode, Context context) {
		if (parentDeptCode == null) {
			return ROOT_PARENT_ID;
		}
		return context.parentIdByCode().get(parentDeptCode);
	}

	@Value
	@Builder
	@Accessors(fluent = true)
	public static class Context {

		Map<String, Long> parentIdByCode;

		Set<String> duplicateDeptCodesInFile;

		Set<String> existingDeptCodes;

	}

}
