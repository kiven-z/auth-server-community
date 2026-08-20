package com.auth.common.web.model.entity;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.io.Serial;
import java.io.Serializable;

/**
 * 用户代理过滤器信息
 *
 * @author Bunny
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserAgent implements Serializable {

	/**
	 * 设备类型 用户代理解析成功后，将设备类型传递给下游服务
	 */
	public static final String DEVICE_TYPE = "X-Device-Class";

	/**
	 * 设备名称 用户代理解析成功后，将设备名称传递给下游服务
	 */
	public static final String DEVICE_NAME = "X-Device-Name";

	/**
	 * 浏览器名称和版本 用户代理解析成功后，将浏览器名称和版本传递给下游服务
	 */
	public static final String AGENT_NAME_VERSION = "X-Browser-Name";

	/**
	 * 操作系统 用户代理解析成功后，将操作系统名称和版本传递给下游服务
	 */
	public static final String OPERATING_SYSTEM_NAME = "X-Operating-System";

	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * 设备类型
	 */
	private String deviceType;

	/**
	 * 设备名称
	 */
	private String deviceName;

	/**
	 * 浏览器名称
	 */
	private String browser;

	/**
	 * 操作系统名称
	 */
	private String os;

	/**
	 * 获取用户代理信息
	 * @param request HTTP请求
	 * @return 用户代理信息
	 */
	public static UserAgent getUserAgent(@NotNull HttpServletRequest request) {
		String deviceType = request.getHeader(DEVICE_TYPE);
		String deviceName = request.getHeader(DEVICE_NAME);
		String agentName = request.getHeader(AGENT_NAME_VERSION);
		String operatingName = request.getHeader(OPERATING_SYSTEM_NAME);
		return new UserAgent(deviceType, deviceName, agentName, operatingName);
	}

}
