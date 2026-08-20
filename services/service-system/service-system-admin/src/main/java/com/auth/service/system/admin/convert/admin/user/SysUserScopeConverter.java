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

		String scopeDeptIdsRaw = entity.getScopeDeptIds();
		List<Long> scopeDeptIds = CharSequenceUtil.isBlank(scopeDeptIdsRaw) ? List.of()
				: JSONUtil.toList(scopeDeptIdsRaw, Long.class);

		SysUserScopeVO vo = new SysUserScopeVO();
		vo.setId(entity.getId());
		vo.setUserId(entity.getUserId());
		vo.setScopeType(entity.getScopeType());
		vo.setScopeDeptIds(scopeDeptIds);
		vo.setRemark(entity.getRemark());
		return vo;
	}

	/**
	 * 将表单字段写入实体（不改 userId）
	 * @param entity 目标实体
	 * @param form 表单
	 * @param scopeType 已规范化的范围类型
	 * @param scopeDeptIds 已规范化的部门 ID（ALL/SELF 为空）
	 */
	public static void applyForm(UserScopeEntity entity, SysDataScopeForm form, String scopeType,
			List<Long> scopeDeptIds) {
		String deptIds = CollUtil.isEmpty(scopeDeptIds) ? null : JSONUtil.toJsonStr(scopeDeptIds);

		entity.setScopeType(scopeType);
		entity.setScopeDeptIds(deptIds);
		entity.setRemark(form.getRemark());
	}

}
