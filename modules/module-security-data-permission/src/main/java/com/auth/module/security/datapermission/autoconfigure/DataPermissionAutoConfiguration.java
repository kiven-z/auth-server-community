package com.auth.module.security.datapermission.autoconfigure;

import com.auth.module.security.datapermission.context.LoginUserProvider;
import com.auth.module.security.datapermission.context.SecurityContextLoginUserProvider;
import com.auth.module.security.datapermission.handler.DataScopePermissionHandler;
import com.auth.module.security.datapermission.support.DataScopeAnnotationResolver;
import com.baomidou.mybatisplus.extension.plugins.inner.DataPermissionInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.InnerInterceptor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;

/**
 * 数据权限模块自动配置
 *
 * <p>
 * 只注册 {@link InnerInterceptor}，由 {@code BaseMybatisPlusConfig} 收敛进唯一的
 * {@code MybatisPlusInterceptor}（须排在分页插件之前）。
 * </p>
 *
 * @author Bunny
 */
@AutoConfiguration
@ConditionalOnClass(DataPermissionInterceptor.class)
public class DataPermissionAutoConfiguration {

	/**
	 * 登录用户提供者（允许业务侧覆盖）
	 * @return 登录用户提供者
	 */
	@ConditionalOnMissingBean
	@Bean
	LoginUserProvider loginUserProvider() {
		return new SecurityContextLoginUserProvider();
	}

	/**
	 * 数据权限 Inner 插件
	 * @param loginUserProvider 登录用户提供者
	 * @return Inner 拦截器
	 */
	@Bean
	@Order(0)
	InnerInterceptor dataPermissionInnerInterceptor(LoginUserProvider loginUserProvider) {
		DataScopeAnnotationResolver annotationResolver = new DataScopeAnnotationResolver();
		DataScopePermissionHandler dataPermissionHandler = new DataScopePermissionHandler(loginUserProvider,
				annotationResolver);
		return new DataPermissionInterceptor(dataPermissionHandler);
	}

}
