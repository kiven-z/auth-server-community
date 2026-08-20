package com.auth.service.system.admin.service.admin;

import com.auth.common.core.model.form.IdsEnableStatusForm;
import com.auth.common.data.model.PageResponse;
import com.auth.service.system.admin.model.entity.SysPostEntity;
import com.auth.service.system.admin.model.form.post.SysPostForm;
import com.auth.service.system.admin.model.query.post.SysPostQuery;
import com.auth.service.system.admin.model.vo.post.SysPostDetailVO;
import com.auth.service.system.admin.model.vo.post.SysPostPageVO;
import com.auth.service.system.admin.model.vo.post.SysPostSearchItemVO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 系统岗位服务
 *
 * @author Bunny
 */
public interface SysPostService extends IService<SysPostEntity> {

	/**
	 * 分页查询岗位
	 * @param query 查询条件
	 * @return 分页数据
	 */
	PageResponse<SysPostPageVO> getPage(SysPostQuery query);

	/**
	 * 按关键词搜索岗位（编码/名称前缀）
	 * @param keyword 搜索关键字（前缀匹配，可选）
	 * @param status 启用状态
	 * @param limit 返回条数上限，默认 20，最大 50
	 * @return 搜索项列表
	 */
	List<SysPostSearchItemVO> search(String keyword, Boolean status, Integer limit);

	/**
	 * 获取岗位详情（含所属部门、关联用户、已授权角色）
	 * @param id 岗位主键
	 * @return 详情
	 */
	SysPostDetailVO getDetail(Long id);

	/**
	 * 批量新增岗位
	 * @param forms 新增表单列表
	 */
	void createBatchFromImport(List<SysPostForm> forms);

	/**
	 * 更新岗位（改挂编制部门时目标部门须自身启用）
	 * @param form 表单（须含主键；字段与新增一致）
	 */
	void update(SysPostForm form);

	/**
	 * 批量启停岗位
	 * @param form 主键列表与目标状态
	 */
	void batchUpdateStatus(IdsEnableStatusForm form);

	/**
	 * 删除岗位（存在引用时拒绝，成功后触发授权失效）
	 * @param id 岗位主键
	 */
	void deleteById(Long id);

}
