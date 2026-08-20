package com.auth.service.system.schedule.annotation;

import com.auth.service.system.schedule.model.enums.SysJobTaskType;

import java.lang.annotation.*;

/**
 * 标记可被定时任务安全调度的 Spring Bean 类型；启动时扫描并作为反射白名单
 *
 * @author Bunny
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface QuartzTask {

	/**
	 * 任务展示名称（前端下拉）
	 * @return 名称
	 */
	String name();

	/**
	 * 任务说明
	 * @return 描述
	 */
	String description();

	/**
	 * 适用的调度模式（Bean 调用 / 自定义类）
	 * @return 模式列表
	 */
	SysJobTaskType[] modes() default { SysJobTaskType.BEAN_INVOKE };

	String example() default "";

}
