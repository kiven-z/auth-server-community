package com.auth.service.system.admin.config;

import com.auth.module.security.contract.api.granttable.GrantTableSubjectType;
import com.auth.service.system.admin.support.dept.DeptReferenceChecker;
import com.auth.service.system.admin.support.grant.GrantTableActiveSubjectChecker;
import com.auth.service.system.admin.support.grant.TypedGrantTableActiveSubjectChecker;
import com.auth.service.system.admin.support.post.PostReferenceChecker;
import com.auth.service.system.admin.support.user.UserReferenceChecker;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * grant_table 授权主体校验器装配：按 {@link GrantTableSubjectType} 注册
 * {@link GrantTableActiveSubjectChecker} Bean（USER 存在性；DEPT/POST 计算有效）
 *
 * @author Bunny
 */
@Configuration
public class GrantTableSubjectCheckerConfiguration {

	/**
	 * 用户主体在 grant_table 授权前的存在性校验
	 * @param user 用户引用校验
	 * @return 用户主体校验器
	 */
	@Bean
	GrantTableActiveSubjectChecker userGrantTableActiveSubjectChecker(UserReferenceChecker user) {
		return new TypedGrantTableActiveSubjectChecker(GrantTableSubjectType.USER, user::getExistingActive);
	}

	/**
	 * 部门主体在 grant_table 授权前的计算有效校验
	 * @param dept 部门引用校验
	 * @return 部门主体校验器
	 */
	@Bean
	GrantTableActiveSubjectChecker deptGrantTableActiveSubjectChecker(DeptReferenceChecker dept) {
		return new TypedGrantTableActiveSubjectChecker(GrantTableSubjectType.DEPT, dept::requireEffective);
	}

	/**
	 * 岗位主体在 grant_table 授权前的计算有效校验
	 * @param post 岗位引用校验
	 * @return 岗位主体校验器
	 */
	@Bean
	GrantTableActiveSubjectChecker postGrantTableActiveSubjectChecker(PostReferenceChecker post) {
		return new TypedGrantTableActiveSubjectChecker(GrantTableSubjectType.POST, post::requireEffective);
	}

}
