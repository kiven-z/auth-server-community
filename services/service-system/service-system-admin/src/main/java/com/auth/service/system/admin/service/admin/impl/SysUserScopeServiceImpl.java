package com.auth.service.system.admin.service.admin.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.json.JSONUtil;
import com.auth.common.core.constants.BatchSizes;
import com.auth.service.system.admin.mapper.admin.user.UserScopeMapper;
import com.auth.service.system.admin.model.entity.UserScopeEntity;
import com.auth.service.system.admin.model.form.scope.SysDataScopeForm;
import com.auth.service.system.admin.model.vo.user.SysUserScopeVO;
import com.auth.service.system.admin.service.admin.SysUserScopeService;
import com.auth.service.system.admin.support.scope.DataScopeFormSupport;
import com.auth.service.system.admin.support.user.UserReferenceChecker;
import com.auth.service.system.authorization.dispatch.trigger.UserAuthorizationInvalidationTrigger;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 用户数据范围服务实现
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Slf4j
@Service
public class SysUserScopeServiceImpl extends ServiceImpl<UserScopeMapper, UserScopeEntity>
		implements SysUserScopeService {

	private final UserReferenceChecker userReferenceChecker;

	private final DataScopeFormSupport dataScopeFormSupport;

	private final UserAuthorizationInvalidationTrigger userInvalidationTrigger;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<SysUserScopeVO> getByUserId(Long userId) {
		userReferenceChecker.getExistingActive(userId);
		return super.list(Wrappers.<UserScopeEntity>lambdaQuery().eq(UserScopeEntity::getUserId, userId)).stream()
			.map(entity -> {
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
			})
			.toList();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void replace(Long userId, List<SysDataScopeForm> forms) {
		userReferenceChecker.getExistingActive(userId);
		List<DataScopeFormSupport.ResolvedDataScope> resolved = dataScopeFormSupport.resolveAll(forms);

		int removed = baseMapper.delete(Wrappers.<UserScopeEntity>lambdaQuery().eq(UserScopeEntity::getUserId, userId));
		if (!resolved.isEmpty()) {
			List<UserScopeEntity> entities = resolved.stream().map(item -> {
				UserScopeEntity entity = new UserScopeEntity();
				entity.setUserId(userId);
				entity.setDimension(item.dimension().name());
				entity.setScopeType(item.scopeType().name());
				entity.setScopeIds(CollUtil.isEmpty(item.scopeIds()) ? null : JSONUtil.toJsonStr(item.scopeIds()));
				entity.setRemark(item.remark());
				return entity;
			}).toList();
			super.saveBatch(entities, BatchSizes.SIZE_500);
		}
		if (removed > 0 || !resolved.isEmpty()) {
			userInvalidationTrigger.submitByUserIds(List.of(userId), "replace-user-scope");
		}
	}

}
