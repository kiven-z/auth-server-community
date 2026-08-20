package com.auth.module.file.spreadsheet;

import com.alibaba.excel.EasyExcelFactory;
import com.alibaba.excel.write.builder.ExcelWriterBuilder;
import com.alibaba.excel.write.handler.CellWriteHandler;
import com.alibaba.excel.write.style.column.LongestMatchColumnWidthStyleStrategy;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Excel 文件写入器：将行数据序列化为 xlsx 字节
 *
 * @param <T> 导出行类型
 * @author Bunny
 */
public class ExcelExportWriter<T> {

	private final Class<T> clazz;

	private final String sheetName;

	private final List<CellWriteHandler> writeHandlers;

	/**
	 * @param clazz 导出行类型
	 * @param sheetName 工作表名称
	 */
	public ExcelExportWriter(Class<T> clazz, String sheetName) {
		this.clazz = clazz;
		this.sheetName = sheetName;
		this.writeHandlers = new ArrayList<>();
	}

	/**
	 * 注册单元格写入处理器
	 * @param writeHandler 处理器
	 * @return 当前实例
	 */
	public ExcelExportWriter<T> registerWriteHandler(CellWriteHandler writeHandler) {
		this.writeHandlers.add(writeHandler);
		return this;
	}

	/**
	 * 将数据写入 Excel 并返回字节数组
	 * @param data 导出行列表
	 * @return xlsx 字节
	 * @throws IOException 写入失败
	 */
	public byte[] write(List<T> data) throws IOException {
		try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
			ExcelWriterBuilder excelWriter = EasyExcelFactory.write(outputStream, clazz)
				.registerWriteHandler(new LongestMatchColumnWidthStyleStrategy());
			for (CellWriteHandler handler : writeHandlers) {
				excelWriter.registerWriteHandler(handler);
			}
			excelWriter.sheet(sheetName).doWrite(data);
			return outputStream.toByteArray();
		}
		catch (Exception e) {
			throw new IOException("Excel export failed", e);
		}
	}

}
