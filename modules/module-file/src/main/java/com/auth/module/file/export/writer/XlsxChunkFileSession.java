package com.auth.module.file.export.writer;

import com.alibaba.excel.EasyExcelFactory;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.builder.ExcelWriterBuilder;
import com.alibaba.excel.write.handler.CellWriteHandler;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.alibaba.excel.write.style.column.LongestMatchColumnWidthStyleStrategy;
import com.auth.module.file.export.definition.ExportColumn;
import com.auth.module.file.export.model.enums.ZebraStripeStyleType;
import com.auth.module.file.export.shard.ChunkFileSession;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/**
 * 单个 XLSX 分片写入会话
 *
 * <p>
 * 会话状态：{@code OPEN → COMPLETED}（成功）或 {@code OPEN → CLOSED}（失败/放弃）。 仅在首次写入时才初始化
 * {@link ExcelWriter}；0 行时 {@link #complete()} 产出仅含表头的合法文件
 *
 * @author Bunny
 */
final class XlsxChunkFileSession implements ChunkFileSession<Map<String, Object>> {

	private final Path tempFile;

	private final OutputStream outputStream;

	private final String sheetName;

	private final List<ExportColumn> columns;

	private final List<CellWriteHandler> writeHandlers;

	private ExcelWriter excelWriter;

	private WriteSheet writeSheet;

	private State state = State.OPEN;

	XlsxChunkFileSession(Path tempFile, String sheetName, List<ExportColumn> columns,
			List<CellWriteHandler> writeHandlers) throws IOException {
		this.tempFile = tempFile;
		this.outputStream = Files.newOutputStream(tempFile);
		this.sheetName = sheetName;
		this.columns = columns;
		this.writeHandlers = writeHandlers;
	}

	@Override
	public void writeChunk(List<Map<String, Object>> rows) throws IOException {
		assertOpen();
		if (rows == null || rows.isEmpty()) {
			return;
		}
		ensureWriter();
		try {
			excelWriter.write(MapExcelRowSupport.toCellRows(rows, columns), writeSheet);
		}
		catch (RuntimeException exception) {
			throw new IOException("Excel write failed", exception);
		}
	}

	@Override
	public Path complete() throws IOException {
		assertOpen();
		try {
			finishWriter();
			outputStream.close();
			state = State.COMPLETED;
			return tempFile;
		}
		catch (IOException | RuntimeException exception) {
			closeQuietly();
			TempFileSupport.deleteQuietly(tempFile);
			throw exception;
		}
	}

	@Override
	public void close() {
		if (state != State.OPEN) {
			return;
		}
		closeQuietly();
		TempFileSupport.deleteQuietly(tempFile);
	}

	private void assertOpen() throws IOException {
		if (state != State.OPEN) {
			throw new IOException("Chunk file session is already " + state.name().toLowerCase());
		}
	}

	private void ensureWriter() {
		if (excelWriter != null) {
			return;
		}
		ExcelWriterBuilder builder = EasyExcelFactory.write(outputStream).head(MapExcelRowSupport.buildHead(columns));
		applyDefaultHandlers(builder);
		excelWriter = builder.build();
		writeSheet = EasyExcelFactory.writerSheet(sheetName).build();
	}

	private void finishWriter() {
		if (excelWriter != null) {
			excelWriter.finish();
			excelWriter = null;
			return;
		}
		writeHeaderOnlyWorkbook();
	}

	/**
	 * 0 行场景：EasyExcel 未写入时 finish() 不产出表头，须显式写一次空行触发表头落盘
	 */
	private void writeHeaderOnlyWorkbook() {
		ExcelWriterBuilder builder = EasyExcelFactory.write(outputStream).head(MapExcelRowSupport.buildHead(columns));
		applyDefaultHandlers(builder);
		ExcelWriter headerWriter = builder.build();
		try {
			headerWriter.write(List.of(), EasyExcelFactory.writerSheet(sheetName).build());
		}
		finally {
			headerWriter.finish();
		}
	}

	private void applyDefaultHandlers(ExcelWriterBuilder builder) {
		builder.registerWriteHandler(new LongestMatchColumnWidthStyleStrategy());
		if (writeHandlers == null || writeHandlers.isEmpty()) {
			CellWriteHandler cellWriteHandler = ZebraStripeStyleType.getHandler(ZebraStripeStyleType.BLUE_GREY_TECH);
			builder.registerWriteHandler(cellWriteHandler);
			return;
		}
		for (CellWriteHandler handler : writeHandlers) {
			builder.registerWriteHandler(handler);
		}
	}

	private void closeQuietly() {
		state = State.CLOSED;
		if (excelWriter != null) {
			try {
				excelWriter.finish();
			}
			catch (RuntimeException ignored) {
				// best-effort close after write failure
			}
			finally {
				excelWriter = null;
			}
		}
		try {
			outputStream.close();
		}
		catch (IOException ignored) {
			// best-effort close
		}
	}

	private enum State {

		OPEN, COMPLETED, CLOSED

	}

}
