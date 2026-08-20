package com.auth.module.security.starter;

import com.auth.module.security.starter.fixture.ProbeMvcFixtureController;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 仅用于 module-security-starter 单元测试切片（如
 * {@link org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest}） 的占位
 * {@link SpringBootApplication}；不包含业务逻辑
 */
@SpringBootApplication(scanBasePackageClasses = ProbeMvcFixtureController.class)
public class ResolverTestSpringBootApp {

}
