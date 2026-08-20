package com.auth.service.system.schedule.service;

import com.auth.service.system.schedule.model.vo.QuartzTaskClassVO;
import com.auth.service.system.schedule.model.vo.QuartzTaskMethodVO;

import java.util.List;

/**
 * 任务目录服务（白名单类与可选方法）
 *
 * @author Bunny
 */
public interface SysJobTaskCatalogService {

	/**
	 * 查询白名单任务类列表
	 * @return 白名单类列表
	 */
	List<QuartzTaskClassVO> listQuartzClasses();

	/**
	 * 查询白名单类可执行方法（仅无参或单 String 参）
	 * @param className 类全限定名
	 * @return 方法列表
	 */
	List<QuartzTaskMethodVO> listCallableMethods(String className);

}
