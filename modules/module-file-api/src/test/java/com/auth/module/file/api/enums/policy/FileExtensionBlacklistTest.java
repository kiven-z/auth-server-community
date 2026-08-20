package com.auth.module.file.api.enums.policy;

import com.auth.module.file.api.policy.FileExtensionBlacklist;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link FileExtensionBlacklist} 单元测试。
 *
 * @author Bunny
 */
@DisplayName("FileExtensionBlacklist 危险后缀")
class FileExtensionBlacklistTest {

	@Test
	@DisplayName("isBlocked：命中 exe/sh 等危险后缀")
	void blocksDangerousExtensions() {
		// 验证常见可执行与脚本后缀被拦截
		assertThat(FileExtensionBlacklist.isBlocked("payload.exe")).isTrue();
		assertThat(FileExtensionBlacklist.isBlocked("install.SH")).isTrue();
		assertThat(FileExtensionBlacklist.isBlocked("C:\\temp\\run.bat")).isTrue();
		assertThat(FileExtensionBlacklist.isBlocked("a/b/c.ps1")).isTrue();
	}

	@Test
	@DisplayName("isBlocked：安全后缀与无后缀放行")
	void allowsSafeOrMissingExtensions() {
		// 验证图片/文档后缀与无后缀文件名不命中黑名单
		assertThat(FileExtensionBlacklist.isBlocked("avatar.png")).isFalse();
		assertThat(FileExtensionBlacklist.isBlocked("report.pdf")).isFalse();
		assertThat(FileExtensionBlacklist.isBlocked("readme")).isFalse();
		assertThat(FileExtensionBlacklist.isBlocked(" ")).isFalse();
		assertThat(FileExtensionBlacklist.isBlocked(null)).isFalse();
	}

	@Test
	@DisplayName("extractExtension：取末级后缀并小写化")
	void extractsLastExtensionInLowerCase() {
		// 验证双重后缀只取最后一段，并统一为小写
		assertThat(FileExtensionBlacklist.extractExtension("photo.JPG")).isEqualTo("jpg");
		assertThat(FileExtensionBlacklist.extractExtension("archive.tar.gz")).isEqualTo("gz");
		assertThat(FileExtensionBlacklist.extractExtension("virus.png.exe")).isEqualTo("exe");
	}

}
