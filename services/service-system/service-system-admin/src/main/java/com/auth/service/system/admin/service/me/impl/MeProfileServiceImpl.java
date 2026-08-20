package com.auth.service.system.admin.service.me.impl;

import com.auth.common.core.utils.FieldChangeSupport;
import com.auth.common.data.support.BusinessKeyAssert;
import com.auth.module.platform.persistence.model.UserEntity;
import com.auth.module.security.autoconfigure.web.SecurityUserUtils;
import com.auth.service.system.admin.convert.admin.ReferenceConverter;
import com.auth.service.system.admin.mapper.admin.user.SysUserMapper;
import com.auth.service.system.admin.mapper.admin.user.UserDeptMapper;
import com.auth.service.system.admin.model.form.me.MeAvatarUpdateForm;
import com.auth.service.system.admin.model.form.me.MeProfileUpdateForm;
import com.auth.service.system.admin.model.po.user.UserDeptProfilePO;
import com.auth.service.system.admin.model.po.user.UserPostProfilePO;
import com.auth.service.system.admin.model.po.user.UserPrimaryDeptPO;
import com.auth.service.system.admin.model.vo.me.MeOrgBindingsVO;
import com.auth.service.system.admin.model.vo.me.MeProfileVO;
import com.auth.service.system.admin.service.me.MeProfileService;
import com.auth.service.system.admin.support.user.UserAvatarUpdateSupport;
import com.auth.service.system.admin.support.user.UserReferenceChecker;
import com.auth.service.system.authorization.dispatch.trigger.UserSessionRevocationTrigger;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.common.exception.code.SystemCommonResultCode;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.Objects;

/**
 * 当前登录用户个人资料服务实现
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Service
@Transactional(rollbackFor = Exception.class)
public class MeProfileServiceImpl extends ServiceImpl<SysUserMapper, UserEntity> implements MeProfileService {

	private final UserReferenceChecker userReferenceChecker;

	private final UserSessionRevocationTrigger userSessionRevocationTrigger;

	private final UserAvatarUpdateSupport userAvatarUpdateSupport;

	private final UserDeptMapper userDeptMapper;

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(readOnly = true)
	public MeProfileVO getMyProfile() {
		Long userId = SecurityUserUtils.getUserId();
		UserEntity user = userReferenceChecker.getExistingActive(userId);
		UserPrimaryDeptPO primaryDept = userDeptMapper.selectPrimaryDeptByUserId(userId);

		MeProfileVO profile = new MeProfileVO();
		profile.setUsername(user.getUsername());
		profile.setNickname(user.getNickname());
		profile.setAvatar(user.getAvatar());

		primaryDept = Objects.requireNonNullElse(primaryDept, new UserPrimaryDeptPO());
		profile.setPrimaryDeptId(primaryDept.getDeptId());
		profile.setPrimaryDeptName(primaryDept.getDeptName());
		return profile;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(readOnly = true)
	public MeOrgBindingsVO getMyOrgBindings() {
		Long userId = SecurityUserUtils.getUserId();
		userReferenceChecker.getExistingActive(userId);

		List<UserDeptProfilePO> deptList = baseMapper.selectDeptProfileByUserId(userId);
		List<UserDeptProfilePO> deptRows = Objects.requireNonNullElse(deptList, List.of());
		List<UserPostProfilePO> postList = baseMapper.selectPostProfileByUserId(userId);
		List<UserPostProfilePO> postRows = Objects.requireNonNullElse(postList, List.of());

		MeOrgBindingsVO bindings = new MeOrgBindingsVO();
		bindings.setDepts(ReferenceConverter.INSTANCE.toUserDeptReferenceList(deptRows));
		bindings.setPosts(ReferenceConverter.INSTANCE.toUserPostReferenceList(postRows));
		return bindings;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void updateMyProfile(MeProfileUpdateForm form) {
		Long userId = SecurityUserUtils.getUserId();
		UserEntity existing = userReferenceChecker.getExistingActive(userId);

		BusinessKeyAssert.requireAbsent(baseMapper,
				Wrappers.<UserEntity>lambdaQuery()
					.eq(UserEntity::getEmail, form.getEmail())
					.ne(UserEntity::getId, userId),
				() -> new SystemBusinessException(SystemCommonResultCode.PARAM_DUPLICATE, "邮箱", form.getEmail()));
		BusinessKeyAssert.requireAbsent(baseMapper,
				Wrappers.<UserEntity>lambdaQuery()
					.eq(UserEntity::getPhone, form.getPhone())
					.ne(UserEntity::getId, userId),
				() -> new SystemBusinessException(SystemCommonResultCode.PARAM_DUPLICATE, "手机号", form.getPhone()));

		boolean revoke = FieldChangeSupport.anyChanged(
				FieldChangeSupport.codeChanged(existing.getEmail(), form.getEmail()),
				FieldChangeSupport.codeChanged(existing.getPhone(), form.getPhone()));
		if (revoke) {
			userSessionRevocationTrigger.revokeAllSessionsAfterCommit(List.of(existing.getId()));
		}

		existing.setNickname(form.getNickname());
		existing.setEmail(form.getEmail());
		existing.setPhone(form.getPhone());
		existing.setGender(form.getGender());
		existing.setBirthday(form.getBirthday());
		existing.setIntroduction(form.getIntroduction());
		if (form.getBirthday() != null) {
			existing.setAge(Period.between(form.getBirthday(), LocalDate.now()).getYears());
		}

		updateById(existing);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void updateMyAvatar(MeAvatarUpdateForm form) {
		String avatar = form.getAvatar();
		Long userId = SecurityUserUtils.getUserId();

		UserEntity userEntity = userReferenceChecker.getExistingActive(userId);
		userAvatarUpdateSupport.applyAvatarUpdate(userEntity, avatar, userId);
	}

}
