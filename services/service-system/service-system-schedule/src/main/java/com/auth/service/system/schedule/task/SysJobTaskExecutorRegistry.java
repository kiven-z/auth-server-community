package com.auth.service.system.schedule.task;

import com.auth.service.system.schedule.exception.SysJobException;
import com.auth.service.system.schedule.model.enums.SysJobTaskType;
import com.auth.service.system.schedule.task.executor.SysJobTaskExecutor;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import static com.auth.service.system.schedule.exception.ScheduleResultCode.JOB_TASK_TYPE_UNSUPPORTED;

/**
 * 任务类型执行器注册中心
 *
 * @author Bunny
 */
@Component
public class SysJobTaskExecutorRegistry {

	private final Map<SysJobTaskType, SysJobTaskExecutor> executors;

	public SysJobTaskExecutorRegistry(List<SysJobTaskExecutor> executorList) {
		this.executors = new EnumMap<>(SysJobTaskType.class);
		for (SysJobTaskExecutor executor : executorList) {
			this.executors.put(executor.taskType(), executor);
		}
	}

	/**
	 * 解析任务执行器
	 * @param taskType 任务类型
	 * @return 执行器
	 */
	public SysJobTaskExecutor resolve(SysJobTaskType taskType) {
		SysJobTaskExecutor executor = executors.get(taskType);
		if (executor == null) {
			throw new SysJobException(JOB_TASK_TYPE_UNSUPPORTED, taskType.name());
		}
		return executor;
	}

}
