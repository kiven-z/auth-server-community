package com.auth.service.system.admin.excel.role;

import com.auth.service.system.admin.mapper.admin.role.SysRoleMapper;
import com.auth.service.system.admin.service.admin.SysRoleService;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link SysRoleSpreadsheetService} 单元测试
 *
 * @author Bunny
 */
@DisplayName("SysRoleSpreadsheetService 角色导入")
@ExtendWith(MockitoExtension.class)
class SysRoleSpreadsheetServiceTest {

	@Mock
	private SysRoleMapper sysRoleMapper;

	@Mock
	private SysRoleService sysRoleService;

	private SysRoleSpreadsheetService spreadsheetService;

	@BeforeEach
	void setUp() {
		RoleSheetImporter roleSheetImporter = new RoleSheetImporter(sysRoleMapper, sysRoleService);
		spreadsheetService = new SysRoleSpreadsheetService(roleSheetImporter);
	}

	@Test
	@DisplayName("导入模板下载返回非空 Excel 且表头与示例行正确")
	void downloadImportTemplate_returnsTemplateWithExpectedHeaders() throws IOException {
		ResponseEntity<byte[]> response = spreadsheetService.downloadImportTemplate();

		assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
		assertThat(response.getBody()).isNotNull().isNotEmpty();
		assertThat(response.getHeaders().getContentDisposition().getFilename()).isEqualTo("role_import_template.xlsx");

		try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(response.getBody()))) {
			Sheet sheet = workbook.getSheetAt(0);
			assertThat(sheet.getSheetName()).isEqualTo("角色导入模板");
			Row header = sheet.getRow(0);
			assertThat(header.getCell(0).getStringCellValue()).isEqualTo("角色编码");
			assertThat(header.getCell(1).getStringCellValue()).isEqualTo("角色名称");
			assertThat(header.getCell(2).getStringCellValue()).isEqualTo("状态");
			assertThat(header.getCell(3).getStringCellValue()).isEqualTo("显示顺序");
			assertThat(header.getCell(4).getStringCellValue()).isEqualTo("备注");

			Row sample = sheet.getRow(1);
			assertThat(sample.getCell(0).getStringCellValue()).contains("ADMIN");
			assertThat(sample.getCell(1).getStringCellValue()).contains("系统管理员");
			assertThat(sample.getCell(2).getStringCellValue()).isEqualTo("启用");
			assertThat(sample.getCell(3).getNumericCellValue()).isEqualTo(1.0);
			assertThat(sample.getCell(4).getStringCellValue()).isEqualTo("（可选）");
		}
	}

}
