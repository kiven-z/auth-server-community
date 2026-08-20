package com.auth.service.system.admin.model.vo.dept;

import com.auth.common.core.annotation.JsonStringFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;

/**
 * 父子直连缺失异常行
 *
 * @author Bunny
 */
@Schema(name = "DeptClosureParentLinkAnomalyVO", title = "闭包父子直连异常")
@Getter
@Setter
@ToString
public class DeptClosureParentLinkAnomalyVO implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@JsonStringFormat
	@Schema(title = "部门 ID")
	private Long id;

	@Schema(title = "部门名称")
	private String deptName;

	@JsonStringFormat
	@Schema(title = "父部门 ID")
	private Long parentId;

	@Schema(title = "诊断状态")
	private String parentLinkStatus;

}
