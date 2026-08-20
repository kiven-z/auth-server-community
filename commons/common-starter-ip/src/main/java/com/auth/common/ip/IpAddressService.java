package com.auth.common.ip;

import jakarta.servlet.http.HttpServletRequest;

/**
 * IP地址服务，用于解析客户端IP和区域信息
 * <p>
 * 这个服务支持从当前请求、提供的请求或原始IP字符串解析
 * </p>
 *
 * @author Bunny
 */
public interface IpAddressService {

	/**
	 * 解析当前HTTP请求的IP信息
	 * @return IP信息，永远不会为空
	 */
	IpInfo resolveCurrentRequestIpInfo();

	/**
	 * 解析给定HTTP请求的IP信息
	 * @param request HTTP servlet请求
	 * @return IP信息，永远不会为空
	 */
	IpInfo resolveIpInfo(HttpServletRequest request);

	/**
	 * 解析给定原始IP字符串的IP信息
	 * @param ip 原始IP字符串 (IPv4/IPv6)
	 * @return IP信息，永远不会为空
	 */
	IpInfo resolveIpInfo(String ip);

}
