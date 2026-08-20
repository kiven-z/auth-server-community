package com.auth.service.system.file.support;

import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.file.utils.FileObjectKeyUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * {@link FileObjectKeyUtil} 单元测试。
 *
 * @author Bunny
 */
@DisplayName("FileObjectKeyUtil 对象键工具")
class FileObjectKeyUtilTest {

	@Test
	@DisplayName("build：公开文件使用 public 前缀并归一化 bizType")
	void buildUsesPublicPrefixAndNormalizesBizType() {
		// 验证公开对象键以 public/ 开头，且 bizType 非法字符被替换。
		String objectKey = FileObjectKeyUtil.build("avatar", false, "photo.png");

		assertThat(objectKey).startsWith(FileObjectKeyUtil.PUBLIC_PREFIX + "avatar/").endsWith(".png");
	}

	@Test
	@DisplayName("build：私有文件使用 private 前缀")
	void buildUsesPrivatePrefixForPrivateFiles() {
		// 验证私有对象键以 private/ 开头。
		String objectKey = FileObjectKeyUtil.build("attachment", true, "report.txt");

		assertThat(objectKey).startsWith(FileObjectKeyUtil.PRIVATE_PREFIX + "attachment/").endsWith(".txt");
	}

	@Test
	@DisplayName("build：无扩展名时仅使用随机文件名")
	void buildUsesRandomNameWhenExtensionMissing() {
		// 验证无扩展名文件不会追加多余点号。
		String objectKey = FileObjectKeyUtil.build("avatar", false, "photo");

		assertThat(objectKey).startsWith(FileObjectKeyUtil.PUBLIC_PREFIX + "avatar/").doesNotContain("..");
	}

	@Test
	@DisplayName("switchVisibilityPrefix：public 切换为 private 保留后缀路径")
	void switchVisibilityPrefixMovesPublicToPrivate() {
		// 验证仅替换可见性前缀，bizType/date/uuid 段保持不变。
		String switched = FileObjectKeyUtil.switchVisibilityPrefix("public/avatar/20260702/a.png", true);

		assertThat(switched).isEqualTo("private/avatar/20260702/a.png");
	}

	@Test
	@DisplayName("switchVisibilityPrefix：private 切换为 public 保留后缀路径")
	void switchVisibilityPrefixMovesPrivateToPublic() {
		// 验证 private → public 仅替换可见性前缀。
		String switched = FileObjectKeyUtil.switchVisibilityPrefix("private/attachment/20260702/a.txt", false);

		assertThat(switched).isEqualTo("public/attachment/20260702/a.txt");
	}

	@Test
	@DisplayName("switchVisibilityPrefix：历史无前缀对象键补 public 前缀")
	void switchVisibilityPrefixPrependsPublicForLegacyKey() {
		// 验证历史无前缀对象键可直接补目标公开前缀。
		String switched = FileObjectKeyUtil
			.switchVisibilityPrefix("avatar/20260704/d58e5f1377e1454180f80a96c90bd7ec.png", false);

		assertThat(switched).isEqualTo("public/avatar/20260704/d58e5f1377e1454180f80a96c90bd7ec.png");
	}

	@Test
	@DisplayName("switchVisibilityPrefix：历史无前缀对象键补 private 前缀")
	void switchVisibilityPrefixPrependsPrivateForLegacyKey() {
		// 验证历史无前缀对象键可直接补目标私有前缀。
		String switched = FileObjectKeyUtil.switchVisibilityPrefix("avatar/20260704/a.png", true);

		assertThat(switched).isEqualTo("private/avatar/20260704/a.png");
	}

	@Test
	@DisplayName("switchVisibilityPrefix：未知前缀按历史对象键处理并补目标前缀")
	void switchVisibilityPrefixTreatsUnknownPrefixAsLegacyBody() {
		// 验证非 public/private 前缀时整段 key 作为 body 拼接目标前缀。
		String switched = FileObjectKeyUtil.switchVisibilityPrefix("unknown/avatar/a.png", true);

		assertThat(switched).isEqualTo("private/unknown/avatar/a.png");
	}

	@Test
	@DisplayName("switchVisibilityPrefix：空对象键时抛业务异常")
	void switchVisibilityPrefixThrowsWhenObjectKeyBlank() {
		// 验证空白对象键仍会被拒绝。
		assertThatThrownBy(() -> FileObjectKeyUtil.switchVisibilityPrefix("  ", false))
			.isInstanceOf(SystemBusinessException.class);
	}

}
