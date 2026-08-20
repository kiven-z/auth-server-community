package com.auth.common.ip.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * IP工具配置属性 配置前缀使用 kebab-case 遵循项目约定 配置前缀：auth.common.ip-tool
 *
 * @author Bunny
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "auth.common.ip-tool")
public class IpToolProperties {

	/**
	 * 是否解析外部/公共IP地址的区域 内部/私有IP地址永远不会从数据库中解析
	 */
	private boolean resolveExternalEnabled = true;

	/**
	 * 是否启用IPv6区域解析 当禁用时，IPv6数据库不会被加载，所有IPv6区域查询返回空
	 */
	private boolean resolveIpv6Enabled = false;

	/**
	 * IPv4 数据库文件路径（本地文件系统）
	 */
	private String ipv4DbPath;

	/**
	 * IPv6 数据库文件路径（本地文件系统）
	 */
	private String ipv6DbPath;

}
