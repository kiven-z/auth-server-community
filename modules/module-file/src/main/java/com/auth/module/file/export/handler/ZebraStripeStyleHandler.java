package com.auth.module.file.export.handler;

import com.alibaba.excel.metadata.Head;
import com.alibaba.excel.metadata.data.DataFormatData;
import com.alibaba.excel.metadata.data.WriteCellData;
import com.alibaba.excel.write.handler.CellWriteHandler;
import com.alibaba.excel.write.metadata.holder.WriteSheetHolder;
import com.alibaba.excel.write.metadata.holder.WriteTableHolder;
import com.auth.module.file.export.model.ZebraStripeStyle;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 斑马纹样式处理器 用于在 Excel 导出时为单元格应用斑马纹样式，包括交替行颜色、特殊列高亮等功能
 *
 * @author Bunny
 */
public class ZebraStripeStyleHandler implements CellWriteHandler {

	private final ZebraStripeStyle styleConfig;

	/**
	 * 单次写出会话内的样式缓存；POI {@link CellStyle} 绑定所属 {@link Workbook}，禁止跨工作簿复用
	 */
	private final Map<String, CellStyle> styleCache = new HashMap<>();

	/**
	 * 当前缓存绑定的工作簿；导出服务会复用同一处理器，新工作簿写出前清空缓存
	 */
	private Workbook boundWorkbook;

	/**
	 * @param styleConfig 斑马纹样式配置对象，定义颜色、字体等样式参数
	 */
	public ZebraStripeStyleHandler(ZebraStripeStyle styleConfig) {
		this.styleConfig = styleConfig;
	}

	/**
	 * 单元格处理完成后的回调方法 在此方法中为每个单元格应用斑马纹样式
	 * @param writeSheetHolder 工作表持有器，用于获取工作簿和 worksheet 信息
	 * @param writeTableHolder 表格持有器，当前处理的表格对象（可选）
	 * @param cellDataList 单元格数据列表，包含单元格的原始数据和样式信息
	 * @param cell 当前正在处理的单元格对象
	 * @param head 表头信息，包含列头定义
	 * @param relativeRowIndex 相对行索引，当前行在数据中的位置（从 0 开始）
	 * @param isHead 是否为表头标识，true 表示当前行为表头行
	 */
	@Override
	public void afterCellDispose(WriteSheetHolder writeSheetHolder, WriteTableHolder writeTableHolder,
			List<WriteCellData<?>> cellDataList, Cell cell, Head head, Integer relativeRowIndex, Boolean isHead) {

		// 获取工作簿对象用于创建样式
		Workbook workbook = writeSheetHolder.getSheet().getWorkbook();

		// 确定当前行索引，如果为 null 则默认为 0
		int rowIdx = Objects.requireNonNullElse(relativeRowIndex, 0);

		// 获取当前列索引
		int colIdx = cell.getColumnIndex();

		// 1. 确定背景颜色
		byte[] rgbColor = determineBackgroundColor(colIdx, rowIdx, isHead);

		// 2. 获取原始数据格式 (解决时间变数字的关键)
		// 默认常规格式
		short dataFormat = 0;
		if (cellDataList != null && !cellDataList.isEmpty()) {
			WriteCellData<?> cellData = cellDataList.get(0);
			if (cellData.getWriteCellStyle() != null) {
				DataFormatData dfd = cellData.getWriteCellStyle().getDataFormatData();
				if (dfd != null) {
					if (dfd.getIndex() != null) {
						dataFormat = dfd.getIndex();
					}
					else if (dfd.getFormat() != null) {
						String format = dfd.getFormat();
						dataFormat = workbook.createDataFormat().getFormat(format);
					}
				}
			}
		}

		// 3. 工作簿切换时丢弃旧缓存，避免 CellStyle 跨 Workbook 污染
		if (boundWorkbook != workbook) {
			styleCache.clear();
			boundWorkbook = workbook;
		}

		// 4. 从缓存获取或创建样式
		String cacheKey = String.format("%d_%d_%d_%d_%b", rgbColor[0] & 0xFF, rgbColor[1] & 0xFF, rgbColor[2] & 0xFF,
				dataFormat, isHead);
		short finalDataFormat = dataFormat;
		CellStyle finalStyle = styleCache.computeIfAbsent(cacheKey,
				k -> createBaseStyle(workbook, rgbColor, finalDataFormat, isHead));

		// 5. 应用样式并清理
		cell.setCellStyle(finalStyle);
		if (cellDataList != null && !cellDataList.isEmpty()) {
			cellDataList.get(0).setWriteCellStyle(null);
		}
	}

	/**
	 * 确定单元格背景颜色 根据行列位置和是否为表头来决定使用哪种颜色
	 * @param colIdx 当前列索引
	 * @param rowIdx 当前行索引
	 * @param isHead 是否为表头标识
	 * @return RGB 颜色字节数组，表示确定的背景颜色
	 */
	private byte[] determineBackgroundColor(int colIdx, int rowIdx, boolean isHead) {
		final int i = 2;

		// 表头行：偶数列使用表头颜色，奇数列使用奇数列颜色
		if (isHead) {
			return (colIdx % i == 0) ? styleConfig.getHeaderColor() : styleConfig.getOddColumnColor();
		}

		// 特殊列：使用特殊列颜色高亮显示
		if (colIdx == styleConfig.getSpecialColumnIndex()) {
			return styleConfig.getSpecialColumnColor();
		}

		// 斑马纹逻辑简化
		// 偶数列：偶数行使用偶数列颜色，奇数行使用浅蓝色
		if (colIdx % i == 0) {
			return (rowIdx % i == 0) ? styleConfig.getEvenColumnColor() : styleConfig.getLightBlueColor();
		}
		// 奇数列：偶数行使用奇数列颜色，奇数行使用浅灰色
		else {
			return (rowIdx % i == 0) ? styleConfig.getOddColumnColor() : styleConfig.getLightGreyColor();
		}
	}

	/**
	 * 创建基础单元格样式 设置背景色、边框、数据格式、字体和对齐方式等属性
	 * @param workbook 工作簿对象，用于创建样式和字体
	 * @param rgb 背景颜色 RGB 字节数组
	 * @param dataFormat 数据格式索引，控制单元格内容的显示格式
	 * @param isHead 是否为表头标识，影响字体大小和对齐方式
	 * @return 创建完成的单元格样式对象
	 */
	private CellStyle createBaseStyle(Workbook workbook, byte[] rgb, short dataFormat, boolean isHead) {
		XSSFCellStyle style = (XSSFCellStyle) workbook.createCellStyle();

		// 设置背景填充颜色和填充模式
		style.setFillForegroundColor(new XSSFColor(rgb, null));
		style.setFillPattern(FillPatternType.SOLID_FOREGROUND);

		// 设置四周边框样式
		style.setBorderBottom(BorderStyle.THIN);
		style.setBorderLeft(BorderStyle.THIN);
		style.setBorderRight(BorderStyle.THIN);
		style.setBorderTop(BorderStyle.THIN);
		style.setBottomBorderColor(styleConfig.getBorderColorIndex());

		// 设置格式（时间正常显示的关键）
		style.setDataFormat(dataFormat);

		// 创建并设置字体样式
		Font font = workbook.createFont();
		font.setFontName(styleConfig.getFontName());
		font.setFontHeightInPoints(isHead ? styleConfig.getHeaderFontSize() : styleConfig.getDataFontSize());
		font.setBold(isHead);
		style.setFont(font);

		// 设置对齐方式：表头居中，数据左对齐
		style.setAlignment(isHead ? HorizontalAlignment.CENTER : HorizontalAlignment.LEFT);
		style.setVerticalAlignment(VerticalAlignment.CENTER);
		return style;
	}

}
