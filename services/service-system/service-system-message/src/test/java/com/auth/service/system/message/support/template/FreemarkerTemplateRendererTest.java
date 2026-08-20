package com.auth.service.system.message.support.template;

import com.auth.service.system.message.exception.MessageException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static com.auth.service.system.message.exception.MessageResultCode.TEMPLATE_RENDER_FAILED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * {@link FreemarkerTemplateRenderer} 单元测试
 *
 * @author Bunny
 */
@DisplayName("FreemarkerTemplateRenderer 模板渲染")
class FreemarkerTemplateRendererTest {

	@Test
	@DisplayName("processTemplate：正常渲染并替换变量")
	void processTemplate_shouldRenderTemplate() {
		// 合法模板应直接返回渲染结果
		String result = FreemarkerTemplateRenderer.processTemplate("greet", "Hi ${name}", Map.of("name", "Bunny"));

		assertThat(result).isEqualTo("Hi Bunny");
	}

	@Test
	@DisplayName("processTemplate：渲染失败转为 TEMPLATE_RENDER_FAILED")
	void processTemplate_shouldWrapRenderFailureAsBusinessException() {
		// 语法错误应映射为统一业务码，并带上模板名；变量前置避免 assert 内多次可抛调用
		Map<String, Object> variables = Map.of();
		assertThatThrownBy(() -> FreemarkerTemplateRenderer.processTemplate("bad", "Hi ${", variables))
			.isInstanceOf(MessageException.class)
			.satisfies(ex -> {
				MessageException business = (MessageException) ex;
				assertThat(business.getResultCode()).isEqualTo(TEMPLATE_RENDER_FAILED);
				Object[] args = business.getMessageArgs();
				assertThat(args).hasSize(2);
				assertThat(args[0]).isEqualTo("bad");
				assertThat(String.valueOf(args[1])).contains("Template render failed:");
			});
	}

}
