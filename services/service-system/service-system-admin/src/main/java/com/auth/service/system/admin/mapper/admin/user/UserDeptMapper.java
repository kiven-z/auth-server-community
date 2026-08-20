package com.auth.service.system.admin.mapper.admin.user;

import com.auth.service.system.admin.model.entity.UserDeptEntity;
import com.auth.service.system.admin.model.po.user.UserDeptPageRowPO;
import com.auth.service.system.admin.model.po.user.UserPrimaryDeptPO;
import com.auth.service.system.admin.model.query.user.UserDeptPageQuery;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.*;

/**
 * 用户部门关联 Mapper
 *
 * @author Bunny
 */
@Mapper
public interface UserDeptMapper extends BaseMapper<UserDeptEntity> {

	/**
	 * 分页查询用户部门关联
	 * @param page 分页参数
	 * @param userId 用户 ID
	 * @param query 查询条件
	 * @return 分页结果
	 */
	IPage<UserDeptPageRowPO> selectListByPage(@Param("page") Page<UserDeptEntity> page, @Param("userId") Long userId,
			@Param("query") UserDeptPageQuery query);

	/**
	 * 按用户与关联主键加载
	 * @param id 关联主键
	 * @param userId 用户 ID
	 * @return 实体，不存在时为 null
	 */
	UserDeptEntity selectByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

	/**
	 * 统计用户与部门是否已有关联
	 * @param userId 用户 ID
	 * @param deptId 部门 ID
	 * @return 行数
	 */
	@Select("SELECT COUNT(1) FROM user_dept WHERE user_id = #{userId} AND dept_id = #{deptId}")
	int countByUserIdAndDeptId(@Param("userId") Long userId, @Param("deptId") Long deptId);

	/**
	 * 按用户与部门加载关联
	 * @param userId 用户 ID
	 * @param deptId 部门 ID
	 * @return 实体，不存在时为 null
	 */
	UserDeptEntity selectByUserIdAndDeptId(@Param("userId") Long userId, @Param("deptId") Long deptId);

	/**
	 * 将用户全部主部门的 is_primary 置为 false
	 * @param userId 用户 ID
	 */
	@Update("UPDATE user_dept SET is_primary = 0 WHERE user_id = #{userId} AND is_primary = 1")
	void demotePrimaryByUserId(@Param("userId") Long userId);

	/**
	 * 删除用户全部部门关联
	 * @param userId 用户 ID
	 * @return 影响行数
	 */
	@Delete("DELETE FROM user_dept WHERE user_id = #{userId}")
	int deleteByUserId(@Param("userId") Long userId);

	/**
	 * 查询用户主部门（有效任职且 is_primary）
	 * @param userId 用户 ID
	 * @return 主部门投影，不存在时为 null
	 */
	UserPrimaryDeptPO selectPrimaryDeptByUserId(@Param("userId") Long userId);

}
