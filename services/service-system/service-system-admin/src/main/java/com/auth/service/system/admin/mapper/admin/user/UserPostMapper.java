package com.auth.service.system.admin.mapper.admin.user;

import com.auth.service.system.admin.model.entity.UserPostEntity;
import com.auth.service.system.admin.model.po.user.UserPostPageRowPO;
import com.auth.service.system.admin.model.query.user.UserPostPageQuery;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.*;

/**
 * 用户岗位关联 Mapper
 *
 * @author Bunny
 */
@Mapper
public interface UserPostMapper extends BaseMapper<UserPostEntity> {

	/**
	 * 分页查询用户岗位关联（基表全量，含失效任职）
	 * @param page 分页参数
	 * @param userId 用户 ID
	 * @param query 查询条件
	 * @return 分页结果
	 */
	IPage<UserPostPageRowPO> selectListByPage(@Param("page") Page<UserPostEntity> page, @Param("userId") Long userId,
			@Param("query") UserPostPageQuery query);

	/**
	 * 按用户与关联主键加载
	 * @param id 关联主键
	 * @param userId 用户 ID
	 * @return 实体，不存在时为 null
	 */
	UserPostEntity selectByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

	/**
	 * 统计用户与岗位是否已有关联
	 * @param userId 用户 ID
	 * @param postId 岗位 ID
	 * @return 行数
	 */
	@Select("SELECT COUNT(1) FROM user_post WHERE user_id = #{userId} AND post_id = #{postId}")
	int countByUserIdAndPostId(@Param("userId") Long userId, @Param("postId") Long postId);

	/**
	 * 将用户全部主岗位的 is_primary 置为 false
	 * @param userId 用户 ID
	 */
	@Update("UPDATE user_post SET is_primary = 0 WHERE user_id = #{userId} AND is_primary = 1")
	void demotePrimaryByUserId(@Param("userId") Long userId);

	/**
	 * 删除用户全部岗位关联
	 * @param userId 用户 ID
	 * @return 影响行数
	 */
	@Delete("DELETE FROM user_post WHERE user_id = #{userId}")
	int deleteByUserId(@Param("userId") Long userId);

}
