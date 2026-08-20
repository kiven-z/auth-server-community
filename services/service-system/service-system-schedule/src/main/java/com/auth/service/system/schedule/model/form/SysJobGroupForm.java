package com.auth.service.system.schedule.model.form;

import com.auth.common.web.valid.group.CreateGroup;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

import static com.auth.common.web.validation.ValidationPatterns.UPPER_ALNUM_UNDERSCORE;

/**
 * 新增任务分组
 *
 * @author Bunny
 */
@Schema(name = "SysJobGroupForm", title = "新增任务分组")
@Getter
@Setter
public class SysJobGroupForm implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(title = "分组编码")
	@Size(max = 64, groups = CreateGroup.class)
	@Pattern(regexp = UPPER_ALNUM_UNDERSCORE, message = "仅允许大写字母、数字、下划线", groups = CreateGroup.class)
	@NotBlank(groups = CreateGroup.class)
	private String groupCode;

	@Schema(title = "显示名称")
	@Size(max = 100, groups = CreateGroup.class)
	@NotBlank(groups = CreateGroup.class)
	private String groupName;

	@Schema(title = "描述")
	@Size(max = 255)
	private String description;

	@Schema(title = "启用状态（true=启用分组，false=停用）")
	@NotNull(groups = CreateGroup.class)
	private Boolean status;

	@Schema(title = "排序号")
	private Integer orderNum;

}
