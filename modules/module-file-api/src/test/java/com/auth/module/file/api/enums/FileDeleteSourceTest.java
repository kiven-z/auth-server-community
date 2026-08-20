package com.auth.module.file.api.enums;

import com.auth.module.file.api.model.enums.FileDeleteSource;
import com.auth.module.file.api.model.enums.RecycleVisibility;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link FileDeleteSource} 单元测试。
 *
 * @author Bunny
 */
@DisplayName("FileDeleteSource 删除来源解析")
class FileDeleteSourceTest {

	@Test
	@DisplayName("parse：大小写与空白均可归一化解析")
	void parseParsesCodeIgnoringCaseAndWhitespace() {
		// user_self 采用标准化 code 后可识别
		Optional<FileDeleteSource> userSelf = FileDeleteSource.parse("  user_self  ");
		Optional<FileDeleteSource> adminAction = FileDeleteSource.parse("admin_action");

		assertThat(userSelf).contains(FileDeleteSource.USER_SELF);
		assertThat(adminAction).contains(FileDeleteSource.ADMIN_ACTION);
	}

	@Test
	@DisplayName("parse：空值返回 Optional.empty")
	void parseReturnsEmptyForBlankInput() {
		// 验证空输入不会命中任何删除来源
		assertThat(FileDeleteSource.parse(null)).isEmpty();
		assertThat(FileDeleteSource.parse("")).isEmpty();
		assertThat(FileDeleteSource.parse("   ")).isEmpty();
	}

	@Test
	@DisplayName("parse：非法值返回 Optional.empty")
	void parseReturnsEmptyForUnknownCode() {
		// 验证未知删除来源按宽松策略返回 empty
		assertThat(FileDeleteSource.parse("unknown")).isEmpty();
	}

	@Test
	@DisplayName("userRecycleSourceCodes：仅包含 USER 可见性来源")
	void userRecycleSourceCodesContainsOnlyUserVisibleSources() {
		// 验证用户回收站范围由枚举策略集中维护
		assertThat(FileDeleteSource.userRecycleSourceCodes()).containsExactlyInAnyOrder("USER_SELF");
		assertThat(FileDeleteSource.userRecycleSourceCodes()).doesNotContain("ADMIN_ACTION", "SYSTEM_ACTION");
	}

	@Test
	@DisplayName("回收站可见性策略与枚举定义一致")
	void recycleVisibilityMatchesEnumDefinition() {
		// 验证各来源的可见性策略符合预期
		assertThat(FileDeleteSource.USER_SELF.getRecycleVisibility()).isEqualTo(RecycleVisibility.USER);
		assertThat(FileDeleteSource.ADMIN_ACTION.getRecycleVisibility()).isEqualTo(RecycleVisibility.ADMIN_ONLY);
		assertThat(FileDeleteSource.SYSTEM_ACTION.getRecycleVisibility()).isEqualTo(RecycleVisibility.HIDDEN);
	}

}
