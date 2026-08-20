package com.auth.module.security.starter.annotation;

import com.auth.module.security.autoconfigure.annotation.AuthenticatedApi;
import com.auth.module.security.autoconfigure.annotation.PublicApi;
import com.auth.module.security.autoconfigure.annotation.processor.MutuallyExclusiveAnnotationsRule;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.reflect.Method;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class MutuallyExclusiveAnnotationsRuleTest {

	private final MutuallyExclusiveAnnotationsRule rule = new MutuallyExclusiveAnnotationsRule("testGroup",
			Set.of(PublicApi.class, AuthenticatedApi.class, PreAuthorize.class));

	@Test
	@DisplayName("测试组包含预期类型")
	void group_containsExpectedTypes() {
		assertTrue(rule.getGroup().contains(PublicApi.class));
		assertTrue(rule.getGroup().contains(AuthenticatedApi.class));
		assertTrue(rule.getGroup().contains(PreAuthorize.class));
	}

	@Test
	@DisplayName("测试单个注解允许")
	void singleAnnotation_allowed() throws Exception {
		Method m = Clean.class.getMethod("onlyPublic");
		assertDoesNotThrow(() -> rule.validate(m, m.toGenericString()));
	}

	@Test
	@DisplayName("测试多个注解拒绝")
	void twoOrMoreAnnotations_rejected() throws Exception {
		Method method = Conflict.class.getMethod("triple");
		String signature = method.toGenericString();
		IllegalStateException ex = assertThrows(IllegalStateException.class, () -> rule.validate(method, signature));
		assertTrue(ex.getMessage().contains("testGroup"));
		assertTrue(ex.getMessage().contains(PublicApi.class.getName()));
		assertTrue(ex.getMessage().contains(AuthenticatedApi.class.getName()));
		assertTrue(ex.getMessage().contains(PreAuthorize.class.getName()));
	}

	@SuppressWarnings("unused")
	private static final class Clean {

		@PublicApi
		public void onlyPublic() {
			// fixture: annotation carrier only; never invoked
		}

	}

	@SuppressWarnings("unused")
	private static final class Conflict {

		@PublicApi
		@AuthenticatedApi
		@PreAuthorize("true")
		public void triple() {
			// fixture: annotation carrier only; never invoked
		}

	}

}
