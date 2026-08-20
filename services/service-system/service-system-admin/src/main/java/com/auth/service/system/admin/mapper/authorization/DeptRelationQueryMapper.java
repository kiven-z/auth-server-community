package com.auth.service.system.admin.mapper.authorization;

import com.auth.service.system.admin.model.po.dept.SysDeptBoundUserPO;
import com.auth.service.system.admin.model.po.reference.PostReferencePO;
import com.auth.service.system.admin.model.query.authorization.DeptPostPageQuery;
import com.auth.service.system.admin.model.query.authorization.DeptUserPageQuery;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 部门关联只读查询
 *
 * @author Bunny
 */
@Mapper
public interface DeptRelationQueryMapper {

	/**
	 * 统计部门绑定用户数（基表，含已停用）
	 * @param deptId 部门 ID
	 * @param query 过滤条件
	 * @return 用户数
	 */
	long countUsersByDeptId(@Param("deptId") Long deptId, @Param("query") DeptUserPageQuery query);

	/**
	 * 分页查询部门关联用户
	 * @param page 分页参数
	 * @param deptId 部门 ID
	 * @param query 过滤条件
	 * @return 分页结果
	 */
	IPage<SysDeptBoundUserPO> selectUsersByDeptIdPage(@Param("page") Page<?> page, @Param("deptId") Long deptId,
			@Param("query") DeptUserPageQuery query);

	/**
	 * 统计部门下属岗位数
	 * @param deptId 部门 ID
	 * @param query 过滤条件
	 * @return 岗位数
	 */
	long countPostsByDeptId(@Param("deptId") Long deptId, @Param("query") DeptPostPageQuery query);

	/**
	 * 分页查询部门下属岗位
	 * @param page 分页参数
	 * @param deptId 部门 ID
	 * @param query 过滤条件
	 * @return 分页结果
	 */
	IPage<PostReferencePO> selectPostsByDeptIdPage(@Param("page") Page<?> page, @Param("deptId") Long deptId,
			@Param("query") DeptPostPageQuery query);

}
