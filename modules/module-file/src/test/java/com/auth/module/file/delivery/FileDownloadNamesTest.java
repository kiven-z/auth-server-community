package com.auth.module.file.delivery;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * {@link FileDownloadNames} 单元测试
 *
 * @author Bunny
 */
@DisplayName("FileDownloadNames 下载文件名拼装")
class FileDownloadNamesTest {

	@Test
	@DisplayName("stamp：固定瞬时点格式化为 UTC 时间戳")
	void stampFormatsUtcInstant() {
		Instant instant = Instant.parse("2026-08-11T14:30:22Z");
		assertThat(FileDownloadNames.stamp(instant)).isEqualTo("20260811143022");
	}

	@Test
	@DisplayName("of：拼装 prefix_stamp.ext，扩展名去点并小写")
	void ofBuildsPrefixedName() {
		Instant instant = Instant.parse("2026-08-11T14:30:22Z");
		assertThat(FileDownloadNames.of("sys_post_export", instant, ".XLSX"))
			.isEqualTo("sys_post_export_20260811143022.xlsx");
	}

	@Test
	@DisplayName("batchZip：以 zip 扩展名拼装批量包名")
	void batchZipUsesZipExtension() {
		assertThat(FileDownloadNames.batchZip("file-records")).startsWith("file-records_")
			.endsWith(".zip")
			.matches("file-records_\\d{14}\\.zip");
	}

	@Test
	@DisplayName("of：前缀或扩展名为空时拒绝")
	void ofRejectsBlankArgs() {
		Instant instant = Instant.parse("2026-08-11T14:30:22Z");
		assertThatThrownBy(() -> FileDownloadNames.of(" ", instant, "xlsx"))
			.isInstanceOf(IllegalArgumentException.class);
		assertThatThrownBy(() -> FileDownloadNames.of("prefix", instant, " "))
			.isInstanceOf(IllegalArgumentException.class);
	}

}
