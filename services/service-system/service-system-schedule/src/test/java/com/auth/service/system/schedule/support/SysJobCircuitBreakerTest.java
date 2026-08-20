package com.auth.service.system.schedule.support;

import com.auth.service.system.schedule.config.ScheduleJobProperties;
import com.auth.service.system.schedule.exception.SysJobException;
import com.auth.service.system.schedule.mapper.JobMapper;
import com.auth.service.system.schedule.mapper.LogJobMapper;
import com.auth.service.system.schedule.model.entity.JobEntity;
import com.auth.service.system.schedule.model.entity.LogJobEntity;
import com.auth.service.system.schedule.support.quartz.SysJobSchedulerManager;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.quartz.SchedulerException;

import java.util.List;

import static com.auth.service.system.schedule.exception.ScheduleResultCode.JOB_CIRCUIT_BREAKER_PAUSE_FAILED;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;

/**
 * {@link SysJobCircuitBreaker} 单元测试。
 */
@DisplayName("SysJobCircuitBreaker 连续失败熔断")
@ExtendWith(MockitoExtension.class)
class SysJobCircuitBreakerTest {

	@Mock
	private SysJobSchedulerManager sysJobSchedulerManager;

	@Mock
	private JobMapper jobMapper;

	@Mock
	private LogJobMapper logJobMapper;

	@Mock
	private ScheduleJobProperties scheduleJobProperties;

	@InjectMocks
	private SysJobCircuitBreaker sysJobCircuitBreaker;

	@BeforeAll
	static void initMybatisPlusTableInfo() {
		// LambdaUpdateWrapper 依赖实体元数据缓存，纯 Mockito 单测需预先初始化
		TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""), JobEntity.class);
	}

	private static List<LogJobEntity> failedLogs() {
		return java.util.stream.IntStream.range(0, 5).mapToObj(i -> new LogJobEntity().setStatus(false)).toList();
	}

	@Test
	@DisplayName("evaluateAfterFailure：运行中任务连续失败达到阈值时应置暂停并 pauseJob")
	void evaluateAfterFailure_whenActiveAndThresholdReached_pausesJob() throws SchedulerException {
		when(scheduleJobProperties.getConsecutiveFailureThreshold()).thenReturn(5);

		JobEntity job = new JobEntity();
		job.setId(1L);
		job.setStatus(true);
		when(jobMapper.selectById(1L)).thenReturn(job);
		when(logJobMapper.selectRecentByJobNameAndGroup("demoJob", "DEFAULT", 5)).thenReturn(failedLogs());

		sysJobCircuitBreaker.evaluateAfterFailure(1L, "demoJob", "DEFAULT");

		verify(jobMapper).update(isNull(), any());
		verify(jobMapper, never()).updateById(any(JobEntity.class));
		verify(sysJobSchedulerManager).pauseJob("demoJob", "DEFAULT");
	}

	@Test
	@DisplayName("evaluateAfterFailure：数据库已暂停但 Quartz 仍在跑时应补调 pauseJob")
	void evaluateAfterFailure_whenDbPausedButThresholdReached_stillPausesQuartz() throws SchedulerException {
		when(scheduleJobProperties.getConsecutiveFailureThreshold()).thenReturn(5);

		JobEntity job = new JobEntity();
		job.setId(1L);
		job.setStatus(false);
		when(jobMapper.selectById(1L)).thenReturn(job);
		when(logJobMapper.selectRecentByJobNameAndGroup("demoJob", "DEFAULT", 5)).thenReturn(failedLogs());

		sysJobCircuitBreaker.evaluateAfterFailure(1L, "demoJob", "DEFAULT");

		verify(jobMapper, never()).update(isNull(), any());
		verify(jobMapper, never()).updateById(any(JobEntity.class));
		verify(sysJobSchedulerManager).pauseJob("demoJob", "DEFAULT");
	}

	@Test
	@DisplayName("evaluateAfterFailure：运行中任务 pauseJob 失败时应抛出 SysJobException 触发回滚")
	void evaluateAfterFailure_whenPauseJobFails_throwsSysJobException() throws SchedulerException {
		when(scheduleJobProperties.getConsecutiveFailureThreshold()).thenReturn(5);

		JobEntity job = new JobEntity();
		job.setId(1L);
		job.setStatus(true);
		when(jobMapper.selectById(1L)).thenReturn(job);
		when(logJobMapper.selectRecentByJobNameAndGroup("demoJob", "DEFAULT", 5)).thenReturn(failedLogs());
		doThrow(new SchedulerException("pause failed")).when(sysJobSchedulerManager).pauseJob("demoJob", "DEFAULT");

		assertThatThrownBy(() -> sysJobCircuitBreaker.evaluateAfterFailure(1L, "demoJob", "DEFAULT"))
			.isInstanceOf(SysJobException.class)
			.extracting(ex -> ((SysJobException) ex).getResultCode())
			.isEqualTo(JOB_CIRCUIT_BREAKER_PAUSE_FAILED);
	}

}
