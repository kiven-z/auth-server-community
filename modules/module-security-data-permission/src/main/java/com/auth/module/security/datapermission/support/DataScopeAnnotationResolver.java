package com.auth.module.security.datapermission.support;

import cn.hutool.core.text.CharSequenceUtil;
import com.auth.module.security.datapermission.annotation.DataScope;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 按 MappedStatement ID 解析 {@link DataScope}，并缓存结果
 *
 * @author Bunny
 */
@Slf4j
public class DataScopeAnnotationResolver {

	private final Map<String, Optional<DataScope>> cache = new ConcurrentHashMap<>();

	/**
	 * 解析语句上的数据范围注解
	 * @param mappedStatementId Mapper 方法全限定名
	 * @return 注解；未标注时返回 null
	 */
	public DataScope resolve(String mappedStatementId) {
		if (CharSequenceUtil.isBlank(mappedStatementId)) {
			return null;
		}
		return cache.computeIfAbsent(mappedStatementId, this::findByStatementId).orElse(null);
	}

	private Optional<DataScope> findByStatementId(String statementId) {
		try {
			int idx = statementId.lastIndexOf('.');
			if (idx <= 0 || idx >= statementId.length() - 1) {
				return Optional.empty();
			}
			String className = statementId.substring(0, idx);
			String methodName = statementId.substring(idx + 1);
			Class<?> mapperClass = Class.forName(className);
			for (Method method : mapperClass.getDeclaredMethods()) {
				if (!method.getName().equals(methodName)) {
					continue;
				}
				DataScope annotation = method.getAnnotation(DataScope.class);
				if (annotation != null) {
					return Optional.of(annotation);
				}
			}
			return Optional.empty();
		}
		catch (Exception ex) {
			log.debug("Cannot resolve @DataScope for statementId={}", statementId, ex);
			return Optional.empty();
		}
	}

}
