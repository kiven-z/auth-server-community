package com.auth.service.example.model.vo;

import com.auth.common.core.annotation.JsonStringFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * 数据权限演示单列表行
 *
 * @author Bunny
 */
@Schema(name = "ExampleOrderVO", title = "数据权限演示单列表行")
@Getter
@Setter
public class ExampleOrderVO {

	@JsonStringFormat
	@Schema(title = "主键")
	private Long id;

	@Schema(title = "演示标题")
	private String title;

	@JsonStringFormat
	@Schema(title = "所属部门 ID")
	private Long deptId;

	@JsonStringFormat
	@Schema(title = "创建人")
	private Long createdBy;

}
