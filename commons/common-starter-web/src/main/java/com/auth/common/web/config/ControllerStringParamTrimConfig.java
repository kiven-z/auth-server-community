package com.auth.common.web.config;

import com.auth.common.web.jackson.TrimmingStringDeserializer;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.InitBinder;

/**
 * Trims bound string inputs for web layer where Spring MVC / Jackson apply.
 * <p>
 * <strong>Covered (typically)</strong>:
 * </p>
 * <ul>
 * <li>@RequestParam、@ModelAttribute、表单字段等走 {@link WebDataBinder} 的
 * String（{@link StringTrimmerEditor}，与 Spring 一致，基于 trim 语义）</li>
 * <li>@RequestBody 中 JSON 的字符串标量：通过全局 ObjectMapper 注册的
 * {@link TrimmingStringDeserializer}（{@link String#strip()}，含常见 Unicode 空白）</li>
 * </ul>
 * <p>
 * <strong>Not a substitute for defensive coding
 * everywhere</strong>：不会处理请求头、消息队列、定时任务入参、手写 JDBC、其它进程内非 HTTP JSON
 * 反序列化路径等；业务上若存在「必须保留首尾空白」的字段，应单独约定（例如专用 DTO 或不用 String 承载）
 * </p>
 * <p>
 * 因此服务层对<strong>不可信外部边界</strong>仍可按需校验；本配置只是减少重复的前后端空格噪音
 * </p>
 *
 * @author Bunny
 */
@ConditionalOnMissingBean(ControllerStringParamTrimConfig.class)
@ControllerAdvice
public class ControllerStringParamTrimConfig {

	/**
	 * 注册字符串编辑器：去除前后空白；emptyAsNull=false 表示仅空白时得到空串而非 null
	 */
	@InitBinder
	public void initBinder(WebDataBinder binder) {
		StringTrimmerEditor propertyEditor = new StringTrimmerEditor(false);
		binder.registerCustomEditor(String.class, propertyEditor);
	}

	/**
	 * 为应用主 ObjectMapper 注册 JSON 字符串反序列化修剪；若需完全自定义 ObjectMapper，可提供同名能力的
	 * {@link Jackson2ObjectMapperBuilderCustomizer} 或自行注册
	 * {@link TrimmingStringDeserializer}
	 */
	@Bean
	public Jackson2ObjectMapperBuilderCustomizer stringTrimJacksonCustomizer() {
		return jacksonObjectMapperBuilder -> jacksonObjectMapperBuilder.deserializerByType(String.class,
				new TrimmingStringDeserializer());
	}

}
