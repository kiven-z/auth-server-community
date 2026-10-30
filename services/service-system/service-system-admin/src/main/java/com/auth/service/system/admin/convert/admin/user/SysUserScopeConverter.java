package com.auth.service.system.admin.convert.admin.user;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.json.JSONUtil;
import com.auth.service.system.admin.model.entity.UserScopeEntity;
import com.auth.service.system.admin.model.form.scope.SysDataScopeForm;
import com.auth.service.system.admin.model.vo.user.SysUserScopeVO;
import lombok.experimental.UtilityClass;

import java.util.List;

/**
 * 用户数据范围实体
 *
 * @author Bunny
 */
@UtilityClass
public class SysUserScopeConverter {

	/**
	 * 实体转回显 VO；null 原样返回
	 * @param entity 用户范围实体
	 * @return 回显 VO
	 */
	public static SysUserScopeVO toVo(UserScopeEntity entity) {
		if (entity == null) {
			return null;
		}

		String scopeIdsRaw = entity.getScopeIds();
		List<Long> scopeIds = CharSequenceUtil.isBlank(scopeIdsRaw) ? List.of()
				: JSONUtil.toList(scopeIdsRaw, Long.class);

		SysUserScopeVO vo = new SysUserScopeVO();
		vo.setId(entity.getId());
		vo.setUserId(entity.getUserId());
		vo.setDimension(entity.getDimension());
		vo.setScopeType(entity.getScopeType());
		vo.setScopeIds(scopeIds);
		vo.setRemark(entity.getRemark());
		return vo;
	}

	/**
	 * 将表单字段写入实体（不改 userId）
	 * @param entity 目标实体
	 * @param form 表单
	 * @param dimension 已规范化的数据维度
	 * @param scopeType 已规范化的范围类型
	 * @param scopeIds 已规范化的节点 ID（ALL/SELF 为空）
	 */
	public static void applyForm(UserScopeEntity entity, SysDataScopeForm form, String dimension, String scopeType,
			List<Long> scopeIds) {
		String ids = CollUtil.isEmpty(scopeIds) ? null : JSONUtil.toJsonStr(scopeIds);

		entity.setDimension(dimension);
		entity.setScopeType(scopeType);
		entity.setScopeIds(ids);
		entity.setRemark(form.getRemark());
	}

}
