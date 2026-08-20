package com.auth.service.system.admin.model.vo.dept;

import com.auth.common.core.annotation.JsonStringFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;

/**
 * 闭包深度链条异常行
 *
 * @author Bunny
 */
@Schema(name = "DeptClosureDepthChainAnomalyVO", title = "闭包深度链条异常")
@Getter
@Setter
@ToString
public class DeptClosureDepthChainAnomalyVO implements Serializable {

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

	@Schema(title = "当前部门闭包行数")
	private Long childClosureCnt;

	@Schema(title = "父部门闭包行数")
	private Long parentClosureCnt;

	@Schema(title = "期望闭包行数（父行数 + 1）")
	private Long expectedChildCnt;

}
