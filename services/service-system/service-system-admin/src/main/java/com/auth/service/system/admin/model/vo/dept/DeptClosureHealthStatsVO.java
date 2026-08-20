package com.auth.service.system.admin.model.vo.dept;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;

/**
 * 闭包健康度统计（活跃部门维度）
 *
 * @author Bunny
 */
@Schema(name = "DeptClosureHealthStatsVO", title = "闭包健康度统计")
@Getter
@Setter
@ToString
public class DeptClosureHealthStatsVO implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(title = "闭包行数仅为自环的部门数")
	private Long onlySelf;

	@Schema(title = "闭包行数大于 1 的部门数")
	private Long hasAncestors;

	@Schema(title = "无任何闭包行的活跃部门数")
	private Long zeroClosure;

	@Schema(title = "活跃部门总数")
	private Long total;

}
