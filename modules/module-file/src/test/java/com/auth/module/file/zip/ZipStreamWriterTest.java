package com.auth.module.file.zip;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link ZipStreamWriter} 单元测试
 *
 * @author Bunny
 */
@DisplayName("ZipStreamWriter ZIP 写入")
class ZipStreamWriterTest {

	@Test
	@DisplayName("同名文件写入 ZIP 时自动追加序号")
	void write_whenDuplicateEntryName_suffixWithCounter() throws IOException {
		// 两个同名 entry 写入后，后续文件名应追加 (1) 后缀
		List<ZipStreamWriter.ZipEntryPayload> payloads = List.of(
				new ZipStreamWriter.ZipEntryPayload("a.txt",
						() -> new ByteArrayInputStream("first".getBytes(StandardCharsets.UTF_8))),
				new ZipStreamWriter.ZipEntryPayload("a.txt",
						() -> new ByteArrayInputStream("second".getBytes(StandardCharsets.UTF_8))),
				new ZipStreamWriter.ZipEntryPayload("a.txt",
						() -> new ByteArrayInputStream("third".getBytes(StandardCharsets.UTF_8))));
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

		ZipStreamWriter.write(outputStream, payloads);

		List<String> entryNames = readEntryNames(outputStream.toByteArray());
		assertThat(entryNames).containsExactly("a.txt", "a(1).txt", "a(2).txt");
	}

	@Test
	@DisplayName("空文件名时按兜底名称生成")
	void resolveEntryName_whenPreferredBlank_useFallbackName() {
		// originalName 为空时，使用 fallback + extension 作为条目名
		String entryName = ZipStreamWriter.resolveEntryName(" ", "png", "file-1");
		assertThat(entryName).isEqualTo("file-1.png");
	}

	private List<String> readEntryNames(byte[] zipBytes) throws IOException {
		List<String> entryNames = new ArrayList<>();
		try (ZipInputStream zipInputStream = new ZipInputStream(new ByteArrayInputStream(zipBytes))) {
			ZipEntry entry;
			while ((entry = zipInputStream.getNextEntry()) != null) {
				entryNames.add(entry.getName());
			}
		}
		return entryNames;
	}

}
