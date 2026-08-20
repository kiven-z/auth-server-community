package com.auth.common.ip;

import cn.hutool.core.text.CharSequenceUtil;
import com.auth.common.ip.format.RegionFormatter;
import com.auth.common.ip.inner.InnerIpChecker;
import com.auth.common.ip.normalize.IpNormalizer;
import com.auth.common.ip.region.RegionResolver;
import com.auth.common.ip.resolver.ClientIpResolver;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Optional;

/**
 * IpAddressService 实现
 *
 * @author Bunny
 */
@Slf4j
public class DefaultIpAddressServiceImpl implements IpAddressService {

	private static final String INNER_IP_TEXT = "内网IP";

	private final ClientIpResolver clientIpResolver;

	private final IpNormalizer ipNormalizer;

	private final InnerIpChecker innerIpChecker;

	private final RegionResolver regionResolver;

	private final RegionFormatter regionFormatter;

	public DefaultIpAddressServiceImpl(ClientIpResolver clientIpResolver, IpNormalizer ipNormalizer,
			InnerIpChecker innerIpChecker, RegionResolver regionResolver, RegionFormatter regionFormatter) {
		this.clientIpResolver = clientIpResolver;
		this.ipNormalizer = ipNormalizer;
		this.innerIpChecker = innerIpChecker;
		this.regionResolver = regionResolver;
		this.regionFormatter = regionFormatter;
	}

	/**
	 * 获取当前请求
	 * @return 请求
	 */
	private static Optional<HttpServletRequest> getCurrentRequest() {
		try {
			RequestAttributes attributes = RequestContextHolder.currentRequestAttributes();
			if (attributes instanceof ServletRequestAttributes servletRequestAttributes) {
				return Optional.of(servletRequestAttributes.getRequest());
			}
			return Optional.empty();
		}
		catch (IllegalStateException e) {
			log.debug("No request bound to current thread.");
			return Optional.empty();
		}
	}

	/**
	 * 空IP信息
	 * @return IP信息
	 */
	private static IpInfo emptyIpInfo() {
		return IpInfo.builder().ipAddr(CharSequenceUtil.EMPTY).ipRegion(CharSequenceUtil.EMPTY).build();
	}

	/**
	 * 解析当前请求的IP信息
	 * @return IP信息
	 */
	@Override
	public IpInfo resolveCurrentRequestIpInfo() {
		// 获取当前请求
		HttpServletRequest request = getCurrentRequest().orElse(null);
		return resolveIpInfo(request);
	}

	/**
	 * 解析请求的IP信息
	 * @param request 请求
	 * @return IP信息
	 */
	@Override
	public IpInfo resolveIpInfo(@Nullable HttpServletRequest request) {
		if (request == null) {
			return emptyIpInfo();
		}

		// 解析原始客户端IP字符串
		Optional<String> rawIp = clientIpResolver.resolve(request);
		if (rawIp.isEmpty()) {
			return emptyIpInfo();
		}

		return resolveIpInfo(rawIp.get());
	}

	/**
	 * 解析IP信息
	 * @param ip IP
	 * @return IP信息
	 */
	@Override
	public IpInfo resolveIpInfo(@Nullable String ip) {
		Optional<String> normalized = ipNormalizer.normalize(ip);
		if (normalized.isEmpty()) {
			return emptyIpInfo();
		}

		String normalizedIp = normalized.get();
		if (innerIpChecker.isInner(normalizedIp)) {
			return IpInfo.builder().ipAddr(normalizedIp).ipRegion(INNER_IP_TEXT).build();
		}

		String region = regionResolver.resolveRegion(normalizedIp)
			.map(regionFormatter::format)
			.orElse(CharSequenceUtil.EMPTY);

		return IpInfo.builder().ipAddr(normalizedIp).ipRegion(region).build();
	}

}
