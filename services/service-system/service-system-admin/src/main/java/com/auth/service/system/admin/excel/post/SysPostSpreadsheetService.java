package com.auth.service.system.admin.excel.post;

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
 * 岗位 Excel 导入
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Service
public class SysPostSpreadsheetService {

	private final PostSheetImporter postSheetImporter;

	/**
	 * 从上传文件批量导入岗位（解析 → 校验 → 写入，全部通过才落库）
	 * @param file 上传的 Excel 文件
	 * @return 导入结果（含行级错误）
	 * @throws IOException 文件读取失败
	 */
	@Transactional(rollbackFor = Exception.class)
	public SpreadsheetImportResult importExcel(MultipartFile file) throws IOException {
		return postSheetImporter.doImport(file);
	}

	/**
	 * 生成岗位导入模板并返回文件响应
	 * @return 模板文件响应
	 * @throws IOException 写入失败
	 */
	public ResponseEntity<byte[]> downloadImportTemplate() throws IOException {
		SysPostImportRow row = new SysPostImportRow();
		row.setDeptCode("（示例：RD，部门必须已存在）");
		row.setPostCode("（示例：P01，同部门内唯一）");
		row.setPostName("（示例：高级工程师）");
		row.setStatusLabel("启用");
		row.setOrderNum(1);
		row.setRemark("（可选）");
		byte[] bytes = new ExcelExportWriter<>(SysPostImportRow.class, "岗位导入模板").write(List.of(row));
		return FileDelivery.deliver(bytes, "post_import_template.xlsx");
	}

}
