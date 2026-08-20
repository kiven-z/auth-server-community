package com.auth.service.system.admin.model.entity;

import com.auth.common.data.model.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * 系统岗位主表
 *
 * @author Bunny
 */
@TableName("sys_post")
@Schema(name = "SysPostEntity", title = "系统岗位")
@Getter
@Setter
public class SysPostEntity extends BaseEntity {

	@Schema(title = "所属部门ID")
	private Long deptId;

	@Schema(title = "岗位编码")
	private String postCode;

	@Schema(title = "岗位名称")
	private String postName;

	@Schema(title = "启用状态（true=启用，false=停用）")
	private Boolean status;

	@Schema(title = "显示顺序")
	private Integer orderNum;

}
