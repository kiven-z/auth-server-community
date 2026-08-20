package com.auth.module.security.starter.annotation;

import com.auth.module.security.autoconfigure.annotation.AuthenticatedApi;
import com.auth.module.security.autoconfigure.annotation.InternalApi;
import com.auth.module.security.autoconfigure.annotation.PublicApi;
import com.auth.module.security.autoconfigure.annotation.processor.MutuallyExclusiveAnnotationsRule;
import com.auth.module.security.autoconfigure.annotation.processor.SecurityAnnotationConflictBeanPostProcessor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class SecurityAnnotationConflictBeanPostProcessorTest {

	private final MutuallyExclusiveAnnotationsRule rule = new MutuallyExclusiveAnnotationsRule("testGroup",
			Set.of(PublicApi.class, AuthenticatedApi.class, InternalApi.class, PreAuthorize.class));

	private final SecurityAnnotationConflictBeanPostProcessor processor = new SecurityAnnotationConflictBeanPostProcessor(
			List.of(rule));

	@Test
	@DisplayName("测试清洁控制器通过")
	void cleanController_passes() {
		assertDoesNotThrow(() -> processor.postProcessAfterInitialization(new GoodController(), "good"));
	}

	@Test
	@DisplayName("测试冲突控制器失败")
	void conflictingController_fails() {
		BadController bean = new BadController();
		IllegalStateException ex = assertThrows(IllegalStateException.class,
				() -> processor.postProcessAfterInitialization(bean, "bad"));
		assertTrue(ex.getMessage().contains("testGroup"));
	}

	@Test
	@DisplayName("测试非控制器跳过")
	void nonController_skipped() {
		assertDoesNotThrow(() -> processor.postProcessAfterInitialization(new Object(), "notController"));
	}

	@RestController
	static class GoodController {

		@GetMapping("/x")
		public String ok() {
			return "ok";
		}

	}

	@RestController
	static class BadController {

		@PublicApi
		@InternalApi
		@PreAuthorize("true")
		@GetMapping("/y")
		public String conflict() {
			return "no";
		}

	}

}
