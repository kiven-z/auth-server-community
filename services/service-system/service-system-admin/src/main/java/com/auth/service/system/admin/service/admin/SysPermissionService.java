package com.auth.service.system.admin.service.admin;

import com.auth.common.core.model.form.IdsEnableStatusForm;
import com.auth.common.data.model.PageResponse;
import com.auth.service.system.admin.model.entity.SysPermissionEntity;
import com.auth.service.system.admin.model.form.permission.SysPermissionForm;
import com.auth.service.system.admin.model.query.permission.SysPermissionQuery;
import com.auth.service.system.admin.model.vo.permission.SysPermissionDetailVO;
import com.auth.service.system.admin.model.vo.permission.SysPermissionPageVO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 系统权限服务
 *
 * @author Bunny
 */
public interface SysPermissionService extends IService<SysPermissionEntity> {

	/**
	 * 分页查询权限
	 * @param query 查询条件
	 * @return 分页数据
	 */
	PageResponse<SysPermissionPageVO> getPage(SysPermissionQuery query);

	/**
	 * 获取权限详情
	 * @param id 权限主键
	 * @return 详情 VO
	 */
	SysPermissionDetailVO getDetail(Long id);

	/**
	 * 批量新增权限
	 * @param forms 新增表单列表
	 */
	void createBatchFromImport(List<SysPermissionForm> forms);

	/**
	 * 更新权限
	 * @param form 更新表单
	 */
	void update(SysPermissionForm form);

	/**
	 * 批量启停权限
	 * @param form 主键列表与目标状态
	 */
	void batchUpdateStatus(IdsEnableStatusForm form);

	/**
	 * 删除权限（物理删除）
	 * @param id 权限主键
	 */
	void deleteById(Long id);

}
