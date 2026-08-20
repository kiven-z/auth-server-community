package com.auth.service.system.admin.support.dept;

import com.auth.service.system.admin.mapper.admin.dept.SysDeptMapper;
import com.auth.service.system.admin.model.entity.SysDeptEntity;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.common.exception.code.SystemCommonResultCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

/**
 * {@link DeptReferenceChecker} 单元测试。
 */
@DisplayName("DeptReferenceChecker 部门引用校验")
@ExtendWith(MockitoExtension.class)
class DeptReferenceCheckerTest {

	private static final Long DEPT_ID = 10L;

	@Mock
	private SysDeptMapper sysDeptMapper;

	@InjectMocks
	private DeptReferenceChecker deptReferenceChecker;

	private static SysDeptEntity activeDept() {
		SysDeptEntity entity = new SysDeptEntity();
		entity.setId(DeptReferenceCheckerTest.DEPT_ID);
		entity.setDeptCode("D" + DeptReferenceCheckerTest.DEPT_ID);
		entity.setDeptName("部门-" + DeptReferenceCheckerTest.DEPT_ID);
		return entity;
	}

	@Test
	@DisplayName("getExistingActive：部门存在时返回实体")
	void getExistingActiveReturnsEntityWhenPresent() {
		SysDeptEntity entity = activeDept();
		when(sysDeptMapper.selectById(DEPT_ID)).thenReturn(entity);

		assertThat(deptReferenceChecker.getExistingActive(DEPT_ID)).isSameAs(entity);
		verify(sysDeptMapper).selectById(DEPT_ID);
	}

	@Test
	@DisplayName("getExistingActive：部门不存在时抛出 DATA_NOT_EXIST")
	void getExistingActiveThrowsWhenMissing() {
		when(sysDeptMapper.selectById(DEPT_ID)).thenReturn(null);

		assertThatThrownBy(() -> deptReferenceChecker.getExistingActive(DEPT_ID))
			.isInstanceOf(SystemBusinessException.class)
			.satisfies(ex -> {
				SystemBusinessException biz = (SystemBusinessException) ex;
				assertThat(biz.getResultCode()).isEqualTo(SystemCommonResultCode.DATA_NOT_EXIST);
				assertThat(biz.getMessageArgs()).isEmpty();
			});
	}

	@Test
	@DisplayName("requireEffective：计算有效时返回实体")
	void requireEffectiveReturnsEntityWhenInView() {
		SysDeptEntity entity = activeDept();
		when(sysDeptMapper.selectById(DEPT_ID)).thenReturn(entity);
		when(sysDeptMapper.countEffectiveById(DEPT_ID)).thenReturn(1L);

		assertThat(deptReferenceChecker.requireEffective(DEPT_ID)).isSameAs(entity);
		verify(sysDeptMapper).countEffectiveById(DEPT_ID);
	}

	@Test
	@DisplayName("requireEffective：部门不存在时抛出 DATA_NOT_EXIST")
	void requireEffectiveThrowsWhenMissing() {
		when(sysDeptMapper.selectById(DEPT_ID)).thenReturn(null);

		assertThatThrownBy(() -> deptReferenceChecker.requireEffective(DEPT_ID))
			.isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(SystemCommonResultCode.DATA_NOT_EXIST);
		verify(sysDeptMapper, never()).countEffectiveById(DEPT_ID);
	}

	@Test
	@DisplayName("requireEffective：不在有效视图时抛出 DATA_UNAVAILABLE")
	void requireEffectiveThrowsWhenNotInView() {
		SysDeptEntity entity = activeDept();
		when(sysDeptMapper.selectById(DEPT_ID)).thenReturn(entity);
		when(sysDeptMapper.countEffectiveById(DEPT_ID)).thenReturn(0L);

		assertThatThrownBy(() -> deptReferenceChecker.requireEffective(DEPT_ID))
			.isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(SystemCommonResultCode.DATA_UNAVAILABLE);
	}

}
