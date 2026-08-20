package com.auth.service.system.admin.support;

import com.auth.common.core.model.query.SortDirection;
import com.auth.common.core.model.query.SortSpec;
import com.auth.service.system.admin.support.sqlbuild.SysUserPageOrderSqlBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link SysUserPageOrderSqlBuilder} 单元测试。
 *
 * @author Bunny
 */
@DisplayName("SysUserPageOrderSqlBuilder 用户分页排序")
class SysUserPageOrderSqlBuilderTest {

	@Test
	@DisplayName("空排序：回退默认 created_at DESC")
	void buildOrderBySqlFallsBackToDefaultWhenSortEmpty() {
		assertThat(SysUserPageOrderSqlBuilder.buildOrderBySql(null))
			.isEqualTo(SysUserPageOrderSqlBuilder.DEFAULT_ORDER_BY_SQL);
		assertThat(SysUserPageOrderSqlBuilder.buildOrderBySql(List.of()))
			.isEqualTo(SysUserPageOrderSqlBuilder.DEFAULT_ORDER_BY_SQL);
	}

	@Test
	@DisplayName("白名单字段：生成安全 ORDER BY 片段")
	void buildOrderBySqlMapsWhitelistedField() {
		SortSpec spec = new SortSpec();
		spec.setField("username");
		spec.setDirection(SortDirection.ASC);

		assertThat(SysUserPageOrderSqlBuilder.buildOrderBySql(List.of(spec))).isEqualTo("u.username ASC");
	}

}
