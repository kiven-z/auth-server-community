package com.auth.service.system.admin.convert.admin.role;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.json.JSONUtil;
import com.auth.service.system.admin.model.entity.RoleScopeEntity;
import com.auth.service.system.admin.model.form.scope.SysDataScopeForm;
import com.auth.service.system.admin.model.vo.role.SysRoleScopeVO;
import lombok.experimental.UtilityClass;

import java.util.List;

/**
 * 角色数据范围实体 / 表单 / VO 转换
 *
 * @author Bunny
 */
@UtilityClass
public class SysRoleScopeConverter {

	/**
	 * 实体转回显 VO；null 原样返回
	 * @param entity 角色范围实体
	 * @return 回显 VO
	 */
	public static SysRoleScopeVO toVo(RoleScopeEntity entity) {
		if (entity == null) {
			return null;
		}

		String scopeIdsRaw = entity.getScopeIds();
		List<Long> scopeIds = CharSequenceUtil.isBlank(scopeIdsRaw) ? List.of()
				: JSONUtil.toList(scopeIdsRaw, Long.class);

		SysRoleScopeVO vo = new SysRoleScopeVO();
		vo.setId(entity.getId());
		vo.setRoleId(entity.getRoleId());
		vo.setDimension(entity.getDimension());
		vo.setScopeType(entity.getScopeType());
		vo.setScopeIds(scopeIds);
		vo.setRemark(entity.getRemark());
		return vo;
	}

	/**
	 * 将表单字段写入实体（不改 roleId）
	 * @param entity 目标实体
	 * @param form 表单
	 * @param dimension 已规范化的数据维度
	 * @param scopeType 已规范化的范围类型
	 * @param scopeIds 已规范化的节点 ID（ALL/SELF 为空）
	 */
	public static void applyForm(RoleScopeEntity entity, SysDataScopeForm form, String dimension, String scopeType,
			List<Long> scopeIds) {
		String ids = CollUtil.isEmpty(scopeIds) ? null : JSONUtil.toJsonStr(scopeIds);

		entity.setDimension(dimension);
		entity.setScopeType(scopeType);
		entity.setScopeIds(ids);
		entity.setRemark(form.getRemark());
	}

}
