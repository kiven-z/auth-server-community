package com.auth.service.system.schedule.task.support;

import cn.hutool.core.text.CharSequenceUtil;
import com.auth.service.system.schedule.exception.SysJobException;
import com.auth.service.system.schedule.model.constants.SysJobQuartzDataKeys;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.springframework.stereotype.Component;

import static com.auth.service.system.common.exception.code.SystemCommonResultCode.PARAM_REQUIRED;
import static com.auth.service.system.schedule.exception.ScheduleResultCode.JOB_CONFIG_INVALID;

/**
 * 内置任务 job_params JSON 反序列化（运行时）
 *
 * @author Bunny
 */
@Component
public class BuiltinJobParamsParser {

	private final ObjectMapper objectMapper;

	public BuiltinJobParamsParser(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	/**
	 * 从 {@link JobExecutionContext} 读取并解析 job_params
	 * @param context Quartz 执行上下文
	 * @param paramsType 载荷类型
	 * @param <T> 载荷类型
	 * @return 解析后的载荷
	 */
	public <T> T parse(JobExecutionContext context, Class<T> paramsType) {
		JobDataMap jobDataMap = context.getMergedJobDataMap();
		String raw = jobDataMap.getString(SysJobQuartzDataKeys.JOB_PARAMS);
		if (CharSequenceUtil.isBlank(raw)) {
			throw new SysJobException(PARAM_REQUIRED, "job_params");
		}
		return parse(raw, paramsType);
	}

	/**
	 * 反序列化 job_params JSON，不进行空值检查（调用方需保证传入非空）
	 * @param jobParamsJson 原始 JSON
	 * @param paramsType 载荷类型
	 * @param <T> 载荷类型
	 * @return 解析后的载荷
	 */
	public <T> T parse(String jobParamsJson, Class<T> paramsType) {
		try {
			return objectMapper.readValue(jobParamsJson, paramsType);
		}
		catch (Exception exception) {
			throw new SysJobException(JOB_CONFIG_INVALID, "job_params", exception.getMessage());
		}
	}

}
