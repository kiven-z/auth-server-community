package com.auth.service.system.admin.service.authorization.impact;

import java.util.List;

import com.auth.common.data.model.PageResponse;
import com.auth.service.system.admin.model.query.authorization.RoleImpactUserPageQuery;
import com.auth.service.system.admin.model.vo.authorization.RoleImpactRefreshResultVO;
import com.auth.service.system.admin.model.vo.reference.UserReferenceVO;

/**
 * 角色影响面
 *
 * @author Bunny
 */
public interface RoleImpactService {

	/**
	 * 分页查询角色影响用户
	 * @param roleId 角色 ID
	 * @param query 查询条件
	 * @return 分页数据
	 */
	PageResponse<UserReferenceVO> pageImpactUsers(Long roleId, RoleImpactUserPageQuery query);

	/**
	 * 刷新角色受影响用户授权画像
	 * @param roleId 角色 ID
	 * @param userIds 指定用户 ID（空则刷新全部影响用户）
	 * @return 刷新结果
	 */
	RoleImpactRefreshResultVO refreshImpactedProfiles(Long roleId, List<Long> userIds);

}
