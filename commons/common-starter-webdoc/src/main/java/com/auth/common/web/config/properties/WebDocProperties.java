package com.auth.common.web.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * WebDoc / SpringDoc 配置；品牌与版本由 Nacos {@code AUTH_COMMON/common-config.yaml} 注入
 *
 * @author Bunny
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "auth.common.webdoc")
public class WebDocProperties {

	/**
	 * Toggle for webdoc auto configuration.
	 */
	private Boolean enabled = Boolean.TRUE;

	/**
	 * OpenAPI title prefix；拼成 {@code title-serviceName}
	 */
	private String title;

	/**
	 * OpenAPI description.
	 */
	private String description;

	/**
	 * OpenAPI summary.
	 */
	private String summary;

	/**
	 * OpenAPI version（与发版 tag 对齐，在 Nacos 维护）.
	 */
	private String version;

	/**
	 * Contact name.
	 */
	private String contactName;

	/**
	 * Contact email.
	 */
	private String contactEmail;

	/**
	 * License name.
	 */
	private String licenseName;

	/**
	 * License URL.
	 */
	private String licenseUrl;

	/**
	 * OpenAPI groups.
	 */
	private List<ApiGroup> groups = defaultGroups();

	/**
	 * 默认 web / inner 分组
	 * @return 分组列表
	 */
	public static List<ApiGroup> defaultGroups() {
		ApiGroup web = new ApiGroup();
		web.setGroup("web");
		web.setDisplayName("默认分组");
		web.setPathsToMatch(List.of("/api/**"));

		ApiGroup inner = new ApiGroup();
		inner.setGroup("inner");
		inner.setDisplayName("内部分组");
		inner.setPathsToMatch(List.of("/api/**/inner/**"));

		return List.of(web, inner);
	}

	@Getter
	@Setter
	public static class ApiGroup {

		private String group;

		private String displayName;

		private List<String> pathsToMatch = new ArrayList<>();

	}

}
