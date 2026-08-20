package com.auth.service.system.admin.service.admin.impl;

import com.auth.service.system.admin.convert.admin.user.SysUserScopeConverter;
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
	public SysUserScopeVO getByUserId(Long userId) {
		userReferenceChecker.getExistingActive(userId);
		UserScopeEntity entity = super.getOne(
				Wrappers.<UserScopeEntity>lambdaQuery().eq(UserScopeEntity::getUserId, userId));
		return SysUserScopeConverter.toVo(entity);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void upsert(Long userId, SysDataScopeForm form) {
		userReferenceChecker.getExistingActive(userId);
		DataScopeFormSupport.ResolvedDataScope resolved = dataScopeFormSupport.resolve(form);

		UserScopeEntity existing = super.getOne(
				Wrappers.<UserScopeEntity>lambdaQuery().eq(UserScopeEntity::getUserId, userId));
		UserScopeEntity entity = existing != null ? existing : new UserScopeEntity();
		if (existing == null) {
			entity.setUserId(userId);
		}
		SysUserScopeConverter.applyForm(entity, form, resolved.scopeType().name(), resolved.scopeDeptIds());
		super.saveOrUpdate(entity);

		userInvalidationTrigger.submitByUserIds(List.of(userId), "upsert-user-scope");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void clearByUserId(Long userId) {
		userReferenceChecker.getExistingActive(userId);
		UserScopeEntity existing = super.getOne(
				Wrappers.<UserScopeEntity>lambdaQuery().eq(UserScopeEntity::getUserId, userId));
		if (existing == null) {
			return;
		}
		super.removeById(existing.getId());
		userInvalidationTrigger.submitByUserIds(List.of(userId), "clear-user-scope");
	}

}
