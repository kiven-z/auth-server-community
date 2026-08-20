package com.auth.service.system.file.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link FileObjectKeyUtil} URL 解析单元测试
 */
@DisplayName("FileObjectKeyUtil URL 解析")
class FileUrlResolverUtilTest {

	@Test
	@DisplayName("公开路径 URL 可解析出 object_key")
	void resolveObjectKeyFromUrl_publicPath() {
		String url = "https://cdn.example.com/public/avatar/20260702/a.png";

		var objectKey = FileObjectKeyUtil.resolveObjectKeyFromUrl(url);

		assertThat(objectKey).hasValue("public/avatar/20260702/a.png");
	}

	@Test
	@DisplayName("私有路径 URL 可解析出 object_key")
	void resolveObjectKeyFromUrl_privatePath() {
		String url = "https://cdn.example.com/private/attachment/20260702/a.txt";

		var objectKey = FileObjectKeyUtil.resolveObjectKeyFromUrl(url);

		assertThat(objectKey).hasValue("private/attachment/20260702/a.txt");
	}

	@Test
	@DisplayName("无 public/private 段时返回空")
	void resolveObjectKeyFromUrl_unknownPath() {
		String url = "https://cdn.example.com/assets/a.png";

		var objectKey = FileObjectKeyUtil.resolveObjectKeyFromUrl(url);

		assertThat(objectKey).isEmpty();
	}

	@Test
	@DisplayName("空 URL 返回空")
	void resolveObjectKeyFromUrl_blank() {
		assertThat(FileObjectKeyUtil.resolveObjectKeyFromUrl(null)).isEmpty();
		assertThat(FileObjectKeyUtil.resolveObjectKeyFromUrl("  ")).isEmpty();
	}

}
