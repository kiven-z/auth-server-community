package com.auth.service.system.schedule.support.quartz;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.auth.service.system.schedule.model.constants.SysJobQuartzDataKeys;
import com.auth.service.system.schedule.model.entity.JobEntity;
import com.auth.service.system.schedule.model.enums.SysJobTaskType;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.springframework.stereotype.Component;

/**
 * 根据 {@link JobEntity} 构建 Quartz {@link JobDetail}
 *
 * @author Bunny
 */
@Slf4j
@Component
public class SysJobQuartzJobFactory {

	/**
	 * 合并 JSON 参数
	 * @param map 参数映射
	 * @param jobParamsJson 参数 JSON
	 */
	private static void mergeJsonParams(JobDataMap map, String jobParamsJson) {
		if (CharSequenceUtil.isBlank(jobParamsJson)) {
			return;
		}
		try {
			JSONObject json = JSONUtil.parseObj(jobParamsJson);
			map.putAll(json);
		}
		catch (Exception exception) {
			log.warn("解析 job_params 失败: {}", jobParamsJson, exception);
			map.put("jobParamsRaw", jobParamsJson);
		}
	}

	/**
	 * 构建 JobDetail
	 * @param job 任务实体
	 * @return JobDetail
	 */
	public JobDetail build(JobEntity job) {
		// 1. 获取任务名称、分组、任务类型、并发标志
		String jobName = job.getJobName();
		String jobGroup = job.getJobGroup();
		SysJobTaskType resolvedTaskType = SysJobTaskType.resolve(job.getTaskType());
		boolean concurrent = job.getConcurrent() != null && job.getConcurrent();
		Class<? extends Job> jobClass = resolvedTaskType.resolveEntryJobClass(concurrent);

		// 2. 构建 JobDataMap
		JobDataMap map = new JobDataMap();
		map.put(SysJobQuartzDataKeys.SYS_JOB_ID, String.valueOf(job.getId()));
		map.put(SysJobQuartzDataKeys.INVOKE_TARGET, job.getInvokeTarget());
		map.put(SysJobQuartzDataKeys.JOB_CLASS, job.getJobClass());
		map.put(SysJobQuartzDataKeys.TASK_TYPE, resolvedTaskType.name());
		String handlerCode = CharSequenceUtil.isNotBlank(job.getHandlerCode()) ? job.getHandlerCode()
				: resolvedTaskType.defaultHandlerCode();
		map.put(SysJobQuartzDataKeys.HANDLER_CODE, handlerCode);
		map.put(SysJobQuartzDataKeys.PAYLOAD_JSON, job.getPayloadJson());
		map.put(SysJobQuartzDataKeys.JOB_PARAMS, job.getJobParams());
		map.put(SysJobQuartzDataKeys.TRIGGER_TYPE, SysJobQuartzDataKeys.TRIGGER_TYPE_SCHEDULE);
		mergeJsonParams(map, job.getJobParams());

		// 3. 构建 JobDetail
		return JobBuilder.newJob(jobClass).withIdentity(jobName, jobGroup).usingJobData(map).storeDurably().build();
	}

}
