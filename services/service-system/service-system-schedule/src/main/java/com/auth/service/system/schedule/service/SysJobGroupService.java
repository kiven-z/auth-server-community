package com.auth.service.system.schedule.service;

import com.auth.common.data.model.PageResponse;
import com.auth.service.system.schedule.model.form.SysJobGroupForm;
import com.auth.service.system.schedule.model.form.SysJobGroupUpdateForm;
import com.auth.service.system.schedule.model.query.SysJobGroupQuery;
import com.auth.service.system.schedule.model.vo.SysJobGroupDetailVO;
import com.auth.service.system.schedule.model.vo.SysJobGroupPageVO;

import java.util.List;

/**
 * 任务分组服务
 *
 * @author Bunny
 */
public interface SysJobGroupService {

	/**
	 * 分页查询任务分组
	 * @param query 查询参数
	 * @return 分页数据
	 */
	PageResponse<SysJobGroupPageVO> getPage(SysJobGroupQuery query);

	/**
	 * 查询启用状态任务分组选项
	 * @param keyword 关键字（可空）
	 * @param limit 最大条数
	 * @return 列表
	 */
	List<SysJobGroupPageVO> listEnabledOptions(String keyword, int limit);

	/**
	 * 获取任务分组详情
	 * @param id 主键
	 * @return 详情
	 */
	SysJobGroupDetailVO getDetail(Long id);

	/**
	 * 新增任务分组
	 * @param form 表单
	 */
	void create(SysJobGroupForm form);

	/**
	 * 更新任务分组（不含分组编码）
	 * @param form 表单
	 */
	void update(SysJobGroupUpdateForm form);

	/**
	 * 删除任务分组（非内置且无关联任务）
	 * @param id 主键
	 */
	void deleteById(Long id);

}
