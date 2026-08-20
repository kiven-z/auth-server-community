package com.auth.gateway.config;

import com.auth.gateway.ua.YauaaUserAgentFields;
import nl.basjes.parse.useragent.UserAgentAnalyzer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 网关内共享的 Yauaa供规范化层与各过滤器复用
 *
 * @author Bunny
 */
@Configuration
public class UserAgentAnalyzerConfiguration {

	/**
	 * 网关内共享的 Yauaa 供规范化层与各过滤器复用
	 * @return 网关内共享的 Yauaa
	 */
	@Bean
	public UserAgentAnalyzer gatewayUserAgentAnalyzer() {
		return UserAgentAnalyzer.newBuilder()
			.withCache(10000)
			.withField(YauaaUserAgentFields.DEVICE_CLASS)
			.withField(YauaaUserAgentFields.DEVICE_NAME)
			.withField(YauaaUserAgentFields.AGENT_NAME_VERSION)
			.withField(YauaaUserAgentFields.OPERATING_SYSTEM_NAME_VERSION)
			.build();
	}

}
