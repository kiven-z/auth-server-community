package com.auth.service.system.admin.support.user;

import cn.hutool.core.collection.CollUtil;
import com.auth.service.system.authorization.dispatch.trigger.UserAuthorizationInvalidationTrigger;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Consumer;

/**
 * 用户组织关联（部门/岗位）批量删除编排
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Component
public class UserOrgRelationBatchRemoveSupport {

	private final UserReferenceChecker userReferenceChecker;

	private final UserAuthorizationInvalidationTrigger userAuthorizationInvalidationTrigger;

	/**
	 * 校验用户可写后批量删除关联，并触发权限失效。
	 * @param userId 用户主键
	 * @param ids 关联主键列表
	 * @param removeByIds 实际删除动作
	 * @param invalidationReason 授权失效原因标识
	 */
	public void removeBatch(Long userId, List<Long> ids, Consumer<List<Long>> removeByIds, String invalidationReason) {
		if (CollUtil.isEmpty(ids)) {
			return;
		}

		userReferenceChecker.getExistingActive(userId);
		userReferenceChecker.requireOperable(List.of(userId));

		List<Long> distinctIds = ids.stream().distinct().toList();
		removeByIds.accept(distinctIds);
		userAuthorizationInvalidationTrigger.submitByUserIds(List.of(userId), invalidationReason);
	}

}
