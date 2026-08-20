package com.auth.service.system.admin.convert.authorization;

import com.auth.common.mapstruct.config.AuthMapperConfig;
import com.auth.service.system.admin.model.po.dept.SysDeptBoundUserPO;
import com.auth.service.system.admin.model.po.permission.SysPermissionBoundMenuPO;
import com.auth.service.system.admin.model.po.post.SysPostBoundUserPO;
import com.auth.service.system.admin.model.vo.permission.SysPermissionBoundMenuItemVO;
import com.auth.service.system.admin.model.vo.reference.ext.DeptBoundUserReferenceVO;
import com.auth.service.system.admin.model.vo.reference.ext.PostBoundUserReferenceVO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 授权面分页行转换
 *
 * @author Bunny
 */
@Mapper(config = AuthMapperConfig.class)
public interface AuthorizationSurfaceConverter {

	AuthorizationSurfaceConverter INSTANCE = Mappers.getMapper(AuthorizationSurfaceConverter.class);

	/**
	 * 部门详情-关联用户 PO → 部门关联用户回显
	 * @param po 持久层投影
	 * @return 部门关联用户回显
	 */
	DeptBoundUserReferenceVO toDeptBoundUserReference(SysDeptBoundUserPO po);

	/**
	 * 部门详情-关联用户 PO 列表 → 部门关联用户回显列表
	 * @param poList 持久层投影列表
	 * @return 部门关联用户回显列表
	 */
	List<DeptBoundUserReferenceVO> toDeptBoundUserReferenceList(List<SysDeptBoundUserPO> poList);

	/**
	 * 岗位详情-关联用户 PO → 岗位关联用户回显
	 * @param po 持久层投影
	 * @return 岗位关联用户回显
	 */
	PostBoundUserReferenceVO toPostBoundUserReference(SysPostBoundUserPO po);

	/**
	 * 岗位详情-关联用户 PO 列表 → 岗位关联用户回显列表
	 * @param poList 持久层投影列表
	 * @return 岗位关联用户回显列表
	 */
	List<PostBoundUserReferenceVO> toPostBoundUserReferenceList(List<SysPostBoundUserPO> poList);

	/**
	 * 角色已绑定菜单 PO → 详情项 VO
	 * @param po 持久层投影
	 * @return 详情项 VO
	 */
	SysPermissionBoundMenuItemVO toBoundMenuItem(SysPermissionBoundMenuPO po);

	/**
	 * 角色已绑定菜单 PO 列表 → 详情项 VO 列表
	 * @param poList 持久层投影列表
	 * @return 详情项 VO 列表
	 */
	List<SysPermissionBoundMenuItemVO> toBoundMenuItemList(List<SysPermissionBoundMenuPO> poList);

}
