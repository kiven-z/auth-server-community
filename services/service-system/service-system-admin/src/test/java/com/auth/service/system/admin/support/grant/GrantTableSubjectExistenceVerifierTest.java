package com.auth.service.system.admin.support.grant;

import com.auth.module.security.contract.api.GrantTableSubjectType;
import com.auth.service.system.admin.mapper.admin.post.SysPostMapper;
import com.auth.service.system.admin.model.entity.SysPostEntity;
import com.auth.service.system.admin.support.post.PostReferenceChecker;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.common.exception.code.SystemCommonResultCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

/**
 * {@link GrantTableSubjectExistenceVerifier} 单元测试。
 */
@DisplayName("GrantTableSubjectExistenceVerifier 主体存在性校验")
@ExtendWith(MockitoExtension.class)
class GrantTableSubjectExistenceVerifierTest {

	private static final Long SUBJECT_ID = 10L;

	@Mock
	private SysPostMapper sysPostMapper;

	private GrantTableSubjectExistenceVerifier verifier;

	@BeforeEach
	void setUp() {
		TypedGrantTableActiveSubjectChecker postSubjectChecker = new TypedGrantTableActiveSubjectChecker(
				GrantTableSubjectType.POST, new PostReferenceChecker(sysPostMapper)::getExistingActive);
		verifier = new GrantTableSubjectExistenceVerifier(List.of(postSubjectChecker));
	}

	@Test
	@DisplayName("已注册主体类型：岗位行存在时通过校验")
	void requireExistingActivePassesWhenSubjectExists() {
		SysPostEntity entity = new SysPostEntity();
		entity.setId(SUBJECT_ID);
		when(sysPostMapper.selectById(SUBJECT_ID)).thenReturn(entity);

		assertThatCode(() -> verifier.requireExistingActive(GrantTableSubjectType.POST, SUBJECT_ID))
			.doesNotThrowAnyException();
	}

	@Test
	@DisplayName("已注册主体类型：不存在时抛业务异常")
	void requireExistingActiveThrowsWhenSubjectMissing() {
		when(sysPostMapper.selectById(SUBJECT_ID)).thenReturn(null);

		assertThatThrownBy(() -> verifier.requireExistingActive(GrantTableSubjectType.POST, SUBJECT_ID))
			.isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(SystemCommonResultCode.DATA_NOT_EXIST);
	}

	@Test
	@DisplayName("未注册主体类型：抛 IllegalArgumentException")
	void requireExistingActiveThrowsForUnregisteredType() {
		assertThatThrownBy(() -> verifier.requireExistingActive(GrantTableSubjectType.USER, SUBJECT_ID))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("Unsupported grant subject type");
	}

	@Test
	@DisplayName("重复注册同一主体类型：构造时抛 IllegalStateException")
	void constructorThrowsOnDuplicateSubjectType() {
		GrantTableActiveSubjectChecker duplicate = new TypedGrantTableActiveSubjectChecker(GrantTableSubjectType.POST,
				new PostReferenceChecker(sysPostMapper)::getExistingActive);

		List<GrantTableActiveSubjectChecker> duplicateCheckers = List.of(duplicate, duplicate);
		assertThatThrownBy(() -> new GrantTableSubjectExistenceVerifier(duplicateCheckers))
			.isInstanceOf(IllegalStateException.class)
			.hasMessageContaining("Duplicate grant table subject checker");
	}

}
