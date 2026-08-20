package com.auth.service.system.admin.service.admin;

import com.auth.common.core.model.form.IdsEnableStatusForm;
import com.auth.common.data.model.PageResponse;
import com.auth.service.system.admin.model.entity.SysRoleEntity;
import com.auth.service.system.admin.model.form.role.SysRoleForm;
import com.auth.service.system.admin.model.query.role.SysRoleQuery;
import com.auth.service.system.admin.model.vo.role.SysRoleDetailVO;
import com.auth.service.system.admin.model.vo.role.SysRoleOptionVO;
import com.auth.service.system.admin.model.vo.role.SysRolePageVO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 系统角色服务
 *
 * @author Bunny
 */
public interface SysRoleService extends IService<SysRoleEntity> {

	/**
	 * 分页查询角色
	 * @param query 查询条件
	 * @return 分页数据
	 */
	PageResponse<SysRolePageVO> getPage(SysRoleQuery query);

	/**
	 * 获取角色详情（含禁用）
	 * @param id 角色 ID
	 * @return 角色详情
	 */
	SysRoleDetailVO getDetail(Long id);

	/**
	 * 查询角色下拉选项
	 * @param roleName 角色名称（可选模糊）
	 * @param roleCode 角色编码（可选模糊）
	 * @return 下拉选项列表
	 */
	List<SysRoleOptionVO> listOptions(String roleName, String roleCode);

	/**
	 * 批量新增角色
	 * @param forms 新增表单列表
	 */
	void createBatchFromImport(List<SysRoleForm> forms);

	/**
	 * 更新角色
	 * @param form 保存表单
	 */
	void update(SysRoleForm form);

	/**
	 * 批量启停角色
	 * @param form 主键列表与目标状态
	 */
	void batchUpdateStatus(IdsEnableStatusForm form);

	/**
	 * 删除角色
	 * @param id 角色主键
	 */
	void deleteById(Long id);

}
