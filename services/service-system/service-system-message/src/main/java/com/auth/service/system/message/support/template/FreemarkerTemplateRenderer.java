package com.auth.service.system.message.support.template;

import com.auth.service.system.message.exception.MessageException;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.io.StringWriter;

import static com.auth.service.system.message.exception.MessageResultCode.TEMPLATE_RENDER_FAILED;

/**
 * 字符串模板渲染（多渠道共用）
 *
 * @author Bunny
 */
@UtilityClass
@Slf4j
public class FreemarkerTemplateRenderer {

	public static final Configuration CONFIGURATION = createConfiguration();

	/**
	 * 渲染模板
	 * @param templateName 模板名称
	 * @param templateContent 模板内容
	 * @param dataModel 数据模型
	 * @return 渲染后的模板
	 */
	public static String processTemplate(String templateName, String templateContent, Object dataModel) {
		try {
			Template template = new Template(templateName, templateContent, CONFIGURATION);

			try (StringWriter writer = new StringWriter()) {
				template.process(dataModel, writer);
				return writer.toString();
			}
		}
		catch (Exception e) {
			log.error("Template render failed, templateName={}", templateName, e);
			throw new MessageException(TEMPLATE_RENDER_FAILED, templateName,
					"Template render failed: " + e.getMessage());
		}
	}

	/**
	 * 创建配置
	 * @return 配置
	 */
	private static Configuration createConfiguration() {
		try {
			Configuration cfg = new Configuration(Configuration.VERSION_2_3_31);
			cfg.setDefaultEncoding("UTF-8");
			cfg.setOutputEncoding("UTF-8");
			cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
			cfg.setNumberFormat("#0.#####");
			cfg.setBooleanFormat("true,false");
			cfg.setDateTimeFormat("yyyy-MM-dd HH:mm:ss");
			cfg.setDateFormat("yyyy-MM-dd");
			cfg.setTimeFormat("HH:mm:ss");
			cfg.setClassicCompatible(true);
			cfg.setCacheStorage(new freemarker.cache.MruCacheStorage(20, 250));
			return cfg;
		}
		catch (Exception e) {
			log.error("Failed to initialize FreeMarker configuration", e);
			throw new IllegalStateException("Failed to initialize FreeMarker configuration", e);
		}
	}

}
