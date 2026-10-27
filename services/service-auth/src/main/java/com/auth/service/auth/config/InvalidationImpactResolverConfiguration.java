package com.auth.service.auth.config;

import com.auth.module.security.contract.invalidation.AuthorizationChangeKind;
import com.auth.module.security.contract.invalidation.AuthorizationInvalidatePayload;
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
	ImpactResolver<AuthorizationInvalidatePayload.Role> roleImpactResolver(
			AuthorizationImpactQuerySupport impactQuery) {
		return new PortBackedImpactResolver<>(AuthorizationChangeKind.ROLE, AuthorizationInvalidatePayload.Role.class,
				payload -> impactQuery.findUserIdsByRoleCodes(payload.roleCodes()));
	}

	/**
	 * 按权限码反查受影响用户 ID
	 * @param impactQuery 授权影响面查询
	 * @return 权限码影响面解析器
	 */
	@Bean
	ImpactResolver<AuthorizationInvalidatePayload.Permission> permissionImpactResolver(
			AuthorizationImpactQuerySupport impactQuery) {
		return new PortBackedImpactResolver<>(AuthorizationChangeKind.PERMISSION,
				AuthorizationInvalidatePayload.Permission.class,
				payload -> impactQuery.findUserIdsByPermissionCodes(payload.permissionCodes()));
	}

	/**
	 * 按 grant_table 授权主体反查受影响用户 ID
	 * @param impactQuery 授权影响面查询
	 * @return 授权主体影响面解析器
	 */
	@Bean
	ImpactResolver<AuthorizationInvalidatePayload.Grant> grantSubjectImpactResolver(
			AuthorizationImpactQuerySupport impactQuery) {
		return new PortBackedImpactResolver<>(AuthorizationChangeKind.GRANT, AuthorizationInvalidatePayload.Grant.class,
				payload -> impactQuery.findUserIdsByGrantSubjects(payload.subjects()));
	}

	/**
	 * 按用户 ID 直连失效
	 * <p>
	 * 用户主档变更，无需 SQL 反查
	 * </p>
	 * @return 用户直连影响面解析器
	 */
	@Bean
	ImpactResolver<AuthorizationInvalidatePayload.User> userImpactResolver() {
		return new PortBackedImpactResolver<>(AuthorizationChangeKind.USER, AuthorizationInvalidatePayload.User.class,
				payload -> Set.copyOf(payload.userIds()));
	}

}
