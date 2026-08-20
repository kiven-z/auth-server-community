package com.auth.service.system.admin.excel.post;

import com.auth.module.file.importer.AbstractSheetImporter;
import com.auth.module.file.importer.model.ImportRowError;
import com.auth.module.file.importer.model.RowOutcome;
import com.auth.module.file.importer.parse.ImportTextParsers;
import com.auth.service.system.admin.excel.EnableStatus;
import com.auth.service.system.admin.excel.EnableStatusLabels;
import com.auth.service.system.admin.mapper.admin.post.SysPostMapper;
import com.auth.service.system.admin.model.form.post.SysPostForm;
import com.auth.service.system.admin.service.admin.SysPostService;
import com.auth.service.system.admin.support.dept.DeptLookupSupport;
import com.auth.service.system.admin.support.post.PostWriteGuard;
import com.auth.service.system.admin.support.post.PostWriteGuard.ImportPrecheck;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.experimental.Accessors;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 岗位导入器：Extract → Validate → Map 三阶段编排。
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Component
public class PostSheetImporter extends AbstractSheetImporter<SysPostImportRow, SysPostForm, PostSheetImporter.Context> {

	private static final String FIELD_DEPT_CODE = "deptCode";

	private static final String FIELD_POST_CODE = "postCode";

	private static final String FIELD_POST_NAME = "postName";

	private final DeptLookupSupport deptLookupSupport;

	private final SysPostMapper sysPostMapper;

	private final SysPostService sysPostService;

	/**
	 * 从 Excel 行提取并解析字段
	 * @param row Excel 行
	 * @param rowNum 行号
	 * @return 解析结果
	 */
	public static RowOutcome<PostParsedRow> extract(SysPostImportRow row, int rowNum) {
		RowOutcome<String> deptCode = ImportTextParsers.require(row.getDeptCode(), rowNum, FIELD_DEPT_CODE);
		if (!deptCode.ok()) {
			return RowOutcome.err(deptCode.errors());
		}
		RowOutcome<String> postCode = ImportTextParsers.require(row.getPostCode(), rowNum, FIELD_POST_CODE);
		if (!postCode.ok()) {
			return RowOutcome.err(postCode.errors());
		}
		RowOutcome<String> postName = ImportTextParsers.require(row.getPostName(), rowNum, FIELD_POST_NAME);
		if (!postName.ok()) {
			return RowOutcome.err(postName.errors());
		}

		return RowOutcome.ok(PostParsedRow.builder()
			.deptCode(deptCode.value())
			.postCode(postCode.value())
			.postName(postName.value())
			.statusLabel(row.getStatusLabel())
			.orderNum(row.getOrderNum())
			.remark(row.getRemark())
			.build());
	}

	@Override
	protected Class<SysPostImportRow> rowType() {
		return SysPostImportRow.class;
	}

	@Override
	protected Context prepareContext(List<SysPostImportRow> rows) {
		Map<String, Long> deptIdByCode = deptLookupSupport.resolveIdsByCodes(rows, SysPostImportRow::getDeptCode);
		ImportPrecheck precheck = PostWriteGuard.precheckForImport(sysPostMapper, rows, deptIdByCode);

		return Context.builder()
			.deptIdByCode(deptIdByCode)
			.unassignableDeptCodes(precheck.getUnassignableDeptCodes())
			.duplicateCompositeKeysInFile(precheck.getDuplicateCompositeKeysInFile())
			.existingCompositeKeys(precheck.getExistingCompositeKeys())
			.build();
	}

	@Override
	protected SysPostForm convertRow(SysPostImportRow row, int rowNum, List<ImportRowError> errors, Context context) {
		RowOutcome<PostParsedRow> extracted = extract(row, rowNum);
		if (!extracted.ok()) {
			errors.addAll(extracted.errors());
			return null;
		}

		PostParsedRow parsed = extracted.value();
		List<ImportRowError> violations = PostRowValidator.validate(parsed, rowNum, context);
		if (!violations.isEmpty()) {
			errors.addAll(violations);
			return null;
		}

		Long deptId = context.deptIdByCode().get(parsed.deptCode());
		SysPostForm form = new SysPostForm();
		form.setDeptId(deptId);
		form.setPostCode(parsed.postCode());
		form.setPostName(parsed.postName());
		form.setStatus(EnableStatusLabels.parseImport(parsed.statusLabel()) == EnableStatus.ENABLED);
		form.setOrderNum(parsed.orderNum());
		form.setRemark(parsed.remark());
		return form;
	}

	@Override
	protected void saveBatch(List<SysPostForm> forms) {
		sysPostService.createBatchFromImport(forms);
	}

	@Value
	@Builder
	@Accessors(fluent = true)
	public static class Context {

		Map<String, Long> deptIdByCode;

		Set<String> unassignableDeptCodes;

		Set<String> duplicateCompositeKeysInFile;

		Set<String> existingCompositeKeys;

	}

}
