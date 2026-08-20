package com.auth.service.system.schedule.support;

import com.auth.service.system.schedule.config.ScheduleJobProperties;
import com.auth.service.system.schedule.exception.SysJobException;
import com.auth.service.system.schedule.mapper.JobMapper;
import com.auth.service.system.schedule.mapper.LogJobMapper;
import com.auth.service.system.schedule.model.entity.JobEntity;
import com.auth.service.system.schedule.model.entity.LogJobEntity;
import com.auth.service.system.schedule.support.quartz.SysJobSchedulerManager;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.extern.slf4j.Slf4j;
import org.quartz.SchedulerException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

import static com.auth.service.system.schedule.exception.ScheduleResultCode.JOB_CIRCUIT_BREAKER_PAUSE_FAILED;

/**
 * 根据最近执行日志连续失败次数触发熔断
 *
 * @author Bunny
 */
@Slf4j
@Component
public class SysJobCircuitBreaker {

	/**
	 * Quartz 调度管理
	 */
	private final SysJobSchedulerManager sysJobSchedulerManager;

	/**
	 * 系统任务Mapper
	 */
	private final JobMapper jobMapper;

	/**
	 * 系统任务日志Mapper
	 */
	private final LogJobMapper logJobMapper;

	/**
	 * 定时任务配置
	 */
	private final ScheduleJobProperties scheduleJobProperties;

	public SysJobCircuitBreaker(SysJobSchedulerManager sysJobSchedulerManager, JobMapper jobMapper,
			LogJobMapper logJobMapper, ScheduleJobProperties scheduleJobProperties) {
		this.sysJobSchedulerManager = sysJobSchedulerManager;
		this.jobMapper = jobMapper;
		this.logJobMapper = logJobMapper;
		this.scheduleJobProperties = scheduleJobProperties;
	}

	/**
	 * 在写入一条失败日志后评估是否熔断
	 * @param jobId 业务任务主键（可为空则跳过）
	 * @param jobName 任务名
	 * @param jobGroup 分组
	 */
	@Transactional(rollbackFor = Exception.class)
	public void evaluateAfterFailure(Long jobId, String jobName, String jobGroup) {
		int threshold = scheduleJobProperties.getConsecutiveFailureThreshold();
		if (threshold <= 0 || jobId == null) {
			return;
		}

		// 1. 检查任务是否已暂停
		JobEntity job = jobMapper.selectById(jobId);
		if (job == null) {
			return;
		}

		// 2. 查询最近连续失败日志
		List<LogJobEntity> recent = logJobMapper.selectRecentByJobNameAndGroup(jobName, jobGroup, threshold);
		if (recent.size() < threshold) {
			return;
		}
		// 失败日志约定：status=false 表示失败（log_job.status=0）
		boolean allFailed = recent.stream().allMatch(log -> log.getStatus() != null && !log.getStatus());
		if (!allFailed) {
			return;
		}

		// 3. 检查任务是否已暂停
		boolean wasActive = job.getStatus() != null && job.getStatus();
		if (wasActive) {
			jobMapper.update(null,
					Wrappers.lambdaUpdate(JobEntity.class)
						.set(JobEntity::getStatus, false)
						.set(JobEntity::getUpdatedAt, Instant.now())
						.eq(JobEntity::getId, jobId));
		}

		// 4. 暂停任务
		try {
			sysJobSchedulerManager.pauseJob(jobName, jobGroup);
		}
		catch (SchedulerException ex) {
			log.error("熔断后暂停 Quartz 任务失败: {}.{}", jobGroup, jobName, ex);
			if (wasActive) {
				throw new SysJobException(JOB_CIRCUIT_BREAKER_PAUSE_FAILED, ex, jobGroup, jobName);
			}
			return;
		}

		log.warn("任务已连续失败 {} 次，已暂停: {} / {}", threshold, jobGroup, jobName);
	}

}
