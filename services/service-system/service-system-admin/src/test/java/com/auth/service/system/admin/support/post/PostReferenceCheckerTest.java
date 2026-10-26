package com.auth.service.system.admin.support.post;

import com.auth.service.system.admin.mapper.admin.post.SysPostMapper;
import com.auth.service.system.admin.model.entity.SysPostEntity;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.common.exception.code.SystemCommonResultCode;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link PostReferenceChecker} 单元测试。
 */
@DisplayName("PostReferenceChecker 岗位存在性校验")
@ExtendWith(MockitoExtension.class)
class PostReferenceCheckerTest {

	private static final Long POST_ID = 10L;

	@Mock
	private SysPostMapper sysPostMapper;

	private PostReferenceChecker postReferenceChecker;

	@BeforeEach
	void setUp() {
		postReferenceChecker = new PostReferenceChecker(sysPostMapper);
	}

	@Test
	@DisplayName("getExistingActive：岗位存在时返回实体")
	void getExistingActiveReturnsEntityWhenPresent() {
		SysPostEntity entity = new SysPostEntity();
		entity.setId(POST_ID);
		when(sysPostMapper.selectById(POST_ID)).thenReturn(entity);

		assertThat(postReferenceChecker.getExistingActive(POST_ID)).isSameAs(entity);
		verify(sysPostMapper).selectById(POST_ID);
	}

	@Test
	@DisplayName("getExistingActive：岗位不存在时抛出 DATA_NOT_EXIST")
	void getExistingActiveThrowsWhenMissing() {
		when(sysPostMapper.selectById(POST_ID)).thenReturn(null);

		ThrowingCallable executable = () -> postReferenceChecker.getExistingActive(POST_ID);
		assertThatThrownBy(executable).isInstanceOf(SystemBusinessException.class).satisfies(ex -> {
			SystemBusinessException biz = (SystemBusinessException) ex;
			assertThat(biz.getResultCode()).isEqualTo(SystemCommonResultCode.DATA_NOT_EXIST);
			assertThat(biz.getMessageArgs()).isEmpty();
		});
	}

}
