package com.auth.service.system.admin.support.sqlbuild;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.auth.common.core.model.query.AbstractOrderSqlBuilder;
import com.auth.common.core.model.query.SortSpec;
import lombok.experimental.UtilityClass;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 岗位分页 ORDER BY 片段构建：仅允许白名单字段，防止 SQL 注入。
 *
 * @author Bunny
 */
@UtilityClass
public class SysPostPageOrderSqlBuilder extends AbstractOrderSqlBuilder {

	/**
	 * 默认排序：显示顺序升序，创建时间降序。
	 */
	public static final String DEFAULT_ORDER_BY_SQL = "p.order_num ASC, p.created_at DESC";

	private static final Map<String, String> FIELD_TO_COLUMN = Map.ofEntries(Map.entry("orderNum", "p.order_num"),
			Map.entry("createdAt", "p.created_at"), Map.entry("updatedAt", "p.updated_at"),
			Map.entry("postCode", "p.post_code"), Map.entry("postName", "p.post_name"), Map.entry("status", "p.status"),
			Map.entry("deptName", "d.dept_name"));

	/**
	 * 根据前端传入的排序规则生成 ORDER BY 子句（不含 ORDER BY 关键字）。
	 * @param sort 排序列表，可为 null
	 * @return 安全列名片段
	 */
	public static String buildOrderBySql(List<SortSpec> sort) {
		if (CollUtil.isEmpty(sort)) {
			return DEFAULT_ORDER_BY_SQL;
		}
		String joined = sort.stream()
			.flatMap(spec -> toSegment(spec, FIELD_TO_COLUMN).stream())
			.collect(Collectors.joining(", "));
		return CharSequenceUtil.isBlank(joined) ? DEFAULT_ORDER_BY_SQL : joined;
	}

}
