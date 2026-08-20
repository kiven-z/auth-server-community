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

		String scopeDeptIdsRaw = entity.getScopeDeptIds();
		List<Long> scopeDeptIds = CharSequenceUtil.isBlank(scopeDeptIdsRaw) ? List.of()
				: JSONUtil.toList(scopeDeptIdsRaw, Long.class);

		SysRoleScopeVO vo = new SysRoleScopeVO();
		vo.setId(entity.getId());
		vo.setRoleId(entity.getRoleId());
		vo.setScopeType(entity.getScopeType());
		vo.setScopeDeptIds(scopeDeptIds);
		vo.setRemark(entity.getRemark());
		return vo;
	}

	/**
	 * 将表单字段写入实体（不改 roleId）
	 * @param entity 目标实体
	 * @param form 表单
	 * @param scopeType 已规范化的范围类型
	 * @param scopeDeptIds 已规范化的部门 ID（ALL/SELF 为空）
	 */
	public static void applyForm(RoleScopeEntity entity, SysDataScopeForm form, String scopeType,
			List<Long> scopeDeptIds) {
		String deptIds = CollUtil.isEmpty(scopeDeptIds) ? null : JSONUtil.toJsonStr(scopeDeptIds);

		entity.setScopeType(scopeType);
		entity.setScopeDeptIds(deptIds);
		entity.setRemark(form.getRemark());
	}

}
