package com.auth.module.file.importer.reader;

import com.alibaba.excel.EasyExcelFactory;
import com.alibaba.excel.read.listener.PageReadListener;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 通用 Excel 导入读取器：将上传文件的第一个 Sheet 解析为行对象列表。
 *
 * @param <T> 行类型（需包含 EasyExcel 注解）
 * @author Bunny
 */
public class ExcelImportReader<T> {

	/**
	 * 每次触发回调的批次大小
	 */
	private static final int BATCH_SIZE = 100;

	private final Class<T> clazz;

	/**
	 * @param clazz 行类型
	 */
	public ExcelImportReader(Class<T> clazz) {
		this.clazz = clazz;
	}

	/**
	 * 读取上传文件的第一个 Sheet，返回所有行
	 * @param file 上传文件
	 * @return 行列表（不含表头）
	 * @throws IOException 读取失败
	 */
	public List<T> read(MultipartFile file) throws IOException {
		List<T> rows = new ArrayList<>();
		try {
			EasyExcelFactory.read(file.getInputStream(), clazz, new PageReadListener<T>(rows::addAll, BATCH_SIZE))
				.sheet()
				.doRead();
		}
		catch (Exception e) {
			throw new IOException("Excel import read failed", e);
		}
		return rows;
	}

}
