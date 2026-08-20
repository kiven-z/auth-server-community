package com.auth.service.system.admin.support.dept;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.auth.service.system.admin.mapper.admin.dept.SysDeptMapper;
import com.auth.service.system.admin.model.entity.SysDeptEntity;
import com.auth.service.system.admin.model.po.dept.SysDeptPathRowPO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 部门只读批量查询：全路径解析与编码→主键映射，供导入导出复用
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Component
public class DeptLookupSupport {

	private final SysDeptMapper sysDeptMapper;

	/**
	 * 批量解析部门全路径
	 * @param ids 部门主键列表
	 * @return 部门 ID → 全路径（如 总部/研发/后端）
	 */
	public Map<Long, String> resolvePaths(List<Long> ids) {
		if (CollUtil.isEmpty(ids)) {
			return Collections.emptyMap();
		}

		List<SysDeptPathRowPO> rows = sysDeptMapper.selectDeptPathByIds(ids);
		return rows.stream()
			.collect(Collectors.toMap(SysDeptPathRowPO::getId, SysDeptPathRowPO::getDeptPath, (left, right) -> left));
	}

	/**
	 * 从行集合中提取部门编码并批量解析为主键映射
	 * @param rows 行数据集合
	 * @param deptCodeGetter 部门编码提取函数
	 * @param <R> 行数据类型
	 * @return 部门编码 → 部门主键
	 */
	public <R> Map<String, Long> resolveIdsByCodes(List<R> rows, Function<R, String> deptCodeGetter) {
		if (CollUtil.isEmpty(rows)) {
			return Map.of();
		}

		List<String> deptCodes = rows.stream()
			.map(deptCodeGetter)
			.filter(CharSequenceUtil::isNotBlank)
			.distinct()
			.toList();
		if (CollUtil.isEmpty(deptCodes)) {
			return Map.of();
		}

		return sysDeptMapper.selectActiveByDeptCodes(deptCodes)
			.stream()
			.collect(Collectors.toMap(SysDeptEntity::getDeptCode, SysDeptEntity::getId, (left, right) -> left,
					HashMap::new));
	}

}
