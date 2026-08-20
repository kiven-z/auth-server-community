package com.auth.module.security.autoconfigure.boot.integration;

import com.auth.common.data.model.BaseEntity;
import com.auth.module.security.contract.api.authorization.AuthProfile;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * {@link MybatisPlusAuditFillAutoConfiguration} 审计字段自动填充测试。
 */
@DisplayName("MybatisPlusAuditFillAutoConfiguration 审计字段填充")
class MybatisPlusAuditFillAutoConfigurationTest {

	private final MybatisPlusAuditFillAutoConfiguration configuration = new MybatisPlusAuditFillAutoConfiguration();

	@AfterEach
	void tearDown() {
		SecurityContextHolder.clearContext();
	}

	@Test
	@DisplayName("updateFill：无登录用户时仍填充 updatedAt，updatedBy 保持为空")
	void updateFill_withoutAuthenticatedUser_fillsUpdatedAtOnly() {
		BaseEntity entity = new BaseEntity();
		MetaObject metaObject = SystemMetaObject.forObject(entity);

		configuration.updateFill(metaObject);

		assertThat(entity.getUpdatedAt()).isNotNull();
		assertThat(entity.getUpdatedBy()).isNull();
	}

	@Test
	@DisplayName("updateFill：有登录用户时同时填充 updatedAt 与 updatedBy")
	void updateFill_withAuthenticatedUser_fillsUpdatedAtAndUser() {
		AuthProfile profile = mock(AuthProfile.class);
		when(profile.getUserId()).thenReturn(42L);
		SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(profile, null));

		BaseEntity entity = new BaseEntity();
		MetaObject metaObject = SystemMetaObject.forObject(entity);

		configuration.updateFill(metaObject);

		assertThat(entity.getUpdatedAt()).isNotNull();
		assertThat(entity.getUpdatedBy()).isEqualTo(42L);
	}

}
