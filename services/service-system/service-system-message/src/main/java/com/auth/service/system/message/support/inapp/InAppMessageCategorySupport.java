package com.auth.service.system.message.support.inapp;

import cn.hutool.core.text.CharSequenceUtil;
import com.auth.common.core.utils.TreeParentIdUtil;
import com.auth.service.system.message.exception.MessageException;
import com.auth.service.system.message.mapper.InAppMessageCategoryMapper;
import com.auth.service.system.message.model.entity.InAppMessageCategoryEntity;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import static com.auth.common.core.utils.TreeParentIdUtil.ROOT_PARENT_ID;
import static com.auth.service.system.common.exception.code.SystemCommonResultCode.*;
import static com.auth.service.system.message.exception.MessageResultCode.IN_APP_MESSAGE_CATEGORY_NOT_FOUND;

/**
 * 站内信业务分类不变式校验
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Component
public class InAppMessageCategorySupport {

	private final InAppMessageCategoryMapper categoryMapper;

	/**
	 * 校验并返回启用中的小类
	 * @param categoryId 小类主键
	 * @return 启用小类实体
	 */
	public InAppMessageCategoryEntity requireEnabledMinor(Long categoryId) {
		if (categoryId == null) {
			throw new MessageException(PARAM_REQUIRED, "业务小类");
		}
		InAppMessageCategoryEntity entity = categoryMapper.selectOne(Wrappers.<InAppMessageCategoryEntity>lambdaQuery()
			.eq(InAppMessageCategoryEntity::getStatus, Boolean.TRUE)
			.gt(InAppMessageCategoryEntity::getParentId, ROOT_PARENT_ID)
			.eq(InAppMessageCategoryEntity::getId, categoryId));
		if (entity == null) {
			throw new MessageException(IN_APP_MESSAGE_CATEGORY_NOT_FOUND, categoryId);
		}
		return entity;
	}

	/**
	 * 校验并返回启用中的小类（按分类码）
	 * @param code 分类码
	 * @return 启用小类实体
	 */
	public InAppMessageCategoryEntity requireEnabledMinorByCode(String code) {
		if (CharSequenceUtil.isBlank(code)) {
			throw new MessageException(PARAM_REQUIRED, "业务小类");
		}
		InAppMessageCategoryEntity entity = categoryMapper.selectOne(Wrappers.<InAppMessageCategoryEntity>lambdaQuery()
			.eq(InAppMessageCategoryEntity::getStatus, Boolean.TRUE)
			.gt(InAppMessageCategoryEntity::getParentId, ROOT_PARENT_ID)
			.eq(InAppMessageCategoryEntity::getCode, code));
		if (entity == null) {
			throw new MessageException(IN_APP_MESSAGE_CATEGORY_NOT_FOUND, code);
		}
		return entity;
	}

	/**
	 * code 全局唯一
	 * @param code 分类码
	 * @param excludeId 排除的主键；新增传 null
	 */
	public void requireUniqueCode(String code, Long excludeId) {
		var wrapper = Wrappers.<InAppMessageCategoryEntity>lambdaQuery().eq(InAppMessageCategoryEntity::getCode, code);
		if (excludeId != null) {
			wrapper.ne(InAppMessageCategoryEntity::getId, excludeId);
		}
		if (categoryMapper.selectCount(wrapper) > 0) {
			throw new MessageException(DATA_CODE_DUPLICATE, code);
		}
	}

	/**
	 * 规范化并校验父级
	 * @param rawParentId 表单父级
	 * @return 规范化后的 parentId
	 */
	public long requireValidParentId(Long rawParentId) {
		long parentId = TreeParentIdUtil.normalize(rawParentId);
		if (parentId > ROOT_PARENT_ID) {
			InAppMessageCategoryEntity parent = categoryMapper.selectById(parentId);
			if (parent == null || TreeParentIdUtil.normalize(parent.getParentId()) != ROOT_PARENT_ID) {
				throw new MessageException(TREE_PARENT_UNAVAILABLE, parentId);
			}
		}
		return parentId;
	}

}
