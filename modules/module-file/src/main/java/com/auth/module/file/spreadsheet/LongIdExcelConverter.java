package com.auth.module.file.spreadsheet;

import com.alibaba.excel.converters.Converter;
import com.alibaba.excel.enums.CellDataTypeEnum;
import com.alibaba.excel.metadata.GlobalConfiguration;
import com.alibaba.excel.metadata.data.ReadCellData;
import com.alibaba.excel.metadata.data.WriteCellData;
import com.alibaba.excel.metadata.property.ExcelContentProperty;

/**
 * 长整型主键导出为 Excel 文本，避免 Snowflake ID 被数值格式截断或显示为科学计数法。
 *
 * @author Bunny
 */
public class LongIdExcelConverter implements Converter<Long> {

	@Override
	public Class<Long> supportJavaTypeKey() {
		return Long.class;
	}

	@Override
	public CellDataTypeEnum supportExcelTypeKey() {
		return CellDataTypeEnum.STRING;
	}

	@Override
	public Long convertToJavaData(ReadCellData<?> cellData, ExcelContentProperty contentProperty,
			GlobalConfiguration globalConfiguration) {
		String text = cellData.getStringValue();
		if (text == null || text.isBlank()) {
			return null;
		}
		return Long.parseLong(text.trim());
	}

	@Override
	public WriteCellData<?> convertToExcelData(Long value, ExcelContentProperty contentProperty,
			GlobalConfiguration globalConfiguration) {
		if (value == null) {
			return new WriteCellData<>("");
		}
		return new WriteCellData<>(String.valueOf(value));
	}

}
