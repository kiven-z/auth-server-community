package com.auth.service.system.admin.support.scope;

import cn.hutool.core.collection.CollUtil;
import com.auth.module.security.contract.profile.datascope.DataScopeDimension;
import com.auth.module.security.contract.profile.datascope.DataScopeStorageType;
import com.auth.service.system.admin.mapper.admin.dept.SysDeptMapper;
import com.auth.service.system.admin.model.entity.SysDeptEntity;
import com.auth.service.system.admin.model.form.scope.SysDataScopeForm;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.common.exception.code.SystemCommonResultCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 数据范围表单解析
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Slf4j
@Component
public class DataScopeFormSupport {

	private final SysDeptMapper sysDeptMapper;

	/**
	 * 解析维度、范围类型并规范化节点 ID
	 * @param form 数据范围表单
	 * @return 已解析维度、类型与节点 ID
	 */
	public ResolvedDataScope resolve(SysDataScopeForm form) {
		DataScopeDimension dimension = DataScopeDimension.parse(form.getDimension());
		DataScopeStorageType scopeType = DataScopeStorageType.parse(form.getScopeType());
		if (dimension == null || scopeType == null) {
			throw new SystemBusinessException(SystemCommonResultCode.DATA_INVALID);
		}
		List<Long> scopeIds = normalizeIds(dimension, scopeType, form.getScopeIds());
		return new ResolvedDataScope(dimension, scopeType, scopeIds, form.getRemark());
	}

	/**
	 * 按提交顺序解析整份范围列表。同一维出现多次时拒绝
	 * @param forms 数据范围表单；空列表表示清空
	 * @return 与表单顺序一致的解析结果
	 */
	public List<ResolvedDataScope> resolveAll(List<SysDataScopeForm> forms) {
		if (forms == null) {
			throw new SystemBusinessException(SystemCommonResultCode.DATA_INVALID);
		}

		List<ResolvedDataScope> resolved = new ArrayList<>(forms.size());
		Set<DataScopeDimension> seen = new LinkedHashSet<>();
		for (SysDataScopeForm form : forms) {
			if (form == null) {
				throw new SystemBusinessException(SystemCommonResultCode.DATA_INVALID);
			}
			ResolvedDataScope item = resolve(form);
			if (!seen.add(item.dimension())) {
				throw new SystemBusinessException(SystemCommonResultCode.PARAM_DUPLICATE);
			}
			resolved.add(item);
		}
		return List.copyOf(resolved);
	}

	/**
	 * 规范化节点 ID：IDS / IDS_AND_CHILD 必填； 树形维校验节点存在； ALL/SELF 清空
	 * @param dimension 数据维度
	 * @param scopeType 范围类型
	 * @param rawIds 表单节点 ID
	 * @return 去重后的节点 ID；ALL/SELF 为空列表
	 */
	private List<Long> normalizeIds(DataScopeDimension dimension, DataScopeStorageType scopeType, List<Long> rawIds) {
		if (scopeType != DataScopeStorageType.IDS && scopeType != DataScopeStorageType.IDS_AND_CHILD) {
			return List.of();
		}
		if (CollUtil.isEmpty(rawIds)) {
			throw new SystemBusinessException(SystemCommonResultCode.PARAM_REQUIRED);
		}
		List<Long> distinctIds = rawIds.stream().distinct().toList();
		if (dimension.isTree()) {
			requireExistingDeptIds(distinctIds);
		}
		return distinctIds;
	}

	/**
	 * 批量校验部门 ID 均存在（含停用节点）
	 * @param deptIds 已去重的部门 ID
	 */
	private void requireExistingDeptIds(List<Long> deptIds) {
		List<SysDeptEntity> found = sysDeptMapper.selectByIds(deptIds);
		Set<Long> foundIds = found.stream().map(SysDeptEntity::getId).collect(Collectors.toSet());
		if (foundIds.size() != deptIds.size()) {
			Long invalidId = deptIds.stream()
				.filter(id -> !foundIds.contains(id))
				.findFirst()
				.orElseThrow(() -> new IllegalStateException("dept id mismatch without invalid id"));
			log.warn("Scope dept reference invalid: id={}", invalidId);
			throw new SystemBusinessException(SystemCommonResultCode.GRANT_REFERENCE_INVALID);
		}
	}

	/**
	 * 表单解析结果
	 *
	 * @param dimension 已解析的数据维度
	 * @param scopeType 已解析的范围类型
	 * @param scopeIds 已规范化的节点 ID（ALL/SELF 为空列表）
	 * @param remark 备注
	 */
	public record ResolvedDataScope(DataScopeDimension dimension, DataScopeStorageType scopeType, List<Long> scopeIds,
			String remark) {
	}

}
