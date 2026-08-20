package com.auth.module.security.autoconfigure.outbound;

import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;

import static com.auth.module.security.contract.constants.SecurityInternalTokenConstants.INTERNAL_HEADER;

/**
 * 负载均衡 {@link org.springframework.web.client.RestTemplate} 出站拦截器：自动附加 X-Internal-JWT
 *
 * @author Bunny
 */
public final class InternalJwtClientHttpRequestInterceptor implements ClientHttpRequestInterceptor {

	private final OutboundInternalJwtIssuer jwtIssuer;

	/**
	 * @param jwtIssuer 出站内部 JWT 签发器
	 */
	public InternalJwtClientHttpRequestInterceptor(OutboundInternalJwtIssuer jwtIssuer) {
		this.jwtIssuer = jwtIssuer;
	}

	@Override
	@NotNull
	public ClientHttpResponse intercept(HttpRequest request, @NotNull byte[] body, ClientHttpRequestExecution execution)
			throws IOException {
		HttpHeaders headers = request.getHeaders();
		headers.remove(HttpHeaders.AUTHORIZATION);
		headers.remove(INTERNAL_HEADER);

		String internalToken = jwtIssuer.issueInternalToken();
		headers.set(INTERNAL_HEADER, internalToken);

		return execution.execute(request, body);
	}

}
