package com.auth.module.security.autoconfigure.pipeline.authenticate;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.auth.module.security.contract.api.authorization.AuthProfile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 将 AuthProfile 写入 SecurityContextHolder
 *
 * @author Bunny
 */
public final class AuthProfileSecurityContextPopulator {

	/**
	 * 构建授权权限
	 * @param profile 授权画像
	 * @return 授权权限
	 */
	private static List<GrantedAuthority> buildAuthorities(AuthProfile profile) {
		return Stream.concat(
				// 构建权限
				CollUtil.emptyIfNull(profile.getPermissions())
					.stream()
					.filter(StrUtil::isNotBlank)
					.map(StrUtil::trim)
					.map(SimpleGrantedAuthority::new),

				// 构建角色
				CollUtil.emptyIfNull(profile.getRoles())
					.stream()
					.filter(StrUtil::isNotBlank)
					.map(StrUtil::trim)
					.map(role -> role.startsWith("ROLE_") ? role : "ROLE_" + role)
					.map(SimpleGrantedAuthority::new))
			.collect(Collectors.toList());
	}

	/**
	 * 填充安全上下文
	 * @param profile 授权画像
	 */
	public void populate(AuthProfile profile) {
		List<GrantedAuthority> authorities = buildAuthorities(profile);
		UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(profile, null,
				authorities);
		SecurityContextHolder.getContext().setAuthentication(authentication);
	}

	/**
	 * 在指定画像上下文中执行（结束后恢复原 SecurityContext）
	 * @param profile 授权画像
	 * @param action 业务动作
	 * @param <T> 返回类型
	 * @return 业务结果
	 */
	public <T> T runWithProfile(AuthProfile profile, ScopedAction<T> action) {
		SecurityContext originalContext = SecurityContextHolder.getContext();
		SecurityContext scopedContext = SecurityContextHolder.createEmptyContext();
		SecurityContextHolder.setContext(scopedContext);
		try {
			populate(profile);
			return action.execute();
		}
		finally {
			SecurityContextHolder.setContext(originalContext);
		}
	}

	/**
	 * 带 AuthProfile 作用域的动作
	 *
	 * @param <T> 返回类型
	 */
	@FunctionalInterface
	public interface ScopedAction<T> {

		/**
		 * 执行动作
		 * @return 结果
		 */
		T execute();

	}

}
