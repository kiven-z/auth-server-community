package com.auth.service.system.schedule.support.quartz;

import cn.hutool.core.collection.CollUtil;
import com.auth.common.core.constants.BatchSizes;
import com.auth.service.system.schedule.mapper.JobMapper;
import com.auth.service.system.schedule.model.entity.JobEntity;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.SchedulerException;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 按 job 补偿 Quartz 运行态（DB 为单一事实来源）
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Slf4j
@Component
public class SysJobScheduleReconciler {

	private final JobMapper jobMapper;

	private final SysJobSchedulerManager sysJobSchedulerManager;

	/**
	 * 全量对账：启用与暂停任务分别分页同步
	 */
	public void reconcileAll() {
		reconcileByStatus(true);
		reconcileByStatus(false);
	}

	/**
	 * 按任务主键对账
	 * @param jobIds 任务 ID 列表
	 */
	public void reconcileByJobIds(List<Long> jobIds) {
		if (CollUtil.isEmpty(jobIds)) {
			return;
		}

		// 查询任务列表
		List<JobEntity> jobs = jobMapper.selectByIds(jobIds);
		for (JobEntity job : jobs) {
			reconcileSingle(job);
		}
	}

	/**
	 * 按分组编码对账
	 * @param groupCode 分组编码
	 */
	public void reconcileByGroupCode(String groupCode) {
		List<JobEntity> jobs = jobMapper
			.selectList(Wrappers.<JobEntity>lambdaQuery().eq(JobEntity::getJobGroup, groupCode));

		// 对账任务
		for (JobEntity job : jobs) {
			reconcileSingle(job);
		}
	}

	/**
	 * 按状态对账
	 * @param enabled 是否启用
	 */
	private void reconcileByStatus(boolean enabled) {
		var query = Wrappers.lambdaQuery(JobEntity.class)
			.eq(JobEntity::getStatus, enabled)
			.orderByAsc(JobEntity::getId);

		// 查询第一页
		IPage<JobEntity> firstPage = jobMapper.selectPage(new Page<>(1, BatchSizes.SIZE_500), query);
		long totalPages = firstPage.getPages();

		// 遍历所有页
		for (long pageNum = 1; pageNum <= totalPages; pageNum++) {
			IPage<JobEntity> result = pageNum == 1 ? firstPage
					: jobMapper.selectPage(new Page<>(pageNum, BatchSizes.SIZE_500), query);

			// 对账任务
			for (JobEntity job : result.getRecords()) {
				reconcileSingle(job);
			}
		}
	}

	/**
	 * 单个对账
	 * @param job 任务实体
	 */
	private void reconcileSingle(JobEntity job) {
		try {
			if (job.getStatus() != null && job.getStatus()) {
				sysJobSchedulerManager.scheduleOrReplace(job);
				return;
			}

			sysJobSchedulerManager.pauseJob(job.getJobName(), job.getJobGroup());
		}
		catch (SchedulerException exception) {
			log.error("Quartz reconcile failed status={} {} / {}", job.getStatus(), job.getJobGroup(), job.getJobName(),
					exception);
		}
	}

}
