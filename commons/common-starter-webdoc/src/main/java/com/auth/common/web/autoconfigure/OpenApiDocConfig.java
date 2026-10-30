package com.auth.common.web.autoconfigure;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

/**
 * SpringDoc OpenAPI 自动配置
 *
 * @author Bunny
 */
@AutoConfiguration
public class OpenApiDocConfig {

	private static final String TITLE = "Auth";

	private static final String SUMMARY = "Auth 管理后台";

	private static final String DESCRIPTION = "身份认证、访问控制与系统管理 API";

	private static final String CONTACT_NAME = "Auth Admin";

	private static final String LICENSE_NAME = "Apache License 2.0";

	private static final String LICENSE_URL = "https://www.apache.org/licenses/LICENSE-2.0";

	/**
	 * OpenAPI 安全方案名称，对应 HTTP Bearer JWT
	 */
	private static final String BEARER_AUTH_SCHEME = "bearerAuth";

	/**
	 * 文档元信息：产品名拼应用名
	 * @param environment 运行环境
	 * @return OpenAPI
	 */
	@ConditionalOnMissingBean(OpenAPI.class)
	@Bean
	public OpenAPI defaultOpenApi(Environment environment) {
		String serviceName = environment.getRequiredProperty("spring.application.name");
		Info info = new Info().title(TITLE + "-" + serviceName)
			.summary(SUMMARY)
			.description(DESCRIPTION)
			.contact(new Contact().name(CONTACT_NAME))
			.license(new License().name(LICENSE_NAME).url(LICENSE_URL));
		SecurityScheme bearerScheme = new SecurityScheme().type(SecurityScheme.Type.HTTP)
			.scheme("bearer")
			.bearerFormat("JWT");
		return new OpenAPI().info(info)
			.components(new Components().addSecuritySchemes(BEARER_AUTH_SCHEME, bearerScheme))
			.addSecurityItem(new SecurityRequirement().addList(BEARER_AUTH_SCHEME));
	}

	/**
	 * 对外接口分组
	 * @return GroupedOpenApi
	 */
	@Bean
	public GroupedOpenApi webApi() {
		return GroupedOpenApi.builder()
			.group("web")
			.displayName("默认分组")
			.pathsToMatch("/api/**")
			.pathsToExclude("/api/**/inner/**")
			.build();
	}

	/**
	 * 内部接口分组
	 * @return GroupedOpenApi
	 */
	@Bean
	public GroupedOpenApi innerApi() {
		return GroupedOpenApi.builder().group("inner").displayName("内部分组").pathsToMatch("/api/**/inner/**").build();
	}

}
