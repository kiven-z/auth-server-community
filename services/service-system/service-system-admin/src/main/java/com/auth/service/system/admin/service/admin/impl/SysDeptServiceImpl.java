package com.auth.service.system.admin.service.admin.impl;

import cn.hutool.core.collection.CollUtil;
import com.auth.common.core.constants.BatchSizes;
import com.auth.common.core.model.form.IdsEnableStatusForm;
import com.auth.common.core.utils.FieldChangeSupport;
import com.auth.common.core.utils.TreeParentIdUtil;
import com.auth.common.core.utils.TreeRelationUtil;
import com.auth.common.data.support.BusinessKeyAssert;
import com.auth.service.system.admin.convert.admin.SysDeptConverter;
import com.auth.service.system.admin.mapper.admin.dept.SysDeptMapper;
import com.auth.service.system.admin.model.entity.SysDeptEntity;
import com.auth.service.system.admin.model.form.dept.SysDeptForm;
import com.auth.service.system.admin.model.form.dept.SysDeptMoveForm;
import com.auth.service.system.admin.model.po.dept.DeptClosureNodePO;
import com.auth.service.system.admin.service.admin.SysDeptService;
import com.auth.service.system.admin.support.dept.DeptClosureMaintainer;
import com.auth.service.system.admin.support.dept.DeptReferenceChecker;
import com.auth.service.system.authorization.dispatch.trigger.DeptAuthorizationInvalidationTrigger;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.common.exception.code.SystemCommonResultCode;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

/**
 * 部门写服务实现
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class SysDeptServiceImpl extends ServiceImpl<SysDeptMapper, SysDeptEntity> implements SysDeptService {

	private final DeptReferenceChecker deptReferenceChecker;

	private final DeptClosureMaintainer deptClosureMaintainer;

	private final DeptAuthorizationInvalidationTrigger deptInvalidationTrigger;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void createBatchFromImport(List<SysDeptForm> forms) {
		if (CollUtil.isEmpty(forms)) {
			return;
		}

		List<String> deptCodes = forms.stream().map(SysDeptForm::getDeptCode).toList();
		requireAbsentDeptCodes(deptCodes);

		List<SysDeptEntity> entities = forms.stream().map(form -> {
			Long parentId = normalizeAndRequireExistingParent(form.getParentId());
			SysDeptEntity entity = SysDeptConverter.INSTANCE.toEntity(form);
			entity.setParentId(parentId);
			return entity;
		}).toList();
		super.saveBatch(entities, BatchSizes.SIZE_500);

		List<DeptClosureNodePO> deptClosureNodes = entities.stream()
			.map(entity -> new DeptClosureNodePO(entity.getId(), entity.getParentId()))
			.toList();
		deptClosureMaintainer.insertNodes(deptClosureNodes);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void updateMeta(SysDeptForm form) {
		Long id = form.getId();
		SysDeptEntity existing = deptReferenceChecker.getExistingActive(id);

		String newDeptCode = form.getDeptCode();
		String rawDeptCode = existing.getDeptCode();
		boolean codeChanged = FieldChangeSupport.codeChanged(rawDeptCode, newDeptCode);
		if (codeChanged) {
			BusinessKeyAssert.requireAbsent(baseMapper,
					Wrappers.<SysDeptEntity>lambdaQuery()
						.eq(SysDeptEntity::getDeptCode, newDeptCode)
						.ne(SysDeptEntity::getId, id),
					() -> new SystemBusinessException(SystemCommonResultCode.DATA_CODE_DUPLICATE, newDeptCode));
		}

		long newParentId = normalizeAndRequireExistingParent(form.getParentId());
		boolean parentChanged = TreeParentIdUtil.normalize(existing.getParentId()) != newParentId;
		boolean statusChanged = FieldChangeSupport.valueChanged(existing.getStatus(), form.getStatus());
		if (parentChanged) {
			TreeRelationUtil.requireValidMoveTarget(id, newParentId,
					(ancestorId, nodeId) -> baseMapper.countDescendantRelation(ancestorId, nodeId) > 0);
			existing.setParentId(newParentId);
		}

		SysDeptConverter.INSTANCE.applyUpdateForm(form, existing);
		updateById(existing);

		if (parentChanged) {
			deptClosureMaintainer.moveNode(id, newParentId);
		}

		if (FieldChangeSupport.anyChanged(parentChanged, statusChanged)) {
			String invalidationOp = parentChanged ? "move" : "update";
			deptInvalidationTrigger.submitByDeptIds(List.of(id), invalidationOp);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void batchUpdateStatus(IdsEnableStatusForm form) {
		List<Long> ids = form.getIds().stream().filter(Objects::nonNull).distinct().toList();
		if (CollUtil.isEmpty(ids)) {
			return;
		}

		Boolean status = form.getStatus();
		List<SysDeptEntity> updates = ids.stream().map(id -> {
			SysDeptEntity entity = new SysDeptEntity();
			entity.setId(id);
			entity.setStatus(status);
			return entity;
		}).toList();
		super.updateBatchById(updates);
		deptInvalidationTrigger.submitByDeptIds(ids, "update");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void move(SysDeptMoveForm form) {
		Long deptId = form.getId();
		SysDeptEntity existing = deptReferenceChecker.getExistingActive(deptId);
		long newParentId = normalizeAndRequireExistingParent(form.getParentId());

		if (TreeParentIdUtil.normalize(existing.getParentId()) == newParentId) {
			return;
		}

		TreeRelationUtil.requireValidMoveTarget(deptId, newParentId,
				(ancestorId, nodeId) -> baseMapper.countDescendantRelation(ancestorId, nodeId) > 0);
		existing.setParentId(newParentId);
		updateById(existing);

		deptClosureMaintainer.moveNode(deptId, newParentId);
		deptInvalidationTrigger.submitByDeptIds(List.of(deptId), "move");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void deleteById(Long deptId) {
		deptReferenceChecker.getExistingActive(deptId);

		if (baseMapper.countActiveDirectChildren(deptId) > 0) {
			log.warn("Tree has active children: id={}", deptId);
			throw new SystemBusinessException(SystemCommonResultCode.TREE_HAS_ACTIVE_CHILDREN);
		}
		if (baseMapper.existsSubtreeReference(deptId)) {
			log.warn("department in use: id={}", deptId);
			throw new SystemBusinessException(SystemCommonResultCode.DATA_IN_USE);
		}
		deptInvalidationTrigger.submitByDeptIds(List.of(deptId), "delete");
		deptClosureMaintainer.removeClosurePaths(deptId);

		removeById(deptId);
	}

	/**
	 * 规范化父部门 ID 并校验父部门存在（顶级跳过校验；含停用节点）
	 * @param rawParentId 原始父部门 ID
	 * @return 规范化后的父部门 ID
	 */
	private long normalizeAndRequireExistingParent(Long rawParentId) {
		long parentId = TreeParentIdUtil.normalize(rawParentId);
		if (parentId > TreeParentIdUtil.ROOT_PARENT_ID && baseMapper.countById(parentId) == 0) {
			log.warn("Tree parent unavailable: parentId={}", parentId);
			throw new SystemBusinessException(SystemCommonResultCode.TREE_PARENT_UNAVAILABLE);
		}
		return parentId;
	}

	/**
	 * 批量写入前校验部门编码在请求内及库内均唯一
	 * @param deptCodes 待写入部门编码
	 */
	private void requireAbsentDeptCodes(List<String> deptCodes) {
		List<String> distinctCodes = BusinessKeyAssert.requireDistinct(deptCodes,
				code -> new SystemBusinessException(SystemCommonResultCode.DATA_CODE_DUPLICATE, code));
		if (CollUtil.isEmpty(distinctCodes)) {
			return;
		}

		List<SysDeptEntity> existing = baseMapper.selectActiveByDeptCodes(distinctCodes);
		if (CollUtil.isNotEmpty(existing)) {
			throw new SystemBusinessException(SystemCommonResultCode.DATA_CODE_DUPLICATE,
					existing.get(0).getDeptCode());
		}
	}

}
