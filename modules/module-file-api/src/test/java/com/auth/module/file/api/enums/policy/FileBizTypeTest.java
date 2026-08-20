package com.auth.module.file.api.enums.policy;

import com.auth.module.file.api.model.enums.VisibilityRule;
import com.auth.module.file.api.policy.AllowedMimeTypes;
import com.auth.module.file.api.policy.FileBizType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * {@link FileBizType} 单元测试。
 *
 * @author Bunny
 */
@DisplayName("FileBizType 业务类型解析")
class FileBizTypeTest {

	@Test
	@DisplayName("require：大小写与空白均可归一化解析")
	void requireParsesCodeIgnoringCaseAndWhitespace() {
		// 验证严格解析支持大小写与首尾空白归一化
		assertThat(FileBizType.require("  AvAtAr  ")).isEqualTo(FileBizType.AVATAR);
		assertThat(FileBizType.require("MESSAGE-IMAGE")).isEqualTo(FileBizType.MESSAGE_IMAGE);
		assertThat(FileBizType.require("ATTACHMENT")).isEqualTo(FileBizType.ATTACHMENT);
	}

	@Test
	@DisplayName("require：非法值抛出 IllegalArgumentException")
	void requireThrowsForUnknownCode() {
		// 验证严格解析会拒绝未知业务类型
		assertThatThrownBy(() -> FileBizType.require("unknown")).isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("unknown");
		assertThatThrownBy(() -> FileBizType.require("product_image")).isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("product_image");
	}

	@Test
	@DisplayName("require：空值抛出 IllegalArgumentException")
	void requireThrowsForBlankCode() {
		// 验证严格解析会拒绝空白输入
		assertThatThrownBy(() -> FileBizType.require(" ")).isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("blank");
	}

	@Test
	@DisplayName("策略字段：枚举项暴露可见性、大小与 MIME 白名单")
	void exposesPolicyMetadataOnEnumConstants() {
		// 验证上传策略元数据已内聚到枚举，且复用 AllowedMimeTypes 常量组
		assertThat(FileBizType.AVATAR.getCode()).isEqualTo("avatar");
		assertThat(FileBizType.AVATAR.getVisibilityRule()).isEqualTo(VisibilityRule.FORCE_PUBLIC);
		assertThat(FileBizType.AVATAR.getMaxSizeMb()).isEqualTo(2);
		assertThat(FileBizType.AVATAR.getAllowedMimeTypes()).isEqualTo(AllowedMimeTypes.RASTER_IMAGE);
		assertThat(FileBizType.AVATAR.allowsMimeType("image/png")).isTrue();
		assertThat(FileBizType.AVATAR.allowsMimeType("image/gif")).isFalse();
		assertThat(FileBizType.MESSAGE_IMAGE.getAllowedMimeTypes()).isEqualTo(AllowedMimeTypes.RASTER_IMAGE_WITH_GIF);
		assertThat(FileBizType.MESSAGE_IMAGE.allowsMimeType("image/gif")).isTrue();
		assertThat(FileBizType.ATTACHMENT.getAllowedMimeTypes()).containsAll(AllowedMimeTypes.RASTER_IMAGE_WITH_GIF)
			.containsAll(AllowedMimeTypes.OFFICE_DOCUMENTS);
		assertThat(FileBizType.ATTACHMENT.allowsMimeType("application/pdf")).isTrue();
		assertThat(FileBizType.AVATAR.maxSizeBytes()).isEqualTo(2L * 1024 * 1024);
	}

	@Test
	@DisplayName("allowsMimeType：忽略 charset 参数并大小写不敏感")
	void allowsMimeTypeIgnoresCharsetAndCase() {
		// 验证 MIME 参数剥离与大小写归一化
		assertThat(FileBizType.ATTACHMENT.allowsMimeType("TEXT/PLAIN; charset=UTF-8")).isTrue();
		assertThat(FileBizType.AVATAR.allowsMimeType("Image/JPEG")).isTrue();
		assertThat(FileBizType.AVATAR.allowsMimeType("")).isFalse();
		assertThat(FileBizType.AVATAR.allowsMimeType(null)).isFalse();
	}

	@Test
	@DisplayName("可见性策略：avatar/message-image 固定公开，attachment 默认私有")
	void visibilityPolicyMatchesBizType() {
		// 验证各业务类型的可见性策略与 VisibilityRule 组合一致
		assertThat(FileBizType.AVATAR.getVisibilityRule().resolve(true)).isFalse();
		assertThat(FileBizType.AVATAR.getVisibilityRule().resolve(false)).isFalse();
		assertThat(FileBizType.MESSAGE_IMAGE.getVisibilityRule().resolve(true)).isFalse();
		assertThat(FileBizType.MESSAGE_IMAGE.getVisibilityRule().resolve(false)).isFalse();
		assertThat(FileBizType.ATTACHMENT.getVisibilityRule().resolve(null)).isTrue();
		assertThat(FileBizType.ATTACHMENT.getVisibilityRule().resolve(false)).isFalse();
		assertThat(FileBizType.ATTACHMENT.getVisibilityRule().resolve(true)).isTrue();
	}

}
