package com.auth.service.system.schedule.support.catalog;

import com.auth.service.system.schedule.model.vo.QuartzTaskClassVO;
import com.auth.service.system.schedule.task.builtin.HttpInvokeJob;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * {@link QuartzTaskRegistry} 单元测试。
 *
 * @author Bunny
 */
@DisplayName("QuartzTaskRegistry 白名单扫描")
@ExtendWith(MockitoExtension.class)
class QuartzTaskRegistryTest {

	@Mock
	private ApplicationContext applicationContext;

	@Test
	@DisplayName("scan：应填充 jobParamsExample")
	void scan_populatesJobParamsExample() {
		when(applicationContext.getBeanDefinitionNames()).thenReturn(new String[] { "httpInvokeJob" });
		when(applicationContext.getType("httpInvokeJob")).thenAnswer(invocation -> HttpInvokeJob.class);

		QuartzTaskRegistry registry = new QuartzTaskRegistry(applicationContext);
		registry.scan();

		List<QuartzTaskClassVO> classes = registry.listAll();
		QuartzTaskClassVO httpInvoke = classes.stream()
			.filter(item -> HttpInvokeJob.class.getName().equals(item.getClassName()))
			.findFirst()
			.orElseThrow();

		assertThat(httpInvoke.getJobParamsExample()).contains("\"method\":\"POST\"");
		assertThat(httpInvoke.getJobParamsExample()).contains("https://example.com/api/items");
	}

}
