package com.auth.service.system.schedule.model.form;

import com.auth.common.web.valid.group.UpdateGroup;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

/**
 * 修改任务分组
 *
 * @author Bunny
 */
@Schema(name = "SysJobGroupUpdateForm", title = "修改任务分组")
@Getter
@Setter
public class SysJobGroupUpdateForm implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(title = "主键 ID")
	@NotBlank(groups = UpdateGroup.class)
	private String id;

	@Schema(title = "显示名称")
	@Size(max = 100)
	@NotBlank(groups = UpdateGroup.class)
	private String groupName;

	@Schema(title = "描述")
	@Size(max = 255)
	private String description;

	@Schema(title = "排序号")
	@NotNull(groups = UpdateGroup.class)
	private Integer orderNum;

	@Schema(title = "启用状态（true=启用分组，false=停用）")
	@NotNull(groups = UpdateGroup.class)
	private Boolean status;

}
