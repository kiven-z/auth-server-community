package com.auth.service.system.message.service.admin.impl;

import cn.hutool.core.collection.CollUtil;
import com.auth.common.core.model.form.IdsEnableStatusForm;
import com.auth.common.core.utils.TreeParentIdUtil;
import com.auth.service.system.common.service.AuditUserDisplayService;
import com.auth.service.system.message.convert.InAppMessageCategoryConverter;
import com.auth.service.system.message.exception.MessageException;
import com.auth.service.system.message.mapper.InAppMessageCategoryMapper;
import com.auth.service.system.message.model.entity.InAppMessageCategoryEntity;
import com.auth.service.system.message.model.form.inapp.InAppMessageCategoryForm;
import com.auth.service.system.message.model.po.InAppMessageCategoryDetailRowPO;
import com.auth.service.system.message.model.po.InAppMessageCategoryPageRowPO;
import com.auth.service.system.message.model.query.InAppMessageCategoryQuery;
import com.auth.service.system.message.model.vo.inapp.InAppMessageCategoryDetailVO;
import com.auth.service.system.message.model.vo.inapp.InAppMessageCategoryPageVO;
import com.auth.service.system.message.model.vo.inapp.InAppMessageCategoryVO;
import com.auth.service.system.message.service.admin.InAppMessageCategoryService;
import com.auth.service.system.message.support.inapp.InAppMessageCategorySupport;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static com.auth.common.core.utils.TreeParentIdUtil.ROOT_PARENT_ID;
import static com.auth.service.system.common.exception.code.SystemCommonResultCode.*;
import static com.auth.service.system.message.exception.MessageResultCode.IN_APP_MESSAGE_CATEGORY_NOT_FOUND;

/**
 * 站内信业务分类服务实现
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Service
public class InAppMessageCategoryServiceImpl extends ServiceImpl<InAppMessageCategoryMapper, InAppMessageCategoryEntity>
		implements InAppMessageCategoryService {

	private final AuditUserDisplayService auditUserDisplayService;

	private final InAppMessageCategorySupport categorySupport;

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(readOnly = true)
	public List<InAppMessageCategoryPageVO> listCategories(InAppMessageCategoryQuery query) {
		List<InAppMessageCategoryPageRowPO> rows = baseMapper.selectCategoryList(query);
		List<InAppMessageCategoryPageVO> list = InAppMessageCategoryConverter.INSTANCE.toPageVOList(rows);

		auditUserDisplayService.enrichAuditUsernames(list, null, null);
		return list;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(readOnly = true)
	public InAppMessageCategoryDetailVO getCategoryById(Long id) {
		InAppMessageCategoryDetailRowPO row = baseMapper.selectDetailById(id);
		if (row == null) {
			throw new MessageException(DATA_NOT_EXIST);
		}

		InAppMessageCategoryDetailVO vo = InAppMessageCategoryConverter.INSTANCE.toDetailVo(row);
		auditUserDisplayService.enrichAuditUsernames(Collections.singletonList(vo), null, null);
		return vo;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void create(InAppMessageCategoryForm form) {
		categorySupport.requireUniqueCode(form.getCode(), null);
		long parentId = categorySupport.requireValidParentId(form.getParentId());

		InAppMessageCategoryEntity entity = InAppMessageCategoryConverter.INSTANCE.toEntity(form);
		entity.setParentId(parentId);
		super.save(entity);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void update(InAppMessageCategoryForm form) {
		InAppMessageCategoryEntity existing = super.getById(form.getId());
		if (existing == null) {
			throw new MessageException(DATA_NOT_EXIST);
		}

		categorySupport.requireUniqueCode(form.getCode(), form.getId());
		long newParentId = categorySupport.requireValidParentId(form.getParentId());
		if (existing.getId().equals(newParentId)) {
			throw new MessageException(TREE_PARENT_UNAVAILABLE, newParentId);
		}
		// 大类降为小类前须无子节点，否则会出现三级树
		boolean counted = super.count(Wrappers.<InAppMessageCategoryEntity>lambdaQuery()
			.eq(InAppMessageCategoryEntity::getParentId, existing.getId())) > 0;
		if (TreeParentIdUtil.normalize(existing.getParentId()) == ROOT_PARENT_ID && newParentId > ROOT_PARENT_ID
				&& counted) {
			throw new MessageException(TREE_HAS_ACTIVE_CHILDREN);
		}

		InAppMessageCategoryConverter.INSTANCE.applyUpdateForm(form, existing);
		existing.setParentId(newParentId);
		super.updateById(existing);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void batchUpdateStatus(IdsEnableStatusForm form) {
		List<Long> ids = form.getIds();
		if (CollUtil.isEmpty(ids)) {
			return;
		}

		super.update(Wrappers.<InAppMessageCategoryEntity>lambdaUpdate()
			.in(InAppMessageCategoryEntity::getId, ids)
			.set(InAppMessageCategoryEntity::getStatus, form.getStatus()));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void batchDelete(List<Long> ids) {
		if (CollUtil.isEmpty(ids)) {
			return;
		}

		List<Long> idsToDelete = ids.stream().filter(Objects::nonNull).distinct().toList();
		if (CollUtil.isEmpty(idsToDelete)) {
			return;
		}

		long childCount = super.count(Wrappers.<InAppMessageCategoryEntity>lambdaQuery()
			.in(InAppMessageCategoryEntity::getParentId, idsToDelete));
		if (childCount > 0) {
			throw new MessageException(TREE_HAS_ACTIVE_CHILDREN);
		}

		super.removeByIds(idsToDelete);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(readOnly = true)
	public List<InAppMessageCategoryVO> listMajors(Boolean status) {
		List<InAppMessageCategoryEntity> list = baseMapper.selectByParentId(ROOT_PARENT_ID, status);
		return list.stream().map(InAppMessageCategoryConverter.INSTANCE::toFlatVo).toList();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(readOnly = true)
	public List<InAppMessageCategoryVO> listChildren(Long parentId, Boolean status) {
		InAppMessageCategoryEntity parent = super.getById(parentId);
		if (parent == null || TreeParentIdUtil.normalize(parent.getParentId()) != ROOT_PARENT_ID) {
			throw new MessageException(IN_APP_MESSAGE_CATEGORY_NOT_FOUND, parentId);
		}

		List<InAppMessageCategoryEntity> list = baseMapper.selectByParentId(parentId, status);
		return list.stream().map(InAppMessageCategoryConverter.INSTANCE::toFlatVo).toList();
	}

}
