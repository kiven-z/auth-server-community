package com.auth.service.system.schedule.service.impl;

import cn.hutool.core.text.CharSequenceUtil;
import com.auth.service.system.schedule.exception.SysJobException;
import com.auth.service.system.schedule.model.vo.QuartzTaskClassVO;
import com.auth.service.system.schedule.model.vo.QuartzTaskMethodVO;
import com.auth.service.system.schedule.service.SysJobTaskCatalogService;
import com.auth.service.system.schedule.support.catalog.QuartzTaskRegistry;
import com.auth.service.system.schedule.task.support.InvokeTargetSupport;
import org.springframework.stereotype.Service;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static com.auth.service.system.common.exception.code.SystemCommonResultCode.PARAM_REQUIRED;
import static com.auth.service.system.schedule.exception.ScheduleResultCode.JOB_CLASS_INVALID;

/**
 * 任务目录服务实现
 *
 * @author Bunny
 */
@Service
public class SysJobTaskCatalogServiceImpl implements SysJobTaskCatalogService {

	private final QuartzTaskRegistry quartzTaskRegistry;

	public SysJobTaskCatalogServiceImpl(QuartzTaskRegistry quartzTaskRegistry) {
		this.quartzTaskRegistry = quartzTaskRegistry;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<QuartzTaskClassVO> listQuartzClasses() {
		return quartzTaskRegistry.listAll();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<QuartzTaskMethodVO> listCallableMethods(String className) {
		// 验证类名是否为空
		if (CharSequenceUtil.isBlank(className)) {
			throw new SysJobException(PARAM_REQUIRED, "job_class");
		}
		// 验证类名是否在白名单
		if (!quartzTaskRegistry.isAllowed(className)) {
			throw new SysJobException(JOB_CLASS_INVALID, "not in whitelist");
		}
		// 解析类名对应的 Bean 名称
		String beanName = quartzTaskRegistry.resolveBeanName(className);
		if (CharSequenceUtil.isBlank(beanName)) {
			throw new SysJobException(JOB_CLASS_INVALID, "not in whitelist");
		}

		Class<?> targetClass;
		try {
			targetClass = Class.forName(className);
		}
		catch (ClassNotFoundException exception) {
			throw new SysJobException(JOB_CLASS_INVALID, exception.getMessage());
		}

		// 获取方法列表
		List<QuartzTaskMethodVO> methodList = new ArrayList<>();
		for (Method method : targetClass.getDeclaredMethods()) {
			if (!InvokeTargetSupport.isCallableMethod(method)) {
				continue;
			}
			methodList.add(InvokeTargetSupport.toMethodCatalog(beanName, method));
		}
		return methodList;
	}

}
