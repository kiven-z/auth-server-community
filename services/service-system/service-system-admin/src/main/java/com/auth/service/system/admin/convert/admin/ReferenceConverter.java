package com.auth.service.system.admin.convert.admin;

import com.auth.common.mapstruct.config.AuthMapperConfig;
import com.auth.service.system.admin.model.po.reference.*;
import com.auth.service.system.admin.model.po.user.UserDeptProfilePO;
import com.auth.service.system.admin.model.po.user.UserPostProfilePO;
import com.auth.service.system.admin.model.vo.reference.*;
import com.auth.service.system.admin.model.vo.reference.ext.MenuAssignedRoleReferenceVO;
import com.auth.service.system.admin.model.vo.reference.ext.UserDeptReferenceVO;
import com.auth.service.system.admin.model.vo.reference.ext.UserPostReferenceVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 关联回显 PO → ReferenceVO 集中转换
 *
 * @author Bunny
 */
@Mapper(config = AuthMapperConfig.class)
public interface ReferenceConverter {

	ReferenceConverter INSTANCE = Mappers.getMapper(ReferenceConverter.class);

	/**
	 * 角色关联 PO → 角色回显
	 * @param po 持久层投影
	 * @return 角色回显
	 */
	RoleReferenceVO toRoleReference(RoleReferencePO po);

	/**
	 * 角色关联 PO 列表 → 角色回显列表
	 * @param poList 持久层投影列表
	 * @return 角色回显列表
	 */
	List<RoleReferenceVO> toRoleReferenceList(List<RoleReferencePO> poList);

	/**
	 * 用户关联 PO → 用户回显
	 * @param po 持久层投影
	 * @return 用户回显
	 */
	UserReferenceVO toUserReference(UserReferencePO po);

	/**
	 * 用户关联 PO 列表 → 用户回显列表
	 * @param poList 持久层投影列表
	 * @return 用户回显列表
	 */
	List<UserReferenceVO> toUserReferenceList(List<UserReferencePO> poList);

	/**
	 * 部门关联 PO → 部门回显
	 * @param po 持久层投影
	 * @return 部门回显
	 */
	DeptReferenceVO toDeptReference(DeptReferencePO po);

	/**
	 * 部门关联 PO 列表 → 部门回显列表
	 * @param poList 持久层投影列表
	 * @return 部门回显列表
	 */
	List<DeptReferenceVO> toDeptReferenceList(List<DeptReferencePO> poList);

	/**
	 * 岗位关联 PO → 岗位回显
	 * @param po 持久层投影
	 * @return 岗位回显
	 */
	PostReferenceVO toPostReference(PostReferencePO po);

	/**
	 * 岗位关联 PO 列表 → 岗位回显列表
	 * @param poList 持久层投影列表
	 * @return 岗位回显列表
	 */
	List<PostReferenceVO> toPostReferenceList(List<PostReferencePO> poList);

	/**
	 * 权限关联 PO → 权限回显
	 * @param po 持久层投影
	 * @return 权限回显
	 */
	PermissionReferenceVO toPermissionReference(PermissionReferencePO po);

	/**
	 * 权限关联 PO 列表 → 权限回显列表
	 * @param poList 持久层投影列表
	 * @return 权限回显列表
	 */
	List<PermissionReferenceVO> toPermissionReferenceList(List<PermissionReferencePO> poList);

	/**
	 * 菜单已分配角色 PO → 菜单角色分配回显
	 * @param po 持久层投影
	 * @return 菜单角色分配回显
	 */
	@Mapping(target = "assigned", constant = "true")
	MenuAssignedRoleReferenceVO toMenuAssignedRole(RoleReferencePO po);

	/**
	 * 菜单已分配角色 PO 列表 → 菜单角色分配回显列表
	 * @param poList 持久层投影列表
	 * @return 菜单角色分配回显列表
	 */
	List<MenuAssignedRoleReferenceVO> toMenuAssignedRoleList(List<RoleReferencePO> poList);

	/**
	 * 用户档案部门 PO → 用户部门关联回显
	 * @param po 持久层投影
	 * @return 用户部门关联回显
	 */
	UserDeptReferenceVO toUserDeptReference(UserDeptProfilePO po);

	/**
	 * 用户档案部门 PO 列表 → 用户部门关联回显列表
	 * @param poList 持久层投影列表
	 * @return 用户部门关联回显列表
	 */
	List<UserDeptReferenceVO> toUserDeptReferenceList(List<UserDeptProfilePO> poList);

	/**
	 * 用户档案岗位 PO → 用户岗位关联回显
	 * @param po 持久层投影
	 * @return 用户岗位关联回显
	 */
	UserPostReferenceVO toUserPostReference(UserPostProfilePO po);

	/**
	 * 用户档案岗位 PO 列表 → 用户岗位关联回显列表
	 * @param poList 持久层投影列表
	 * @return 用户岗位关联回显列表
	 */
	List<UserPostReferenceVO> toUserPostReferenceList(List<UserPostProfilePO> poList);

}
