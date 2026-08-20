package com.auth.service.system.admin.excel.user;

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
 * {@link SysUserSpreadsheetService} 单元测试
 *
 * @author Bunny
 */
@DisplayName("SysUserSpreadsheetService 用户导入")
@ExtendWith(MockitoExtension.class)
class SysUserSpreadsheetServiceTest {

	@Mock
	private UserSheetImporter userSheetImporter;

	private SysUserSpreadsheetService spreadsheetService;

	@BeforeEach
	void setUp() {
		spreadsheetService = new SysUserSpreadsheetService(userSheetImporter);
	}

	@Test
	@DisplayName("导入模板下载返回非空 Excel 且表头与示例行正确")
	void downloadImportTemplate_returnsTemplateWithExpectedHeaders() throws IOException {
		ResponseEntity<byte[]> response = spreadsheetService.downloadImportTemplate();

		assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
		assertThat(response.getBody()).isNotNull().isNotEmpty();
		assertThat(response.getHeaders().getContentDisposition().getFilename()).isEqualTo("user_import_template.xlsx");

		try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(response.getBody()))) {
			Sheet sheet = workbook.getSheetAt(0);
			assertThat(sheet.getSheetName()).isEqualTo("用户导入模板");
			Row header = sheet.getRow(0);
			assertThat(header.getCell(0).getStringCellValue()).isEqualTo("用户名");
			assertThat(header.getCell(1).getStringCellValue()).isEqualTo("昵称");
			assertThat(header.getCell(2).getStringCellValue()).isEqualTo("邮箱");
			assertThat(header.getCell(3).getStringCellValue()).isEqualTo("手机号");
			assertThat(header.getCell(4).getStringCellValue()).isEqualTo("工号");
			assertThat(header.getCell(5).getStringCellValue()).isEqualTo("状态");
			assertThat(header.getCell(6).getStringCellValue()).isEqualTo("初始密码");
			assertThat(header.getCell(7).getStringCellValue()).isEqualTo("性别");
			assertThat(header.getCell(8).getStringCellValue()).isEqualTo("出生日期");
			assertThat(header.getCell(9).getStringCellValue()).isEqualTo("备注");

			Row sample = sheet.getRow(1);
			assertThat(sample.getCell(0).getStringCellValue()).contains("zhangsan");
			assertThat(sample.getCell(1).getStringCellValue()).contains("张三");
			assertThat(sample.getCell(2).getStringCellValue()).contains("zhangsan@example.com");
			assertThat(sample.getCell(3).getStringCellValue()).contains("13800000000");
			assertThat(sample.getCell(4).getStringCellValue()).contains("E001");
			assertThat(sample.getCell(5).getStringCellValue()).isEqualTo("启用");
			assertThat(sample.getCell(6).getStringCellValue()).contains("Abcd1234");
			assertThat(sample.getCell(7).getStringCellValue()).contains("男/女/未知");
			assertThat(sample.getCell(8).getStringCellValue()).contains("1990-01-01");
			assertThat(sample.getCell(9).getStringCellValue()).isEqualTo("（可选）");
		}
	}

}
