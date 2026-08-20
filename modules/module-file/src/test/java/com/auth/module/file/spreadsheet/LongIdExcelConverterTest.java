package com.auth.module.file.spreadsheet;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.metadata.GlobalConfiguration;
import com.alibaba.excel.metadata.data.WriteCellData;
import lombok.Getter;
import lombok.Setter;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link LongIdExcelConverter} 单元测试
 *
 * @author Bunny
 */
@DisplayName("LongIdExcelConverter 长整型 ID 文本导出")
class LongIdExcelConverterTest {

	/**
	 * Snowflake 样例 ID（超过 Excel 数值 15 位精度）
	 */
	private static final long SNOWFLAKE_ID = 2_061_699_478_481_860_001L;

	private final LongIdExcelConverter converter = new LongIdExcelConverter();

	@Test
	@DisplayName("convertToExcelData 将 Long 转为完整字符串")
	void convertToExcelData_whenSnowflakeId_returnsFullString() {
		WriteCellData<?> cellData = converter.convertToExcelData(SNOWFLAKE_ID, null, new GlobalConfiguration());

		assertThat(cellData.getStringValue()).isEqualTo("2061699478481860001");
	}

	@Test
	@DisplayName("convertToExcelData 在 null 时返回空串")
	void convertToExcelData_whenNull_returnsEmptyString() {
		WriteCellData<?> cellData = converter.convertToExcelData(null, null, new GlobalConfiguration());

		assertThat(cellData.getStringValue()).isEmpty();
	}

	@Test
	@DisplayName("Excel 写出后单元格为字符串类型且 ID 无精度丢失")
	void writeExcel_whenIdFieldUsesConverter_cellIsStringWithoutPrecisionLoss() throws IOException {
		IdExportTestRow row = new IdExportTestRow();
		row.setId(SNOWFLAKE_ID);

		byte[] bytes = new ExcelExportWriter<>(IdExportTestRow.class, "测试").write(List.of(row));

		try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(bytes))) {
			Sheet sheet = workbook.getSheetAt(0);
			Row dataRow = sheet.getRow(1);
			Cell idCell = dataRow.getCell(0);

			assertThat(idCell.getCellType()).isEqualTo(CellType.STRING);
			assertThat(idCell.getStringCellValue()).isEqualTo("2061699478481860001");
		}
	}

	@Getter
	@Setter
	private static class IdExportTestRow {

		@ExcelProperty(value = "主键ID", index = 0, converter = LongIdExcelConverter.class)
		private Long id;

	}

}
