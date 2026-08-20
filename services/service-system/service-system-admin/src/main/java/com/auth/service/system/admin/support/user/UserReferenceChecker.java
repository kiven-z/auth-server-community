package com.auth.service.system.admin.support.user;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import com.auth.common.data.support.BusinessKeyAssert;
import com.auth.module.platform.persistence.model.UserEntity;
import com.auth.module.security.autoconfigure.web.SecurityUserUtils;
import com.auth.module.security.contract.constants.PermissionConstant;
import com.auth.service.system.admin.exception.SystemAdminResultCode;
import com.auth.service.system.admin.mapper.admin.user.SysUserMapper;
import com.auth.service.system.admin.model.po.user.UserBusinessKeyRowPO;
import com.auth.service.system.admin.model.po.user.UserBusinessKeysExisting;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.common.exception.code.SystemCommonResultCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 用户写操作前的存在性、业务键唯一性与受保护账号校验。
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Slf4j
@Component
public class UserReferenceChecker {

	private static final String FIELD_USERNAME = "用户名";

	private static final String FIELD_EMAIL = "邮箱";

	private static final String FIELD_PHONE = "手机号";

	private static final String FIELD_EMPLOYEE_NO = "工号";

	private final SysUserMapper sysUserMapper;

	/**
	 * 加载用户，不存在则抛业务异常
	 * @param userId 用户 ID
	 * @return 用户实体
	 */
	public UserEntity getExistingActive(Long userId) {
		UserEntity existing = sysUserMapper.selectById(userId);
		if (existing == null) {
			log.warn("User not found: userId={}", userId);
			throw new SystemBusinessException(SystemCommonResultCode.USER_NOT_FOUND);
		}
		return existing;
	}

	/**
	 * 批量查询库内已占用的用户业务键（单次 SQL，四类 OR 命中）
	 * @param usernames 待匹配用户名
	 * @param emails 待匹配邮箱
	 * @param phones 待匹配手机号
	 * @param employeeNos 待匹配工号
	 * @param excludeUserId 更新时排除的用户 ID，新增时为 null
	 * @return 与候选集合交集后的已占用键
	 */
	public UserBusinessKeysExisting findExistingBusinessKeys(Set<String> usernames, Set<String> emails,
			Set<String> phones, Set<String> employeeNos, Long excludeUserId) {
		if (ObjectUtil.isAllEmpty(usernames, emails, phones, employeeNos)) {
			return UserBusinessKeysExisting.empty();
		}

		List<UserBusinessKeyRowPO> rows = sysUserMapper.selectRowsByBusinessKeys(usernames, emails, phones, employeeNos,
				excludeUserId);

		return UserBusinessKeysExisting.builder()
			.usernames(rows.stream()
				.map(UserBusinessKeyRowPO::getUsername)
				.filter(Objects::nonNull)
				.filter(usernames::contains)
				.collect(Collectors.toUnmodifiableSet()))
			.emails(rows.stream()
				.map(UserBusinessKeyRowPO::getEmail)
				.filter(Objects::nonNull)
				.filter(emails::contains)
				.collect(Collectors.toUnmodifiableSet()))
			.phones(rows.stream()
				.map(UserBusinessKeyRowPO::getPhone)
				.filter(Objects::nonNull)
				.filter(phones::contains)
				.collect(Collectors.toUnmodifiableSet()))
			.employeeNos(rows.stream()
				.map(UserBusinessKeyRowPO::getEmployeeNo)
				.filter(Objects::nonNull)
				.filter(employeeNos::contains)
				.collect(Collectors.toUnmodifiableSet()))
			.build();
	}

	/**
	 * 批量写入前校验用户名、邮箱、手机号、工号在请求内及库内均唯一；更新时可排除当前用户。
	 * @param usernames 待写入用户名
	 * @param emails 待写入邮箱
	 * @param phones 待写入手机号
	 * @param employeeNos 待写入工号
	 * @param excludeUserId 更新时排除的用户 ID，新增时为 null
	 */
	public void requireAbsentUserBusinessKeys(List<String> usernames, List<String> emails, List<String> phones,
			List<String> employeeNos, Long excludeUserId) {
		List<String> distinctUsernames = BusinessKeyAssert.requireDistinct(usernames,
				value -> new SystemBusinessException(SystemCommonResultCode.PARAM_DUPLICATE, FIELD_USERNAME, value));
		List<String> distinctEmails = BusinessKeyAssert.requireDistinct(emails,
				value -> new SystemBusinessException(SystemCommonResultCode.PARAM_DUPLICATE, FIELD_EMAIL, value));
		List<String> distinctPhones = BusinessKeyAssert.requireDistinct(phones,
				value -> new SystemBusinessException(SystemCommonResultCode.PARAM_DUPLICATE, FIELD_PHONE, value));

		List<String> presentEmployeeNos = CollUtil.defaultIfEmpty(employeeNos, List.of())
			.stream()
			.filter(CharSequenceUtil::isNotBlank)
			.map(String::trim)
			.toList();
		List<String> distinctEmployeeNos = BusinessKeyAssert.requireDistinct(presentEmployeeNos,
				value -> new SystemBusinessException(SystemCommonResultCode.PARAM_DUPLICATE, FIELD_EMPLOYEE_NO, value));

		UserBusinessKeysExisting existing = findExistingBusinessKeys(Set.copyOf(distinctUsernames),
				Set.copyOf(distinctEmails), Set.copyOf(distinctPhones), Set.copyOf(distinctEmployeeNos), excludeUserId);

		Set<String> existingUsername = existing.usernames();
		if (CollUtil.isNotEmpty(existingUsername)) {
			throw new SystemBusinessException(SystemCommonResultCode.PARAM_DUPLICATE, FIELD_USERNAME,
					existingUsername.iterator().next());
		}
		Set<String> existingEmail = existing.emails();
		if (CollUtil.isNotEmpty(existingEmail)) {
			throw new SystemBusinessException(SystemCommonResultCode.PARAM_DUPLICATE, FIELD_EMAIL,
					existingEmail.iterator().next());
		}
		Set<String> existingPhone = existing.phones();
		if (CollUtil.isNotEmpty(existingPhone)) {
			throw new SystemBusinessException(SystemCommonResultCode.PARAM_DUPLICATE, FIELD_PHONE,
					existingPhone.iterator().next());
		}
		Set<String> existingEmployeeNo = existing.employeeNos();
		if (CollUtil.isNotEmpty(existingEmployeeNo)) {
			throw new SystemBusinessException(SystemCommonResultCode.PARAM_DUPLICATE, FIELD_EMPLOYEE_NO,
					existingEmployeeNo.iterator().next());
		}
	}

	/**
	 * 校验目标用户是否允许当前写操作（超级管理员与当前登录用户不可被操作）
	 * @param userIds 待操作用户主键
	 */
	public void requireOperable(Collection<Long> userIds) {
		Long currentUserId = SecurityUserUtils.getUserId();

		for (Long userId : userIds) {
			boolean protectedTarget = PermissionConstant.isSuperAdmin(userId)
					|| (currentUserId != null && currentUserId.equals(userId));

			if (protectedTarget) {
				log.warn("User operation forbidden: userId={}", userId);
				throw new SystemBusinessException(SystemAdminResultCode.USER_OPERATION_FORBIDDEN);
			}
		}
	}

}