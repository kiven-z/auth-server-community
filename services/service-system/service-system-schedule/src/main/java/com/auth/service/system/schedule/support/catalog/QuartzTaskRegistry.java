package com.auth.service.system.schedule.support.catalog;

import cn.hutool.core.text.CharSequenceUtil;
import com.auth.service.system.schedule.annotation.QuartzTask;
import com.auth.service.system.schedule.model.vo.QuartzTaskClassVO;
import jakarta.annotation.PostConstruct;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

/**
 * 扫描容器中带 {@link QuartzTask} 的 Bean 类型，维护反射调用白名单
 *
 * @author Bunny
 */
@Component
public class QuartzTaskRegistry {

	/**
	 * 类全限定名 -> 元数据
	 */
	private final Map<String, QuartzTaskClassVO> allowedClasses = new ConcurrentHashMap<>();

	/**
	 * 类全限定名 -> 扫描到的 Spring Bean 名
	 */
	private final Map<String, String> classNameToBeanName = new ConcurrentHashMap<>();

	/**
	 * Spring 上下文
	 */
	private final ApplicationContext applicationContext;

	public QuartzTaskRegistry(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}

	/**
	 * 容器就绪后扫描所有 Bean 定义对应的类型
	 */
	@PostConstruct
	public void scan() {
		for (String beanName : applicationContext.getBeanDefinitionNames()) {
			Class<?> type = applicationContext.getType(beanName);
			if (type == null) {
				continue;
			}
			QuartzTask ann = AnnotationUtils.findAnnotation(type, QuartzTask.class);
			if (ann != null) {
				QuartzTaskClassVO vo = new QuartzTaskClassVO();
				vo.setClassName(type.getName());
				vo.setName(ann.name());
				vo.setDescription(ann.description());
				vo.setInvokeModes(Stream.of(ann.modes()).map(Enum::name).toList());
				vo.setJobParamsExample(CharSequenceUtil.trim(ann.example()));
				String className = type.getName();
				allowedClasses.put(className, vo);
				classNameToBeanName.put(className, beanName);
			}
		}
	}

	/**
	 * 解析白名单类对应的 Spring Bean 名
	 * @param className 类全限定名
	 * @return Bean 名；不在白名单时返回 null
	 */
	public String resolveBeanName(String className) {
		return classNameToBeanName.get(className);
	}

	/**
	 * 是否允许在任务中配置该类
	 * @param className 全限定类名
	 * @return 是否白名单内
	 */
	public boolean isAllowed(String className) {
		return className != null && allowedClasses.containsKey(className);
	}

	/**
	 * 获取所有白名单项（不可变视图）
	 * @return 所有白名单项（不可变视图）
	 */
	public List<QuartzTaskClassVO> listAll() {
		Collection<QuartzTaskClassVO> values = allowedClasses.values();
		return List.copyOf(values);
	}

}
