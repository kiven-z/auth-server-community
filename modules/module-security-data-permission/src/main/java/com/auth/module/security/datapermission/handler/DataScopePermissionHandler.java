package com.auth.module.security.datapermission.handler;

import cn.hutool.core.text.CharSequenceUtil;
import com.auth.module.security.contract.api.authorization.AuthProfile;
import com.auth.module.security.datapermission.annotation.DataScope;
import com.auth.module.security.datapermission.context.LoginUserProvider;
import com.auth.module.security.datapermission.support.DataScopeAnnotationResolver;
import com.baomidou.mybatisplus.extension.plugins.handler.DataPermissionHandler;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * MyBatis-Plus 数据权限 Handler：将 {@link DataScope} 条件以 Expression 注入 WHERE
 *
 * @author Bunny
 */
@Slf4j
public class DataScopePermissionHandler implements DataPermissionHandler {

	private static final String FAIL_CLOSE_CONDITION = "1 = 0";

	private final LoginUserProvider loginUserProvider;

	private final DataScopeAnnotationResolver annotationResolver;

	private final Map<Class<? extends DataScopeHandler>, DataScopeHandler> handlerCache = new ConcurrentHashMap<>();

	public DataScopePermissionHandler(LoginUserProvider loginUserProvider,
			DataScopeAnnotationResolver annotationResolver) {
		this.loginUserProvider = loginUserProvider;
		this.annotationResolver = annotationResolver;
	}

	/**
	 * 合并原 WHERE 与数据范围条件
	 * @param where 原 WHERE 表达式，可为 null
	 * @param mappedStatementId Mapper 方法全限定名
	 * @return 合并后的 WHERE；无需过滤时返回原 where
	 */
	@Override
	public Expression getSqlSegment(Expression where, String mappedStatementId) {
		DataScope dataScope = annotationResolver.resolve(mappedStatementId);
		if (dataScope == null) {
			return where;
		}
		AuthProfile profile = loginUserProvider != null ? loginUserProvider.currentUser() : null;
		if (profile == null) {
			return where;
		}
		String condition = resolveHandler(dataScope).buildCondition(profile, dataScope);
		if (CharSequenceUtil.isBlank(condition)) {
			return where;
		}
		Expression scopeExpression = parseCondition(condition);
		if (where == null) {
			return scopeExpression;
		}
		return new AndExpression(where, scopeExpression);
	}

	private DataScopeHandler resolveHandler(DataScope dataScope) {
		Class<? extends DataScopeHandler> handlerClass = dataScope.handler();
		return handlerCache.computeIfAbsent(handlerClass, clazz -> {
			try {
				return clazz.getDeclaredConstructor().newInstance();
			}
			catch (Exception ex) {
				throw new IllegalStateException("Failed to create DataScopeHandler: " + clazz.getName(), ex);
			}
		});
	}

	private Expression parseCondition(String condition) {
		// 用括号包裹，避免与原 WHERE 做 AND 时优先级错乱（JSqlParser 5.x Parenthesis#setExpression
		// 不能对空列表赋值）
		String wrapped = "(" + condition + ")";
		try {
			return CCJSqlParserUtil.parseCondExpression(wrapped);
		}
		catch (Exception ex) {
			log.error("Failed to parse data scope condition, fail-closed. condition={}", condition, ex);
			try {
				return CCJSqlParserUtil.parseCondExpression("(" + FAIL_CLOSE_CONDITION + ")");
			}
			catch (Exception nested) {
				throw new IllegalStateException("Failed to parse fail-close data scope condition.", nested);
			}
		}
	}

}
