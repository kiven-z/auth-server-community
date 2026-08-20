package com.auth.service.system.admin.model.vo.dept;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.List;

/**
 * 部门闭包表健康检查结果
 *
 * @author Bunny
 */
@Schema(name = "DeptClosureHealthVO", title = "部门闭包表健康检查")
@Getter
@Setter
@ToString
public class DeptClosureHealthVO implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(title = "是否通过（无父子直连缺失且无深度链条异常）")
	private Boolean passed;

	@Schema(title = "检查时间")
	private Instant checkedAt;

	@Schema(title = "健康度统计")
	private DeptClosureHealthStatsVO healthStats;

	@Schema(title = "父子直连缺失总数")
	private Long missingParentLinkCount;

	@Schema(title = "父子直连异常样本是否被截断")
	private Boolean parentLinkSampleTruncated;

	@Schema(title = "父子直连异常样本")
	private List<DeptClosureParentLinkAnomalyVO> parentLinkAnomalies;

	@Schema(title = "深度链条异常总数")
	private Long depthChainAnomalyCount;

	@Schema(title = "深度链条异常样本是否被截断")
	private Boolean depthChainSampleTruncated;

	@Schema(title = "深度链条异常样本")
	private List<DeptClosureDepthChainAnomalyVO> depthChainAnomalies;

}
