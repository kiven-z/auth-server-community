package com.auth.service.system.schedule.service.impl;

import com.auth.service.system.common.service.AuditUserDisplayService;
import com.auth.service.system.schedule.exception.SysJobException;
import com.auth.service.system.schedule.mapper.JobGroupMapper;
import com.auth.service.system.schedule.mapper.JobMapper;
import com.auth.service.system.schedule.model.entity.JobGroupEntity;
import com.auth.service.system.schedule.model.form.SysJobGroupForm;
import com.auth.service.system.schedule.model.vo.SysJobGroupDetailVO;
import com.baomidou.mybatisplus.extension.repository.CrudRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.List;

import static com.auth.service.system.common.exception.code.SystemCommonResultCode.DATA_CODE_DUPLICATE;
import static com.auth.service.system.common.exception.code.SystemCommonResultCode.DATA_NOT_EXIST;
import static com.auth.service.system.schedule.exception.ScheduleResultCode.JOB_GROUP_HAS_ACTIVE_JOBS;
import static com.auth.service.system.schedule.exception.ScheduleResultCode.JOB_GROUP_SYSTEM_PROTECTED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link SysJobGroupServiceImpl} 单元测试。
 *
 * @author Bunny
 */
@DisplayName("SysJobGroupServiceImpl 定时任务分组")
@ExtendWith(MockitoExtension.class)
class SysJobGroupServiceImplTest {

	@Mock
	private JobGroupMapper jobGroupMapper;

	@Mock
	private JobMapper jobMapper;

	@Mock
	private AuditUserDisplayService auditUserDisplayService;

	private SysJobGroupServiceImpl sysJobGroupService;

	/**
	 * 最小合法新增表单
	 */
	private static SysJobGroupForm minimalForm(String groupCode, String groupName) {
		SysJobGroupForm form = new SysJobGroupForm();
		form.setGroupCode(groupCode);
		form.setGroupName(groupName);
		form.setStatus(true);
		return form;
	}

	@BeforeEach
	void setUp() throws Exception {
		sysJobGroupService = new SysJobGroupServiceImpl(jobMapper, auditUserDisplayService);
		Field baseMapperField = CrudRepository.class.getDeclaredField("baseMapper");
		baseMapperField.setAccessible(true);
		baseMapperField.set(sysJobGroupService, jobGroupMapper);
	}

	@Test
	@DisplayName("详情：存在时返回 VO 并补全审计展示名")
	void getDetailReturnsVoWhenExists() {
		JobGroupEntity entity = new JobGroupEntity();
		entity.setId(1L);
		entity.setGroupCode("G1");
		entity.setGroupName("分组一");
		entity.setStatus(true);
		entity.setIsSystem(false);
		entity.setOrderNum(0);
		when(jobGroupMapper.selectById(1L)).thenReturn(entity);

		SysJobGroupDetailVO detail = sysJobGroupService.getDetail(1L);

		assertThat(detail.getGroupCode()).isEqualTo("G1");
		assertThat(detail.getGroupName()).isEqualTo("分组一");
		verify(auditUserDisplayService).enrichAuditUsernames(any(List.class), isNull(), isNull());
	}

	@Test
	@DisplayName("详情：分组不存在时抛出 DATA_NOT_EXIST")
	void getDetailThrowsWhenMissing() {
		when(jobGroupMapper.selectById(99L)).thenReturn(null);

		assertThatThrownBy(() -> sysJobGroupService.getDetail(99L)).isInstanceOf(SysJobException.class)
			.extracting(ex -> ((SysJobException) ex).getResultCode())
			.isEqualTo(DATA_NOT_EXIST);
	}

	@Test
	@DisplayName("新增：分组编码重复时抛出 DATA_CODE_DUPLICATE")
	void createRejectsDuplicateGroupCode() {
		when(jobGroupMapper.selectCount(any())).thenReturn(1L);
		SysJobGroupForm form = minimalForm("DUP", "重复");

		assertThatThrownBy(() -> sysJobGroupService.create(form)).isInstanceOf(SysJobException.class)
			.extracting(ex -> ((SysJobException) ex).getResultCode())
			.isEqualTo(DATA_CODE_DUPLICATE);
	}

	@Test
	@DisplayName("新增：校验通过则 insert")
	void createInsertsWhenCodeUnique() {
		when(jobGroupMapper.selectCount(any())).thenReturn(0L);
		when(jobGroupMapper.insert(org.mockito.ArgumentMatchers.<JobGroupEntity>any())).thenReturn(1);
		SysJobGroupForm form = minimalForm("UNIQ_GROUP", "唯一分组");

		sysJobGroupService.create(form);

		verify(jobGroupMapper).insert(argThat(
				(JobGroupEntity e) -> "UNIQ_GROUP".equals(e.getGroupCode()) && e.getStatus() != null && e.getStatus()));
	}

	@Test
	@DisplayName("删除：分组不存在时抛出 DATA_NOT_EXIST")
	void deleteByIdRejectsWhenMissing() {
		when(jobGroupMapper.selectById(9L)).thenReturn(null);

		assertThatThrownBy(() -> sysJobGroupService.deleteById(9L)).isInstanceOf(SysJobException.class)
			.extracting(ex -> ((SysJobException) ex).getResultCode())
			.isEqualTo(DATA_NOT_EXIST);
	}

	@Test
	@DisplayName("删除：系统内置分组不可删")
	void deleteByIdRejectsSystemGroup() {
		JobGroupEntity entity = new JobGroupEntity();
		entity.setId(1L);
		entity.setGroupCode("SYS");
		entity.setIsSystem(true);
		when(jobGroupMapper.selectById(1L)).thenReturn(entity);

		assertThatThrownBy(() -> sysJobGroupService.deleteById(1L)).isInstanceOf(SysJobException.class)
			.extracting(ex -> ((SysJobException) ex).getResultCode())
			.isEqualTo(JOB_GROUP_SYSTEM_PROTECTED);
	}

	@Test
	@DisplayName("删除：存在关联任务时抛出 JOB_GROUP_HAS_ACTIVE_JOBS")
	void deleteByIdRejectsWhenJobsExist() {
		JobGroupEntity entity = new JobGroupEntity();
		entity.setId(2L);
		entity.setGroupCode("G1");
		entity.setIsSystem(false);
		when(jobGroupMapper.selectById(2L)).thenReturn(entity);
		when(jobMapper.selectCount(any())).thenReturn(3L);

		assertThatThrownBy(() -> sysJobGroupService.deleteById(2L)).isInstanceOf(SysJobException.class)
			.extracting(ex -> ((SysJobException) ex).getResultCode())
			.isEqualTo(JOB_GROUP_HAS_ACTIVE_JOBS);
	}

	@Test
	@DisplayName("删除：非内置且无关联任务时 removeById")
	void deleteByIdRemovesWhenAllowed() {
		JobGroupEntity entity = new JobGroupEntity();
		entity.setId(3L);
		entity.setGroupCode("G2");
		entity.setIsSystem(false);
		when(jobGroupMapper.selectById(3L)).thenReturn(entity);
		when(jobMapper.selectCount(any())).thenReturn(0L);
		when(jobGroupMapper.deleteById(3L)).thenReturn(1);

		sysJobGroupService.deleteById(3L);

		verify(jobGroupMapper).deleteById(3L);
	}

}
