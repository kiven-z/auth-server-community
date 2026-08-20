package com.auth.service.example.model.entity;

import com.auth.common.data.model.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * 数据权限演示单（example_order）
 *
 * @author Bunny
 */
@TableName("example_order")
@Schema(name = "ExampleOrderEntity", title = "数据权限演示单")
@Getter
@Setter
public class ExampleOrderEntity extends BaseEntity {

	@Schema(title = "演示标题")
	private String title;

	@Schema(title = "所属部门 ID")
	private Long deptId;

}
