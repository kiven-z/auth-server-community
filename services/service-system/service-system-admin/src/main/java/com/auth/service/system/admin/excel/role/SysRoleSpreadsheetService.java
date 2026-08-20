package com.auth.service.system.admin.excel.role;

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
 * 角色 Excel 导入
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Service
public class SysRoleSpreadsheetService {

	private final RoleSheetImporter roleSheetImporter;

	/**
	 * 从上传文件批量导入角色（解析 → 校验 → 写入，全部通过才落库）
	 * @param file 上传的 Excel 文件
	 * @return 导入结果（含行级错误）
	 * @throws IOException 文件读取失败
	 */
	@Transactional(rollbackFor = Exception.class)
	public SpreadsheetImportResult importExcel(MultipartFile file) throws IOException {
		return roleSheetImporter.doImport(file);
	}

	/**
	 * 生成角色导入模板并返回文件响应
	 * @return 模板文件响应
	 * @throws IOException 写入失败
	 */
	public ResponseEntity<byte[]> downloadImportTemplate() throws IOException {
		SysRoleImportRow row = new SysRoleImportRow();
		row.setRoleCode("（示例：ADMIN，全大写+下划线，首字符大写字母）");
		row.setRoleName("（示例：系统管理员）");
		row.setStatusLabel("启用");
		row.setOrderNum(1);
		row.setRemark("（可选）");
		byte[] bytes = new ExcelExportWriter<>(SysRoleImportRow.class, "角色导入模板").write(List.of(row));
		return FileDelivery.deliver(bytes, "role_import_template.xlsx");
	}

}
