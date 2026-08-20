package com.auth.service.system.file.util;

import com.auth.service.system.file.config.properties.S3PlatformProfile;
import com.auth.service.system.file.utils.StorageUrlUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link StorageUrlUtil} 单元测试。
 *
 * @author Bunny
 */
@DisplayName("StorageUrlUtil 地址拼接")
class StorageUrlUtilTest {

	@Test
	@DisplayName("concatPublicUrl：自动裁剪斜杠后拼接")
	void concatPublicUrlTrimsSlashBeforeJoin() {
		String url = StorageUrlUtil.concatPublicUrl("https://cdn.example.com/", "/avatar/a.png");

		assertThat(url).isEqualTo("https://cdn.example.com/avatar/a.png");
	}

	@Test
	@DisplayName("normalizeHttpBaseUrl：endpoint 无协议时补全 https")
	void normalizeHttpBaseUrlAddsHttpsWhenProtocolMissing() {
		String baseUrl = StorageUrlUtil.normalizeHttpBaseUrl("oss-cn-hangzhou.aliyuncs.com/");

		assertThat(baseUrl).isEqualTo("https://oss-cn-hangzhou.aliyuncs.com");
	}

	@Test
	@DisplayName("resolveObjectUrl：优先使用 publicUrl")
	void resolveObjectUrlPrefersPublicUrl() {
		S3PlatformProfile profile = new S3PlatformProfile();
		profile.setEndpoint("https://oss-cn-shanghai.aliyuncs.com");
		profile.setBucket("bunny-auth");
		profile.setPublicUrl("https://cdn.example.com/assets");
		profile.setPathStyleAccess(false);

		String url = StorageUrlUtil.resolveObjectUrl(profile, "bunny-auth", "a.png");

		assertThat(url).isEqualTo("https://cdn.example.com/assets/a.png");
	}

	@Test
	@DisplayName("resolveObjectUrl：path-style 拼 endpoint/bucket/key")
	void resolveObjectUrlUsesPathStyle() {
		S3PlatformProfile profile = new S3PlatformProfile();
		profile.setEndpoint("http://127.0.0.1:9000");
		profile.setBucket("auth-files");
		profile.setPathStyleAccess(true);

		String url = StorageUrlUtil.resolveObjectUrl(profile, null, "images/a.png");

		assertThat(url).isEqualTo("http://127.0.0.1:9000/auth-files/images/a.png");
	}

	@Test
	@DisplayName("resolveObjectUrl：virtual-hosted 拼 bucket.endpoint/key")
	void resolveObjectUrlUsesVirtualHosted() {
		S3PlatformProfile profile = new S3PlatformProfile();
		profile.setEndpoint("https://oss-cn-shanghai.aliyuncs.com");
		profile.setBucket("bunny-auth");
		profile.setPathStyleAccess(false);

		String url = StorageUrlUtil.resolveObjectUrl(profile, "bunny-auth", "images/a.png");

		assertThat(url).isEqualTo("https://bunny-auth.oss-cn-shanghai.aliyuncs.com/images/a.png");
	}

}
