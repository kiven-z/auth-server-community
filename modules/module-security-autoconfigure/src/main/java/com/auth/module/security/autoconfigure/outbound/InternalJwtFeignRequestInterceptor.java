package com.auth.module.security.autoconfigure.outbound;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.http.HttpHeaders;

import static com.auth.module.security.contract.constants.SecurityInternalTokenConstants.INTERNAL_HEADER;

/**
 * Feign 拦截器：出站为服务间调用签发短生命周期内部 JWT，并清除外部 Authorization，避免误传播
 *
 * <p>
 * 智能签发策略由 {@link OutboundInternalJwtIssuer} 统一处理：
 * <ul>
 * <li>有用户上下文：签发用户身份令牌（principal_type=USER），将用户信息透传给下游服务</li>
 * <li>无用户上下文：签发服务身份令牌（principal_type=SERVICE），用于定时任务、MQ消费者等场景</li>
 * </ul>
 * 不要将内部令牌传播回客户端
 *
 * @author Bunny
 */
public final class InternalJwtFeignRequestInterceptor implements RequestInterceptor {

	private final OutboundInternalJwtIssuer jwtIssuer;

	/**
	 * @param jwtIssuer 出站内部 JWT 签发器
	 */
	public InternalJwtFeignRequestInterceptor(OutboundInternalJwtIssuer jwtIssuer) {
		this.jwtIssuer = jwtIssuer;
	}

	/**
	 * 应用拦截器：剥离外部 Authorization，并根据当前上下文智能签发内部令牌
	 * @param template 请求模板
	 */
	@Override
	public void apply(RequestTemplate template) {
		template.removeHeader(HttpHeaders.AUTHORIZATION);
		template.removeHeader(INTERNAL_HEADER);

		String internalToken = jwtIssuer.issueInternalToken();
		template.header(INTERNAL_HEADER, internalToken);
	}

}
