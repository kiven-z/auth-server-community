package com.auth.service.system.admin.excel.dept;

import com.auth.module.file.delivery.FileDelivery;
import com.auth.module.file.spreadsheet.ExcelExportWriter;
import com.auth.module.file.importer.model.SpreadsheetImportResult;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * 部门 Excel 导入
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Service
public class SysDeptSpreadsheetService {

	private final DeptSheetImporter deptSheetImporter;

	/**
	 * 从上传文件批量导入部门（解析 → 校验 → 写入，全部通过才落库）
	 * @param file 上传的 Excel 文件
	 * @return 导入结果（含行级错误）
	 * @throws IOException 文件读取失败
	 */
	@Transactional(rollbackFor = Exception.class)
	public SpreadsheetImportResult importExcel(MultipartFile file) throws IOException {
		return deptSheetImporter.doImport(file);
	}

	/**
	 * 生成部门导入模板并返回文件响应
	 * @return 模板文件响应
	 * @throws IOException 写入失败
	 */
	public ResponseEntity<byte[]> downloadImportTemplate() throws IOException {
		SysDeptImportRow row = new SysDeptImportRow();
		row.setParentDeptCode("（示例：ROOT，留空表示顶级部门）");
		row.setDeptCode("（示例：RD，全局唯一）");
		row.setDeptName("（示例：研发部）");
		row.setStatusLabel("启用");
		row.setOrderNum(1);
		row.setRemark("（可选）");
		byte[] bytes = new ExcelExportWriter<>(SysDeptImportRow.class, "部门导入模板").write(List.of(row));
		return FileDelivery.deliver(bytes, "dept_import_template.xlsx");
	}

}
