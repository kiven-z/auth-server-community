package com.auth.service.auth.config;

import com.auth.module.security.contract.api.authorization.AuthorizationChangeKind;
import com.auth.module.security.contract.dto.invalidation.*;
import com.auth.service.auth.mapper.AuthorizationImpactMapper;
import com.auth.service.auth.support.invalidation.impact.AuthorizationImpactQuerySupport;
import com.auth.service.auth.support.invalidation.impact.ImpactResolver;
import com.auth.service.auth.support.invalidation.impact.PortBackedImpactResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Set;

/**
 * 影响面解析器装配：按 {@link AuthorizationChangeKind} 注册 {@link ImpactResolver} Bean。
 *
 * @author Bunny
 */
@Configuration
public class InvalidationImpactResolverConfiguration {

	/**
	 * 按角色码反查受影响用户 ID。
	 * @param impactQuery 授权影响面查询
	 * @return 角色码影响面解析器
	 */
	@Bean
	ImpactResolver<RoleInvalidatePayload> roleImpactResolver(AuthorizationImpactQuerySupport impactQuery) {
		return new PortBackedImpactResolver<>(AuthorizationChangeKind.ROLE, RoleInvalidatePayload.class,
				payload -> impactQuery.findUserIdsByRoleCodes(payload.roleCodes()));
	}

	/**
	 * 按权限码反查受影响用户 ID（P4→P1 桥接）。
	 * @param impactQuery 授权影响面查询
	 * @return 权限码影响面解析器
	 */
	@Bean
	ImpactResolver<PermissionInvalidatePayload> permissionImpactResolver(AuthorizationImpactQuerySupport impactQuery) {
		return new PortBackedImpactResolver<>(AuthorizationChangeKind.PERMISSION, PermissionInvalidatePayload.class,
				payload -> impactQuery.findUserIdsByPermissionCodes(payload.permissionCodes()));
	}

	/**
	 * 按 grant_table 授权主体反查受影响用户 ID（P2）。
	 * @param impactQuery 授权影响面查询
	 * @return 授权主体影响面解析器
	 */
	@Bean
	ImpactResolver<GrantInvalidatePayload> grantSubjectImpactResolver(AuthorizationImpactQuerySupport impactQuery) {
		return new PortBackedImpactResolver<>(AuthorizationChangeKind.GRANT, GrantInvalidatePayload.class,
				payload -> impactQuery.findUserIdsByGrantSubjects(payload.subjects()));
	}

	/**
	 * 按部门 ID 反查成员用户 ID（P3，含子部门）。
	 * @param impactQuery 授权影响面查询
	 * @return 部门成员影响面解析器
	 */
	@Bean
	ImpactResolver<UserDeptInvalidatePayload> userDeptImpactResolver(AuthorizationImpactQuerySupport impactQuery,
			AuthorizationImpactMapper authorizationImpactMapper) {
		return new PortBackedImpactResolver<>(AuthorizationChangeKind.USER_DEPT, UserDeptInvalidatePayload.class,
				payload -> impactQuery.findUserIdsByLongKeys(payload.deptIds(),
						authorizationImpactMapper::selectUserIdsByDeptIds));
	}

	/**
	 * 按岗位 ID 反查成员用户 ID（P3）。
	 * @param impactQuery 授权影响面查询
	 * @return 岗位成员影响面解析器
	 */
	@Bean
	ImpactResolver<UserPostInvalidatePayload> userPostImpactResolver(AuthorizationImpactQuerySupport impactQuery,
			AuthorizationImpactMapper authorizationImpactMapper) {
		return new PortBackedImpactResolver<>(AuthorizationChangeKind.USER_POST, UserPostInvalidatePayload.class,
				payload -> impactQuery.findUserIdsByLongKeys(payload.postIds(),
						authorizationImpactMapper::selectUserIdsByPostIds));
	}

	/**
	 * 按用户 ID 直连失效（用户主档变更，无需 SQL 反查）。
	 * @return 用户直连影响面解析器
	 */
	@Bean
	ImpactResolver<UserInvalidatePayload> userImpactResolver() {
		return new PortBackedImpactResolver<>(AuthorizationChangeKind.USER, UserInvalidatePayload.class,
				payload -> Set.copyOf(payload.userIds()));
	}

}
