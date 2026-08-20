package com.auth.gateway.filter;

import cn.hutool.core.text.CharSequenceUtil;
import com.auth.gateway.config.GatewaySecurityProperties;
import com.auth.gateway.exception.GatewayBusinessException;
import com.auth.gateway.exception.GatewayResultCodeEnum;
import com.auth.module.security.contract.exception.SecurityResultCodeEnum;
import com.auth.module.security.contract.exception.SecurityTokenException;
import com.auth.module.security.core.token.provider.AccessTokenProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 网关 JWT 轻量校验：仅在携带 Authorization 时校验“可解析”
 *
 * <p>
 * 过期语义由下游服务处理，网关不做会话/权限业务判断
 * </p>
 *
 * @author Bunny
 */
@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
@Component
public class JwtValidateGlobalFilter implements GlobalFilter {

	private final AccessTokenProvider accessTokenProvider;

	private final GatewaySecurityProperties gatewaySecurityProperties;

	public JwtValidateGlobalFilter(AccessTokenProvider accessTokenProvider,
			GatewaySecurityProperties gatewaySecurityProperties) {
		this.accessTokenProvider = accessTokenProvider;
		this.gatewaySecurityProperties = gatewaySecurityProperties;
	}

	/**
	 * 网关 JWT 校验
	 * @param exchange the current server exchange
	 * @param chain provides a way to delegate to the next filter
	 * @return Mono
	 */
	@Override
	public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
		ServerHttpRequest request = exchange.getRequest();
		String path = request.getURI().getPath();
		boolean strictPath = gatewaySecurityProperties.isStrictPath(path);

		String authorization = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
		if (CharSequenceUtil.isBlank(authorization)) {
			if (strictPath) {
				return Mono.error(new GatewayBusinessException(GatewayResultCodeEnum.UNAUTHORIZED));
			}
			return chain.filter(exchange);
		}

		try {
			accessTokenProvider.parseToken(authorization);
		}
		catch (SecurityTokenException ex) {
			if (!strictPath && ex.getResultCode() == SecurityResultCodeEnum.TOKEN_EXPIRED) {
				log.debug("Gateway token is expired but allowed in light mode, path={}", path);
				return chain.filter(exchange);
			}
			log.debug("Gateway token parse failed: code={}, path={}", ex.getCode(),
					exchange.getRequest().getURI().getPath());
			return Mono.error(new GatewayBusinessException(GatewayResultCodeEnum.UNAUTHORIZED));
		}
		return chain.filter(exchange);
	}

}
