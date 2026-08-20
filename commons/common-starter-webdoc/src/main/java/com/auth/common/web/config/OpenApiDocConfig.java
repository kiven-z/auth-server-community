package com.auth.common.web.config;

import com.auth.common.web.config.properties.WebDocProperties;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.util.ArrayList;
import java.util.List;

/**
 * SpringDoc OpenAPI 默认配置；如需自定义，可自行声明 {@link OpenApiDocConfig} Bean。
 *
 * @author Bunny
 */
@Slf4j
@ConditionalOnProperty(prefix = "auth.common.webdoc", name = "enabled", havingValue = "true", matchIfMissing = true)
@ConditionalOnMissingBean(OpenApiDocConfig.class)
@Configuration
public class OpenApiDocConfig {

	/**
	 * OpenAPI 安全方案名称，对应 HTTP Bearer JWT
	 */
	private static final String BEARER_AUTH_SCHEME = "bearerAuth";

	private final WebDocProperties webDocProperties;

	private final Environment environment;

	public OpenApiDocConfig(WebDocProperties webDocProperties, Environment environment) {
		this.webDocProperties = webDocProperties;
		this.environment = environment;
	}

	/**
	 * 默认 OpenAPI 文档元信息；如需覆盖，可自行声明 defaultOpenApi Bean。
	 * @return OpenAPI
	 */
	@Bean
	public OpenAPI defaultOpenApi() {
		String port = environment.getProperty("server.port", "8080");
		String serviceName = environment.getProperty("spring.application.name", "service");
		String url = "http://localhost:" + port;

		Contact contact = new Contact().name(webDocProperties.getContactName())
			.email(webDocProperties.getContactEmail())
			.url(url);
		License license = new License().name(webDocProperties.getLicenseName()).url(webDocProperties.getLicenseUrl());
		Info info = new Info().title(resolveDocTitle(serviceName))
			.contact(contact)
			.license(license)
			.description(webDocProperties.getDescription())
			.summary(webDocProperties.getSummary())
			.termsOfService(url)
			.version(webDocProperties.getVersion());

		SecurityScheme bearerScheme = new SecurityScheme().type(SecurityScheme.Type.HTTP)
			.scheme("bearer")
			.bearerFormat("JWT");
		Components components = new Components().addSecuritySchemes(BEARER_AUTH_SCHEME, bearerScheme);

		return new OpenAPI().info(info)
			.externalDocs(new ExternalDocumentation())
			.components(components)
			.addSecurityItem(new SecurityRequirement().addList(BEARER_AUTH_SCHEME));
	}

	/**
	 * 组装文档标题；未配置 title 时仅用服务名，避免出现 {@code null-} 前缀
	 * @param serviceName 应用名
	 * @return 文档标题
	 */
	private String resolveDocTitle(String serviceName) {
		String title = webDocProperties.getTitle();
		if (title == null || title.isBlank()) {
			return serviceName;
		}
		return title + "-" + serviceName;
	}

	/**
	 * OpenAPI 分组（默认 web/inner，可被配置覆盖）。
	 * @return GroupedOpenApi 列表
	 */
	@Bean
	public List<GroupedOpenApi> groupedOpenApis() {
		List<WebDocProperties.ApiGroup> configuredGroups = webDocProperties.getGroups();
		List<WebDocProperties.ApiGroup> groups = (configuredGroups == null || configuredGroups.isEmpty())
				? WebDocProperties.defaultGroups() : configuredGroups;

		List<GroupedOpenApi> apis = new ArrayList<>(groups.size());
		for (WebDocProperties.ApiGroup group : groups) {
			apis.add(GroupedOpenApi.builder()
				.displayName(group.getDisplayName())
				.group(group.getGroup())
				.pathsToMatch(group.getPathsToMatch().toArray(String[]::new))
				.build());
		}
		return apis;
	}

}
