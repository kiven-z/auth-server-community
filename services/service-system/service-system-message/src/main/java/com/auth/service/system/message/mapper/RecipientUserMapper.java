package com.auth.service.system.message.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;

/**
 * 站内信接收人展开查询（只读，跨组织表；统一游标分页）
 *
 * @author Bunny
 */
@Mapper
public interface RecipientUserMapper {

	/**
	 * 过滤启用用户
	 * @param userIds 候选用户 ID
	 * @param lastUserId 上一批最大 userId，首批传 0
	 * @param limit 本批上限
	 * @return 本批 userId
	 */
	List<Long> selectEnabledUserIdsAfter(@Param("userIds") Collection<Long> userIds,
			@Param("lastUserId") long lastUserId, @Param("limit") int limit);

	/**
	 * 按岗位展开启用用户
	 * @param postIds 岗位 ID
	 * @param lastUserId 上一批最大 userId，首批传 0
	 * @param limit 本批上限
	 * @return 本批 userId
	 */
	List<Long> selectUserIdsByPostIdsAfter(@Param("postIds") Collection<Long> postIds,
			@Param("lastUserId") long lastUserId, @Param("limit") int limit);

	/**
	 * 按部门展开启用用户
	 * @param deptIds 部门 ID
	 * @param lastUserId 上一批最大 userId，首批传 0
	 * @param limit 本批上限
	 * @return 本批 userId
	 */
	List<Long> selectUserIdsByDeptIdsAfter(@Param("deptIds") Collection<Long> deptIds,
			@Param("lastUserId") long lastUserId, @Param("limit") int limit);

	/**
	 * 按部门展开启用用户
	 * @param deptIds 部门 ID
	 * @param lastUserId 上一批最大 userId，首批传 0
	 * @param limit 本批上限
	 * @return 本批 userId
	 */
	List<Long> selectUserIdsByDeptIdsWithChildrenAfter(@Param("deptIds") Collection<Long> deptIds,
			@Param("lastUserId") long lastUserId, @Param("limit") int limit);

}
