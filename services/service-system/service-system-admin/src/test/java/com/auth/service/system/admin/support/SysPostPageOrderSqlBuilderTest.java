package com.auth.service.system.admin.support;

import com.auth.common.core.model.query.SortDirection;
import com.auth.common.core.model.query.SortSpec;
import com.auth.service.system.admin.support.sqlbuild.SysPostPageOrderSqlBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link SysPostPageOrderSqlBuilder} 单元测试。
 *
 * @author Bunny
 */
@DisplayName("SysPostPageOrderSqlBuilder 岗位分页排序")
class SysPostPageOrderSqlBuilderTest {

	@Test
	@DisplayName("sort 为空时使用默认排序片段")
	void buildOrderBySqlUsesDefaultWhenSortEmpty() {
		assertThat(SysPostPageOrderSqlBuilder.buildOrderBySql(null))
			.isEqualTo(SysPostPageOrderSqlBuilder.DEFAULT_ORDER_BY_SQL);
		assertThat(SysPostPageOrderSqlBuilder.buildOrderBySql(List.of()))
			.isEqualTo(SysPostPageOrderSqlBuilder.DEFAULT_ORDER_BY_SQL);
	}

	@Test
	@DisplayName("白名单字段 postCode 生成安全片段")
	void buildOrderBySqlMapsWhitelistedField() {
		SortSpec spec = new SortSpec();
		spec.setField("postCode");
		spec.setDirection(SortDirection.ASC);

		assertThat(SysPostPageOrderSqlBuilder.buildOrderBySql(List.of(spec))).isEqualTo("p.post_code ASC");
	}

}
