package com.auth.service.system.admin.support.dept;

import com.auth.service.system.admin.mapper.admin.dept.SysDeptMapper;
import com.auth.service.system.admin.model.entity.SysDeptEntity;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.common.exception.code.SystemCommonResultCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 部门写操作前的存在性与引用校验
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Slf4j
@Component
public class DeptReferenceChecker {

	private final SysDeptMapper sysDeptMapper;

	/**
	 * 加载部门，不存在则抛业务异常（含禁用节点）
	 * @param deptId 部门 ID
	 * @return 部门实体
	 */
	public SysDeptEntity getExistingActive(Long deptId) {
		SysDeptEntity existing = sysDeptMapper.selectById(deptId);
		if (existing == null) {
			log.warn("department not found: id={}", deptId);
			throw new SystemBusinessException(SystemCommonResultCode.DATA_NOT_EXIST);
		}
		return existing;
	}

	/**
	 * 校验部门存在且计算有效
	 * @param deptId 部门 ID
	 * @return 部门实体
	 */
	public SysDeptEntity requireEffective(Long deptId) {
		SysDeptEntity existing = getExistingActive(deptId);
		if (sysDeptMapper.countEffectiveById(deptId) == 0) {
			log.warn("department unavailable: id={}", deptId);
			throw new SystemBusinessException(SystemCommonResultCode.DATA_UNAVAILABLE);
		}
		return existing;
	}

}
