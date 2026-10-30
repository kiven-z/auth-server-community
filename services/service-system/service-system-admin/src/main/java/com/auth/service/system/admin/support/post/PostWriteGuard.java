package com.auth.service.system.admin.support.post;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.auth.common.data.support.BusinessKeyAssert;
import com.auth.service.system.admin.excel.post.SysPostImportRow;
import com.auth.service.system.admin.exception.SystemAdminResultCode;
import com.auth.service.system.admin.mapper.admin.dept.SysDeptMapper;
import com.auth.service.system.admin.mapper.admin.post.SysPostMapper;
import com.auth.service.system.admin.model.entity.SysDeptEntity;
import com.auth.service.system.admin.model.form.post.SysPostForm;
import com.auth.service.system.admin.model.po.post.PostDeptCodePairPO;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.common.exception.code.SystemCommonResultCode;
import lombok.Builder;
import lombok.Value;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 岗位写入复合键校验：部门存在性与 (deptId, postCode) 唯一性
 *
 * @author Bunny
 */
@UtilityClass
@Slf4j
public class PostWriteGuard {

	/**
	 * 复合业务键分隔符（部门与岗位编码拼接用）
	 */
	public static final String COMPOSITE_KEY_SEP = "\u0000";

	/**
	 * 由部门主键与岗位编码构建复合键
	 * @param deptId 部门主键
	 * @param postCode 岗位编码
	 * @return 复合键字符串
	 */
	public static String deptIdPostCodeKey(Long deptId, String postCode) {
		return deptId + COMPOSITE_KEY_SEP + postCode;
	}

	/**
	 * 由部门编码与岗位编码构建文件内复合键
	 * @param deptCode 部门编码
	 * @param postCode 岗位编码
	 * @return 复合键字符串
	 */
	public static String deptCodePostCodeKey(String deptCode, String postCode) {
		return deptCode + COMPOSITE_KEY_SEP + postCode;
	}

	/**
	 * 批量新增前校验：部门存在、批次内键唯一、库中无冲突
	 * @param postMapper 岗位 Mapper
	 * @param deptMapper 部门 Mapper
	 * @param forms 待新增表单
	 */
	public static void requireBatchCreatable(SysPostMapper postMapper, SysDeptMapper deptMapper,
			List<SysPostForm> forms) {
		// 1. 传入表单为空不保存
		if (CollUtil.isEmpty(forms)) {
			return;
		}

		// 2. 去重后是否为空，为空不保存
		List<Long> distinctDeptIds = forms.stream()
			.map(SysPostForm::getDeptId)
			.filter(Objects::nonNull)
			.distinct()
			.toList();
		if (CollUtil.isEmpty(distinctDeptIds)) {
			return;
		}

		// 3. 检查目标部门是否存在
		Set<Long> existingDeptIds = deptMapper.selectByIds(distinctDeptIds)
			.stream()
			.map(SysDeptEntity::getId)
			.collect(Collectors.toSet());
		Long missingDeptId = distinctDeptIds.stream()
			.filter(deptId -> !existingDeptIds.contains(deptId))
			.findFirst()
			.orElse(null);
		if (missingDeptId != null) {
			log.warn("department not found: id={}", missingDeptId);
			throw new SystemBusinessException(SystemCommonResultCode.DATA_NOT_EXIST);
		}

		// 4. 检查批次内复合键是否唯一
		BusinessKeyAssert.requireDistinctBy(forms, form -> deptIdPostCodeKey(form.getDeptId(), form.getPostCode()),
				form -> {
					log.warn("Post code duplicate in department: deptId={}, postCode={}", form.getDeptId(),
							form.getPostCode());
					return new SystemBusinessException(SystemAdminResultCode.POST_CODE_DUPLICATE_IN_DEPT,
							form.getPostCode());
				});

		// 5. 检查库中是否存在复合键
		List<PostDeptCodePairPO> pairs = forms.stream().map(form -> {
			PostDeptCodePairPO pair = new PostDeptCodePairPO();
			pair.setDeptId(form.getDeptId());
			pair.setPostCode(form.getPostCode());
			return pair;
		}).toList();
		List<PostDeptCodePairPO> existing = postMapper.selectReferenceByDeptPostPairs(pairs);
		if (CollUtil.isNotEmpty(existing)) {
			PostDeptCodePairPO conflict = existing.get(0);
			log.warn("Post code duplicate in department: deptId={}, postCode={}", conflict.getDeptId(),
					conflict.getPostCode());
			throw new SystemBusinessException(SystemAdminResultCode.POST_CODE_DUPLICATE_IN_DEPT,
					conflict.getPostCode());
		}
	}

	/**
	 * 导入预检：文件内重复键、库中已存在键
	 * @param mapper 岗位 Mapper
	 * @param rows 导入行
	 * @param deptIdByCode 部门编码与主键映射
	 * @return 预检结果
	 */
	public static ImportPrecheck precheckForImport(SysPostMapper mapper, List<SysPostImportRow> rows,
			Map<String, Long> deptIdByCode) {
		return ImportPrecheck.builder()
			.duplicateCompositeKeysInFile(detectDuplicateCompositeKeysInFile(rows))
			.existingCompositeKeys(loadExistingCompositeKeys(mapper, rows, deptIdByCode))
			.build();
	}

	/**
	 * 检测文件内重复的 (deptCode, postCode) 复合键
	 * @param rows 导入行
	 * @return 重复复合键集合
	 */
	private static Set<String> detectDuplicateCompositeKeysInFile(List<SysPostImportRow> rows) {
		Set<String> seen = new HashSet<>();
		Set<String> duplicates = new HashSet<>();

		for (SysPostImportRow row : rows) {
			if (CharSequenceUtil.isBlank(row.getDeptCode()) || CharSequenceUtil.isBlank(row.getPostCode())) {
				continue;
			}
			String compositeKey = deptCodePostCodeKey(row.getDeptCode().trim(), row.getPostCode().trim());
			if (!seen.add(compositeKey)) {
				duplicates.add(compositeKey);
			}
		}
		return duplicates;
	}

	/**
	 * 加载库中已存在的 (deptId, postCode) 复合键
	 * @param mapper 岗位 Mapper
	 * @param rows 导入行
	 * @param deptIdByCode 部门编码与主键映射
	 * @return 已存在复合键集合
	 */
	private static Set<String> loadExistingCompositeKeys(SysPostMapper mapper, List<SysPostImportRow> rows,
			Map<String, Long> deptIdByCode) {
		// 导入行转为 (部门 ID, 岗位编码) 查询对（仅含已解析出部门主键的行）
		List<PostDeptCodePairPO> pairs = rows.stream()
			.filter(row -> CharSequenceUtil.isNotBlank(row.getDeptCode())
					&& CharSequenceUtil.isNotBlank(row.getPostCode()))
			.map(row -> {
				Long deptId = deptIdByCode.get(row.getDeptCode().trim());
				if (deptId == null) {
					return null;
				}
				PostDeptCodePairPO pair = new PostDeptCodePairPO();
				pair.setDeptId(deptId);
				pair.setPostCode(row.getPostCode().trim());
				return pair;
			})
			.filter(Objects::nonNull)
			.distinct()
			.toList();
		if (pairs.isEmpty()) {
			return Set.of();
		}
		return mapper.selectReferenceByDeptPostPairs(pairs)
			.stream()
			.map(pair -> deptIdPostCodeKey(pair.getDeptId(), pair.getPostCode()))
			.collect(Collectors.toSet());
	}

	/**
	 * 岗位导入预检上下文
	 */
	@Value
	@Builder
	public static class ImportPrecheck {

		Set<String> duplicateCompositeKeysInFile;

		Set<String> existingCompositeKeys;

	}

}
