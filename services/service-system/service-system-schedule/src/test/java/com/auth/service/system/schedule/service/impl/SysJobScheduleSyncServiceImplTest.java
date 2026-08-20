package com.auth.service.system.schedule.service.impl;

import com.auth.service.system.schedule.exception.SysJobException;
import com.auth.service.system.schedule.mapper.JobMapper;
import com.auth.service.system.schedule.model.entity.JobEntity;
import com.auth.common.core.model.form.IdsEnableStatusForm;
import com.auth.service.system.schedule.support.quartz.SysJobScheduleReconciler;
import com.auth.service.system.schedule.support.quartz.SysJobSchedulerManager;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.extension.repository.CrudRepository;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.quartz.SchedulerException;

import java.lang.reflect.Field;
import java.util.List;

import static com.auth.service.system.schedule.exception.ScheduleResultCode.JOB_QUARTZ_OPERATION_FAILED;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * {@link SysJobScheduleSyncServiceImpl} 单元测试。
 *
 * @author Bunny
 */
@DisplayName("SysJobScheduleSyncServiceImpl 运行态同步")
@ExtendWith(MockitoExtension.class)
class SysJobScheduleSyncServiceImplTest {

	@Mock
	private JobMapper jobMapper;

	@Mock
	private SysJobSchedulerManager sysJobSchedulerManager;

	@Mock
	private SysJobScheduleReconciler sysJobScheduleReconciler;

	private SysJobScheduleSyncServiceImpl sysJobScheduleSyncService;

	@BeforeAll
	static void initMybatisPlusTableInfo() {
		TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""), JobEntity.class);
	}

	@BeforeEach
	void setUp() throws Exception {
		sysJobScheduleSyncService = spy(
				new SysJobScheduleSyncServiceImpl(sysJobSchedulerManager, sysJobScheduleReconciler));
		Field baseMapperField = CrudRepository.class.getDeclaredField("baseMapper");
		baseMapperField.setAccessible(true);
		baseMapperField.set(sysJobScheduleSyncService, jobMapper);
		lenient().doReturn(true).when(sysJobScheduleSyncService).updateBatchById(anyList());
	}

	@Test
	@DisplayName("批量停用：批量落库并 pauseJob")
	void batchUpdateStatusSkipsMissingAndPausesQuartz() throws SchedulerException {
		JobEntity entity = new JobEntity();
		entity.setId(1L);
		entity.setJobName("demoJob");
		entity.setJobGroup("DEFAULT");
		entity.setStatus(true);
		when(jobMapper.selectList(any())).thenReturn(List.of(entity));

		IdsEnableStatusForm form = new IdsEnableStatusForm();
		form.setIds(List.of(1L, 2L));
		form.setStatus(false);

		sysJobScheduleSyncService.batchUpdateStatus(form);

		verify(sysJobScheduleSyncService).updateBatchById(anyList());
		verify(sysJobSchedulerManager).pauseJob("demoJob", "DEFAULT");
		verify(sysJobSchedulerManager, never()).scheduleOrReplace(any());
	}

	@Test
	@DisplayName("批量启用：仅对原暂停任务注册 Quartz")
	void batchUpdateStatusEnableOnlySchedulesPreviouslyPausedJobs() throws SchedulerException {
		JobEntity paused = new JobEntity();
		paused.setId(2L);
		paused.setJobName("jobB");
		paused.setJobGroup("DEFAULT");
		paused.setStatus(false);
		when(jobMapper.selectList(any())).thenReturn(List.of(paused));

		IdsEnableStatusForm form = new IdsEnableStatusForm();
		form.setIds(List.of(1L, 2L));
		form.setStatus(true);

		sysJobScheduleSyncService.batchUpdateStatus(form);

		verify(sysJobSchedulerManager, times(1)).scheduleOrReplace(any());
		verify(sysJobSchedulerManager)
			.scheduleOrReplace(argThat(job -> job.getId().equals(2L) && job.getStatus() != null && job.getStatus()));
	}

	@Test
	@DisplayName("分组批量停用：无任务时仍 pauseGroup")
	void batchUpdateStatusByGroupCodePauseEmptyGroupStillPausesQuartz() throws SchedulerException {
		when(jobMapper.selectList(any())).thenReturn(List.of());

		sysJobScheduleSyncService.batchUpdateStatusByGroupCode("G1", false);

		verify(sysJobSchedulerManager).pauseGroup("G1");
		verify(sysJobScheduleSyncService, never()).updateBatchById(anyList());
	}

	@Test
	@DisplayName("分组批量启用：落库并 scheduleOrReplace 分组内全部任务")
	void batchUpdateStatusByGroupCodeEnableSchedulesAllJobsInGroup() throws SchedulerException {
		JobEntity job = new JobEntity();
		job.setId(1L);
		job.setJobName("demoJob");
		job.setJobGroup("G1");
		job.setStatus(false);
		when(jobMapper.selectList(any())).thenReturn(List.of(job));

		sysJobScheduleSyncService.batchUpdateStatusByGroupCode("G1", true);

		verify(sysJobScheduleSyncService).updateBatchById(anyList());
		verify(sysJobSchedulerManager).scheduleOrReplace(
				argThat(entity -> entity.getId().equals(1L) && entity.getStatus() != null && entity.getStatus()));
	}

	@Test
	@DisplayName("分组批量启停：Quartz 同步失败时抛出 JOB_QUARTZ_OPERATION_FAILED")
	void batchUpdateStatusByGroupCodeThrowsWhenQuartzSyncFails() throws SchedulerException {
		when(jobMapper.selectList(any())).thenReturn(List.of());
		doThrow(new SchedulerException("pause failed")).when(sysJobSchedulerManager).pauseGroup("G1");

		assertThatThrownBy(() -> sysJobScheduleSyncService.batchUpdateStatusByGroupCode("G1", false))
			.isInstanceOf(SysJobException.class)
			.extracting(ex -> ((SysJobException) ex).getResultCode())
			.isEqualTo(JOB_QUARTZ_OPERATION_FAILED);
	}

}
