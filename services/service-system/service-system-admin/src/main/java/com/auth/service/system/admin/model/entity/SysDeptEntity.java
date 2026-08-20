package com.auth.service.system.admin.model.entity;

import com.auth.common.data.model.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * 系统部门主表（物理删除；禁用见 {@link #status}）
 *
 * @author Bunny
 */
@TableName("sys_dept")
@Schema(name = "SysDeptEntity", title = "系统部门")
@Getter
@Setter
public class SysDeptEntity extends BaseEntity {

	@Schema(title = "父部门 ID（0 表示顶级）")
	private Long parentId;

	@Schema(title = "部门名称")
	private String deptName;

	@Schema(title = "部门编码（唯一标识）")
	private String deptCode;

	@Schema(title = "启用状态（true=正常，false=禁用）")
	private Boolean status;

	@Schema(title = "显示顺序")
	private Integer orderNum;

}
