package com.auth.service.system.schedule.model.entity;

import com.auth.common.data.model.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * 定时任务分组
 *
 * @author Bunny
 */
@TableName("job_group")
@Schema(name = "JobGroupEntity", title = "定时任务分组")
@Getter
@Setter
@Accessors(chain = true)
public class JobGroupEntity extends BaseEntity {

	@Schema(title = "分组编码（大写）")
	private String groupCode;

	@Schema(title = "显示名称")
	private String groupName;

	@Schema(title = "描述")
	private String description;

	@Schema(title = "启用状态（true=启用分组，false=停用）")
	private Boolean status;

	@Schema(title = "系统内置分组（true 表示内置）")
	private Boolean isSystem;

	@Schema(title = "排序号")
	private Integer orderNum;

}
