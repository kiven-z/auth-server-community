package com.auth.gateway.filter;

import cn.hutool.core.date.LocalDateTimeUtil;
import com.auth.gateway.exception.GatewayBusinessException;
import com.auth.gateway.exception.GatewayResultCodeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.time.LocalDateTime;

/**
 * 记录请求操作的时间
 *
 * @author Bunny
 */
@Slf4j
@Component
public class RtfFilter implements GlobalFilter, Ordered {

	private static final String DATETIME_FORMATTER = "yyyy-MM-dd HH:mm:ss";

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
		ServerHttpRequest request = exchange.getRequest();

		// 请求地址
		URI uri = request.getURI();

		// 开始时间
		long start = System.currentTimeMillis();

		// 当前时间
		String pattern = LocalDateTimeUtil.format(LocalDateTime.now(), DATETIME_FORMATTER);
		log.debug("请求【{}】开始时间：{}", uri, pattern);

		// 处理逻辑
		return chain.filter(exchange).doOnError(e -> {
			long end = System.currentTimeMillis();
			log.error("请求失败【{}】结束，耗时：{}ms", uri, end - start, e);
		}).onErrorResume(e -> {
			if (e instanceof GatewayBusinessException gbe) {
				return Mono.error(gbe);
			}
			return Mono.error(new GatewayBusinessException(GatewayResultCodeEnum.INTERNAL_ERROR));
		}).doFinally(result -> {
			long end = System.currentTimeMillis();
			log.debug("请求成功【{}】结束，状态：{}，耗时：{}ms", uri, result, end - start);
		});
	}

	/**
	 * 执行顺序
	 * @return 顺序
	 */
	@Override
	public int getOrder() {
		return Ordered.HIGHEST_PRECEDENCE + 1;
	}

}