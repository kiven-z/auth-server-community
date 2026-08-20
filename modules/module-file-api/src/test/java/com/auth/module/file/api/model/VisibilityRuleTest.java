package com.auth.module.file.api.model;

import com.auth.module.file.api.model.enums.VisibilityRule;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link VisibilityRule} 单元测试
 *
 * @author Bunny
 */
@DisplayName("VisibilityRule 可见性规则")
class VisibilityRuleTest {

	@Test
	@DisplayName("FORCE_PUBLIC：始终公开")
	void forcePublicAlwaysReturnsPublic() {
		// 验证强制公开规则忽略客户端请求。
		assertThat(VisibilityRule.FORCE_PUBLIC.resolve(null)).isFalse();
		assertThat(VisibilityRule.FORCE_PUBLIC.resolve(Boolean.TRUE)).isFalse();
		assertThat(VisibilityRule.FORCE_PUBLIC.resolve(Boolean.FALSE)).isFalse();
	}

	@Test
	@DisplayName("FORCE_PRIVATE：始终私有")
	void forcePrivateAlwaysReturnsPrivate() {
		// 验证强制私有规则忽略客户端请求。
		assertThat(VisibilityRule.FORCE_PRIVATE.resolve(null)).isTrue();
		assertThat(VisibilityRule.FORCE_PRIVATE.resolve(Boolean.TRUE)).isTrue();
		assertThat(VisibilityRule.FORCE_PRIVATE.resolve(Boolean.FALSE)).isTrue();
	}

	@Test
	@DisplayName("DEFAULT_PUBLIC：默认公开且支持覆盖")
	void defaultPublicUsesPublicDefaultAndAllowsOverride() {
		// 验证默认公开在空请求时取公开，在有请求时按请求值。
		assertThat(VisibilityRule.DEFAULT_PUBLIC.resolve(null)).isFalse();
		assertThat(VisibilityRule.DEFAULT_PUBLIC.resolve(Boolean.TRUE)).isTrue();
		assertThat(VisibilityRule.DEFAULT_PUBLIC.resolve(Boolean.FALSE)).isFalse();
	}

	@Test
	@DisplayName("DEFAULT_PRIVATE：默认私有且支持覆盖")
	void defaultPrivateUsesPrivateDefaultAndAllowsOverride() {
		// 验证默认私有在空请求时取私有，在有请求时按请求值。
		assertThat(VisibilityRule.DEFAULT_PRIVATE.resolve(null)).isTrue();
		assertThat(VisibilityRule.DEFAULT_PRIVATE.resolve(Boolean.TRUE)).isTrue();
		assertThat(VisibilityRule.DEFAULT_PRIVATE.resolve(Boolean.FALSE)).isFalse();
	}

}
