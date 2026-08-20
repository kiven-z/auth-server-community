package com.auth.module.security.datapermission.handler;

import com.auth.module.security.contract.api.authorization.AuthProfile;
import com.auth.module.security.contract.api.authorization.ScopeGrant;
import com.auth.module.security.contract.api.datascope.DataScopeStorageType;
import com.auth.module.security.datapermission.annotation.DataScope;
import com.auth.module.security.datapermission.context.LoginUserProvider;
import com.auth.module.security.datapermission.support.DataScopeAnnotationResolver;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * {@link DataScopePermissionHandler} 表达式合并测试
 *
 * <p>
 * 全量 SQL 改写由官方 DataPermissionInterceptor 负责；其依赖的 CCJSqlParserTokenManager 在 JaCoCo 仪器化时会
 * MethodTooLarge，故不在此调用 beforeQuery。
 * </p>
 *
 * @author Bunny
 */
@ExtendWith(MockitoExtension.class)
class DataScopePermissionHandlerTest {

	@Mock
	private LoginUserProvider loginUserProvider;

	private DataScopePermissionHandler handler;

	private static AuthProfile selfUser(Long userId) {
		return AuthProfile.builder()
			.userId(userId)
			.deptScope(ScopeGrant.builder().scopeType(DataScopeStorageType.SELF).build())
			.build();
	}

	private static String statementId(String methodName) {
		return DataScopePermissionHandlerTest.class.getName() + '.' + methodName;
	}

	private static String normalize(Expression expression) {
		return expression.toString().replaceAll("\\s+", " ").trim();
	}

	@BeforeEach
	void setUp() {
		handler = new DataScopePermissionHandler(loginUserProvider, new DataScopeAnnotationResolver());
	}

	@DataScope(alias = "d")
	void annotatedDefault() {
		// fixture: annotation carrier only; never invoked
	}

	@DataScope(alias = "unsafe-alias")
	void annotatedUnsafeAlias() {
		// fixture: annotation carrier only; never invoked
	}

	void withoutAnnotation() {
		// fixture: no @DataScope
	}

	@Test
	@DisplayName("无 @DataScope 时原样返回 WHERE")
	void withoutAnnotation_shouldKeepWhere() throws Exception {
		Expression where = CCJSqlParserUtil.parseCondExpression("d.status = 1");
		Expression merged = handler.getSqlSegment(where, statementId("withoutAnnotation"));
		assertSame(where, merged);
	}

	@Test
	@DisplayName("无登录用户时不注入")
	void withoutUser_shouldKeepWhere() throws Exception {
		when(loginUserProvider.currentUser()).thenReturn(null);
		Expression where = CCJSqlParserUtil.parseCondExpression("d.status = 1");
		Expression merged = handler.getSqlSegment(where, statementId("annotatedDefault"));
		assertSame(where, merged);
	}

	@Test
	@DisplayName("ALL 范围不注入")
	void allScope_shouldKeepWhere() throws Exception {
		when(loginUserProvider.currentUser()).thenReturn(AuthProfile.builder()
			.userId(10L)
			.deptScope(ScopeGrant.builder().scopeType(DataScopeStorageType.ALL).build())
			.build());
		Expression where = CCJSqlParserUtil.parseCondExpression("d.status = 1");
		Expression merged = handler.getSqlSegment(where, statementId("annotatedDefault"));
		assertSame(where, merged);
	}

	@Test
	@DisplayName("SELF 范围在无 WHERE 时直接返回范围表达式")
	void self_withoutWhere_shouldReturnScope() {
		when(loginUserProvider.currentUser()).thenReturn(selfUser(10L));
		Expression merged = handler.getSqlSegment(null, statementId("annotatedDefault"));
		assertEquals("(d.created_by = 10)", normalize(merged));
	}

	@Test
	@DisplayName("SELF 范围与原 WHERE 做 AND")
	void self_withWhere_shouldAnd() throws Exception {
		when(loginUserProvider.currentUser()).thenReturn(selfUser(10L));
		Expression where = CCJSqlParserUtil.parseCondExpression("d.status = 1");
		Expression merged = handler.getSqlSegment(where, statementId("annotatedDefault"));
		assertInstanceOf(AndExpression.class, merged);
		assertEquals("d.status = 1 AND (d.created_by = 10)", normalize(merged));
	}

	@Test
	@DisplayName("DEPT 范围生成带括号的 IN 条件")
	void dept_shouldReturnInExpression() {
		when(loginUserProvider.currentUser()).thenReturn(AuthProfile.builder()
			.userId(10L)
			.deptScope(ScopeGrant.builder().scopeType(DataScopeStorageType.DEPT).values(List.of(1L, 2L)).build())
			.build());
		Expression merged = handler.getSqlSegment(null, statementId("annotatedDefault"));
		assertEquals("(d.dept_id IN (1, 2))", normalize(merged));
	}

	@Test
	@DisplayName("非法 alias 时回退为无别名列引用")
	void invalidAlias_shouldFallbackToUnqualifiedColumn() {
		when(loginUserProvider.currentUser()).thenReturn(selfUser(10L));
		Expression merged = handler.getSqlSegment(null, statementId("annotatedUnsafeAlias"));
		assertEquals("(created_by = 10)", normalize(merged));
	}

}
