package com.auth.service.example.model.query;

import com.auth.common.core.annotation.JsonStringFormat;
import com.auth.common.core.model.query.PageQueryRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * 数据权限演示单查询
 *
 * @author Bunny
 */
@Schema(name = "ExampleOrderQuery", title = "数据权限演示单查询")
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class ExampleOrderQuery extends PageQueryRequest {

	@JsonStringFormat
	@Schema(title = "主键")
	private Long id;

	@Schema(title = "演示标题（模糊）")
	private String title;

	@JsonStringFormat
	@Schema(title = "所属部门 ID")
	private Long deptId;

}
