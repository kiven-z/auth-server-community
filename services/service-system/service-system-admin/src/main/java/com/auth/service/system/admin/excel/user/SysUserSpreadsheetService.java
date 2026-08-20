package com.auth.service.system.admin.excel.user;

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
 * 用户 Excel 导入
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Service
public class SysUserSpreadsheetService {

	private final UserSheetImporter userSheetImporter;

	/**
	 * 从上传文件批量导入用户（解析 → 校验 → 写入，全部通过才落库）
	 * @param file 上传的 Excel 文件
	 * @return 导入结果（含行级错误）
	 * @throws IOException 文件读取失败
	 */
	@Transactional(rollbackFor = Exception.class)
	public SpreadsheetImportResult importExcel(MultipartFile file) throws IOException {
		return userSheetImporter.doImport(file);
	}

	/**
	 * 生成用户导入模板并返回文件响应
	 * @return 模板文件响应
	 * @throws IOException 写入失败
	 */
	public ResponseEntity<byte[]> downloadImportTemplate() throws IOException {
		SysUserImportRow row = new SysUserImportRow();
		row.setUsername("（示例：zhangsan，全局唯一）");
		row.setNickname("（示例：张三）");
		row.setEmail("（示例：zhangsan@example.com，全局唯一）");
		row.setPhone("（示例：13800000000，全局唯一）");
		row.setEmployeeNo("（可选，示例：E001，全局唯一）");
		row.setStatusLabel("启用");
		row.setInitialPassword("（示例：Abcd1234，8-18位）");
		row.setGenderLabel("（可选，男/女/未知）");
		row.setBirthday("（可选，示例：1990-01-01）");
		row.setRemark("（可选）");
		byte[] bytes = new ExcelExportWriter<>(SysUserImportRow.class, "用户导入模板").write(List.of(row));
		return FileDelivery.deliver(bytes, "user_import_template.xlsx");
	}

}
