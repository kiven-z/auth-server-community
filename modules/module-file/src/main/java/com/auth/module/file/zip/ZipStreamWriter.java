package com.auth.module.file.zip;

import cn.hutool.core.text.CharSequenceUtil;
import lombok.experimental.UtilityClass;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * ZIP 输出工具：支持流式写入与同名条目重命名。
 *
 * @author Bunny
 */
@UtilityClass
public class ZipStreamWriter {

	/**
	 * 将文件条目流式写入 ZIP 输出流。
	 * @param outputStream 输出流
	 * @param payloads ZIP 条目负载
	 * @throws IOException 写入异常
	 */
	public static void write(OutputStream outputStream, List<ZipEntryPayload> payloads) throws IOException {
		Map<String, Integer> duplicateNameCounter = new HashMap<>();
		try (ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream)) {
			for (ZipEntryPayload payload : payloads) {
				String entryName = resolveUniqueEntryName(payload.entryName(), duplicateNameCounter);
				zipOutputStream.putNextEntry(new ZipEntry(entryName));
				try (InputStream inputStream = payload.inputStreamSupplier().get()) {
					inputStream.transferTo(zipOutputStream);
				}
				zipOutputStream.closeEntry();
			}
			zipOutputStream.finish();
		}
	}

	/**
	 * 解析 ZIP 条目名，不存在文件名时使用兜底名称。
	 * @param preferredName 优先文件名
	 * @param extension 文件后缀（不含点）
	 * @param fallbackBaseName 兜底基础名
	 * @return 条目名
	 */
	public static String resolveEntryName(String preferredName, String extension, String fallbackBaseName) {
		String entryName = CharSequenceUtil.trim(preferredName);
		if (CharSequenceUtil.isBlank(entryName)) {
			String trimmedExtension = CharSequenceUtil.trim(extension);
			String extensionSuffix = CharSequenceUtil.isBlank(trimmedExtension) ? "" : "." + trimmedExtension;
			entryName = fallbackBaseName + extensionSuffix;
		}
		return entryName.replace("\\", "_").replace("/", "_");
	}

	/**
	 * 处理 ZIP 内同名条目，按序追加编号后缀。
	 * @param entryName 原始条目名
	 * @param duplicateNameCounter 文件名计数器
	 * @return 唯一条目名
	 */
	public static String resolveUniqueEntryName(String entryName, Map<String, Integer> duplicateNameCounter) {
		int count = duplicateNameCounter.getOrDefault(entryName, 0);
		duplicateNameCounter.put(entryName, count + 1);
		if (count == 0) {
			return entryName;
		}

		int dotIndex = entryName.lastIndexOf('.');
		if (dotIndex <= 0 || dotIndex == entryName.length() - 1) {
			return entryName + "(" + count + ")";
		}

		String baseName = entryName.substring(0, dotIndex);
		String extension = entryName.substring(dotIndex);
		return baseName + "(" + count + ")" + extension;
	}

	/**
	 * ZIP 条目读取器
	 */
	@FunctionalInterface
	public interface InputStreamSupplier {

		/**
		 * 打开条目输入流
		 * @return 输入流
		 * @throws IOException IO 异常
		 */
		InputStream get() throws IOException;

	}

	/**
	 * ZIP 条目负载
	 *
	 * @param entryName 条目名
	 * @param inputStreamSupplier 输入流读取器
	 */
	public record ZipEntryPayload(String entryName, InputStreamSupplier inputStreamSupplier) {

		/**
		 * 构造 ZIP 条目负载
		 */
		public ZipEntryPayload {
			Objects.requireNonNull(entryName, "entryName must not be null");
			Objects.requireNonNull(inputStreamSupplier, "inputStreamSupplier must not be null");
		}

	}

}
