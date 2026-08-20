package com.auth.service.system.schedule.task.example;

import com.auth.service.system.schedule.annotation.QuartzTask;
import com.auth.service.system.schedule.model.enums.SysJobTaskType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Bean 调用模式示例：通过 invoke_target 反射调用无参或单 String 参数方法
 *
 * @author Bunny
 */
@QuartzTask(name = "BeanInvokeDemoTask", description = "示例：无参 run() 与 runWithMessage(String) 供 Bean 调用",
		modes = { SysJobTaskType.BEAN_INVOKE })
@Slf4j
@Component
public class BeanInvokeDemoTask {

	/**
	 * 无参示例方法
	 */
	public void run() {
		log.info("BeanInvokeDemoTask.run() ok");
	}

	/**
	 * 单 String 参数示例方法
	 * @param message 演示参数
	 */
	public void runWithMessage(String message) {
		log.info("BeanInvokeDemoTask.runWithMessage() message={}", message);
	}

}
