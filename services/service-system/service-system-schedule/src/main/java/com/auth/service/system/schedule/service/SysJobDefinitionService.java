package com.auth.service.system.schedule.service;

import com.auth.common.data.model.PageResponse;
import com.auth.service.system.schedule.model.entity.JobEntity;
import com.auth.service.system.schedule.model.form.SysJobCreateForm;
import com.auth.service.system.schedule.model.form.SysJobUpdateForm;
import com.auth.service.system.schedule.model.query.SysJobQuery;
import com.auth.service.system.schedule.model.vo.SysJobDetailVO;
import com.auth.service.system.schedule.model.vo.SysJobPageVO;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 定时任务定义服务
 *
 * @author Bunny
 */
public interface SysJobDefinitionService extends IService<JobEntity> {

	/**
	 * 分页查询任务定义
	 * @param query 查询参数
	 * @return 分页结果
	 */
	PageResponse<SysJobPageVO> getPage(SysJobQuery query);

	/**
	 * 获取任务定义详情
	 * @param id 主键
	 * @return 详情
	 */
	SysJobDetailVO getDetail(Long id);

	/**
	 * 新增任务定义并注册调度
	 * @param form 新增表单
	 */
	void create(SysJobCreateForm form);

	/**
	 * 更新任务定义并重建调度
	 * @param form 更新表单
	 */
	void update(SysJobUpdateForm form);

	/**
	 * 删除任务定义并移除调度
	 * @param id 主键
	 */
	void deleteById(Long id);

	/**
	 * 立即执行一次（不影响 Cron）
	 * @param id 主键
	 */
	void runOnce(Long id);

}
