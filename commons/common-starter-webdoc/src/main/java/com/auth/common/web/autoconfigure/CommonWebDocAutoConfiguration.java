package com.auth.common.web.autoconfigure;

import com.auth.common.web.config.OpenApiDocConfig;
import com.auth.common.web.config.properties.WebDocProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;

/**
 * SpringDoc OpenAPI auto configuration.
 *
 * @author Bunny
 */
@Slf4j
@AutoConfiguration
@EnableConfigurationProperties(WebDocProperties.class)
@Import({ OpenApiDocConfig.class })
public class CommonWebDocAutoConfiguration {

}
