package com.auth.module.file.importer;

import cn.hutool.core.collection.CollUtil;
import com.auth.module.file.importer.model.ImportErrors;
import com.auth.module.file.importer.model.ImportRowError;
import com.auth.module.file.importer.model.SpreadsheetImportResult;
import com.auth.module.file.importer.reader.ExcelImportReader;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Excel 单 Sheet 批量导入骨架
 *
 * @param <R> Excel 行类型（含 @ExcelProperty 注解）
 * @param <F> 业务表单类型
 * @param <C> 单次导入上下文（由 {@link #prepareContext} 构建，线程安全）
 * @author Bunny
 */
public abstract class AbstractSheetImporter<R, F, C> {

	/**
	 * 默认单次导入最大行数，子类可 override 调整
	 */
	protected int maxRows() {
		return 500;
	}

	/**
	 * 行类型，供 {@link ExcelImportReader} 解析使用
	 * @return 行类型 Class
	 */
	protected abstract Class<R> rowType();

	/**
	 * 批量预加载本次导入所需的上下文（编码映射、重复项集合等）
	 * @param rows 已解析的所有行
	 * @return 单次导入上下文
	 */
	protected abstract C prepareContext(List<R> rows);

	/**
	 * 将单行 Excel 数据转为业务表单；校验失败时追加错误并返回 null
	 * @param row 原始行数据
	 * @param rowNum 行号（1-based，不含表头）
	 * @param errors 错误收集列表
	 * @param context 本次导入上下文
	 * @return 转换成功返回表单，失败返回 null
	 */
	protected abstract F convertRow(R row, int rowNum, List<ImportRowError> errors, C context);

	/**
	 * 批量写入，实现类应在事务内逐条调用业务 create 等方法
	 * @param forms 已通过校验的表单列表
	 */
	protected abstract void saveBatch(List<F> forms);

	/**
	 * 执行导入（模板方法，不可 override）
	 * @param file 上传文件
	 * @return 导入结果
	 * @throws IOException 文件读取失败
	 */
	public final SpreadsheetImportResult doImport(MultipartFile file) throws IOException {
		List<R> rows = new ExcelImportReader<>(rowType()).read(file);

		if (CollUtil.isEmpty(rows)) {
			return SpreadsheetImportResult.success(0);
		}

		int rowSize = CollUtil.size(rows);
		if (rowSize > maxRows()) {
			ImportRowError err = ImportErrors.rowLimitExceeded(maxRows());
			return SpreadsheetImportResult.withErrors(List.of(err));
		}

		List<ImportRowError> errors = new ArrayList<>();
		List<F> forms = new ArrayList<>(rowSize);
		C context = prepareContext(rows);

		for (int i = 0; i < rowSize; i++) {
			F form = convertRow(rows.get(i), i + 1, errors, context);
			if (form != null) {
				forms.add(form);
			}
		}

		if (CollUtil.isNotEmpty(errors)) {
			return SpreadsheetImportResult.withErrors(errors);
		}

		saveBatch(forms);
		return SpreadsheetImportResult.success(forms.size());
	}

}
