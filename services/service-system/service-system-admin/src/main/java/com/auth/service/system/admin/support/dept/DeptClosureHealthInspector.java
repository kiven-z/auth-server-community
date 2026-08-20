package com.auth.service.system.admin.support.dept;

import com.auth.service.system.admin.mapper.admin.dept.DeptClosureMapper;
import com.auth.service.system.admin.model.po.dept.DeptClosureDepthChainRowPO;
import com.auth.service.system.admin.model.po.dept.DeptClosureHealthStatsPO;
import com.auth.service.system.admin.model.po.dept.DeptClosureParentLinkRowPO;
import com.auth.service.system.admin.model.vo.dept.DeptClosureDepthChainAnomalyVO;
import com.auth.service.system.admin.model.vo.dept.DeptClosureHealthStatsVO;
import com.auth.service.system.admin.model.vo.dept.DeptClosureHealthVO;
import com.auth.service.system.admin.model.vo.dept.DeptClosureParentLinkAnomalyVO;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

/**
 * 部门闭包表一致性诊断（只读）。
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Component
@Transactional(readOnly = true)
public class DeptClosureHealthInspector {

	private final DeptClosureMapper deptClosureMapper;

	/**
	 * 执行闭包表一致性诊断
	 * @return 健康检查结果
	 */
	public DeptClosureHealthVO inspect() {
		int sampleLimit = 500;

		long missingParentLinkCount = deptClosureMapper.countParentLinkAnomalies();
		List<DeptClosureParentLinkRowPO> parentLinkSamples = deptClosureMapper.selectParentLinkAnomalies(sampleLimit);

		long depthChainAnomalyCount = deptClosureMapper.countDepthChainAnomalies();
		List<DeptClosureDepthChainRowPO> depthChainSamples = deptClosureMapper.selectDepthChainAnomalies(sampleLimit);

		DeptClosureHealthVO result = new DeptClosureHealthVO();
		result.setCheckedAt(Instant.now());
		result.setMissingParentLinkCount(missingParentLinkCount);
		result.setParentLinkSampleTruncated(missingParentLinkCount > parentLinkSamples.size());
		result.setParentLinkAnomalies(parentLinkSamples.stream().map(row -> {
			DeptClosureParentLinkAnomalyVO anomalyVO = new DeptClosureParentLinkAnomalyVO();
			anomalyVO.setId(row.getId());
			anomalyVO.setDeptName(row.getDeptName());
			anomalyVO.setParentId(row.getParentId());
			anomalyVO.setParentLinkStatus("MISSING_PARENT_LINK");
			return anomalyVO;
		}).toList());
		result.setDepthChainAnomalyCount(depthChainAnomalyCount);
		result.setDepthChainSampleTruncated(depthChainAnomalyCount > depthChainSamples.size());
		result.setDepthChainAnomalies(depthChainSamples.stream().map(row -> {
			DeptClosureDepthChainAnomalyVO anomalyVO = new DeptClosureDepthChainAnomalyVO();
			anomalyVO.setId(row.getId());
			anomalyVO.setDeptName(row.getDeptName());
			anomalyVO.setParentId(row.getParentId());
			anomalyVO.setChildClosureCnt(row.getChildClosureCnt());
			anomalyVO.setParentClosureCnt(row.getParentClosureCnt());
			anomalyVO.setExpectedChildCnt(row.getExpectedChildCnt());
			return anomalyVO;
		}).toList());

		DeptClosureHealthStatsVO vo = getDeptClosureHealthStatsVO();
		result.setHealthStats(vo);
		result.setPassed(missingParentLinkCount == 0 && depthChainAnomalyCount == 0);

		return result;
	}

	/**
	 * 获取部门闭包健康统计
	 * @return 部门闭包健康统计
	 */
	@NotNull
	private DeptClosureHealthStatsVO getDeptClosureHealthStatsVO() {
		DeptClosureHealthStatsPO statsPo = deptClosureMapper.selectHealthStats();
		DeptClosureHealthStatsPO safePo = Objects.requireNonNullElse(statsPo, new DeptClosureHealthStatsPO());
		DeptClosureHealthStatsVO vo = new DeptClosureHealthStatsVO();
		vo.setOnlySelf(Objects.requireNonNullElse(safePo.getOnlySelf(), 0L));
		vo.setHasAncestors(Objects.requireNonNullElse(safePo.getHasAncestors(), 0L));
		vo.setZeroClosure(Objects.requireNonNullElse(safePo.getZeroClosure(), 0L));
		vo.setTotal(Objects.requireNonNullElse(safePo.getTotal(), 0L));
		return vo;
	}

}
