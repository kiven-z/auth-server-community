package com.auth.service.system.admin.service.admin;

import com.auth.common.core.model.form.IdsEnableStatusForm;
import com.auth.common.data.model.PageResponse;
import com.auth.service.system.admin.model.entity.SysMenuEntity;
import com.auth.service.system.admin.model.form.menu.SysMenuAssignRoleForm;
import com.auth.service.system.admin.model.form.menu.SysMenuMoveForm;
import com.auth.service.system.admin.model.form.menu.SysMenuSaveForm;
import com.auth.service.system.admin.model.query.menu.SysMenuQuery;
import com.auth.service.system.admin.model.vo.menu.RouteNodeVO;
import com.auth.service.system.admin.model.vo.menu.SysMenuDetailVO;
import com.auth.service.system.admin.model.vo.menu.SysMenuListVO;
import com.auth.service.system.admin.model.vo.reference.ext.MenuAssignedRoleReferenceVO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 系统菜单服务
 *
 * @author Bunny
 */
public interface SysMenuService extends IService<SysMenuEntity> {

	/**
	 * 查询指定用户可见的 Web 动态路由列表（含子级授权时的祖先壳）
	 * @param userId 当前用户 ID，null 时返回空列表
	 * @return 顶级路由节点，可能为空
	 */
	List<RouteNodeVO> listWebRoutes(Long userId);

	/**
	 * 查询菜单列表
	 * @param query 筛选条件
	 * @return 菜单行
	 */
	List<SysMenuListVO> listFlat(SysMenuQuery query);

	/**
	 * 分页查询菜单
	 * @param query 筛选与分页
	 * @return 分页结果
	 */
	PageResponse<SysMenuListVO> pageFlat(SysMenuQuery query);

	/**
	 * 获取菜单详情
	 * @param id 主键
	 * @return 详情
	 */
	SysMenuDetailVO getDetail(Long id);

	/**
	 * 查询菜单已分配角色
	 * @param menuId 菜单主键
	 * @return 已分配行
	 */
	List<MenuAssignedRoleReferenceVO> listAssignedRoles(Long menuId);

	/**
	 * 新增菜单
	 * @param form 表单
	 * @return 新菜单主键
	 */
	Long create(SysMenuSaveForm form);

	/**
	 * 更新菜单
	 * @param form 表单（含 id）
	 */
	void update(SysMenuSaveForm form);

	/**
	 * 移动菜单（变更父菜单）
	 * @param menuId 菜单主键
	 * @param form 新父菜单
	 */
	void move(Long menuId, SysMenuMoveForm form);

	/**
	 * 批量启停菜单
	 * @param form 请求体
	 */
	void batchUpdateStatus(IdsEnableStatusForm form);

	/**
	 * 批量删除菜单（存在子菜单时拒绝）
	 * @param ids 菜单主键列表
	 */
	void deleteByIds(List<Long> ids);

	/**
	 * 全量覆盖菜单角色关联
	 * @param menuId 菜单主键
	 * @param form 角色 ID 列表
	 */
	void replaceMenuRoles(Long menuId, SysMenuAssignRoleForm form);

}
