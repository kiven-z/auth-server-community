package com.auth.service.system.admin.exception;

import com.auth.service.system.common.exception.code.SystemResultCode;
import lombok.Getter;

/**
 * 系统管理域结果码（岗位、用户部门等 Admin 特有场景）
 *
 * <p>
 * 号段：268、276、315–325、340–341
 * </p>
 *
 * @author Bunny
 */
@Getter
public enum SystemAdminResultCode implements SystemResultCode {

	/**
	 * 同一部门下岗位编码已存在
	 */
	POST_CODE_DUPLICATE_IN_DEPT(409, 276, "POST_CODE_DUPLICATE_IN_DEPT", "system.post.code_duplicate_in_dept"),

	/**
	 * 目标用户不允许执行该写操作（内置超级管理员或当前登录账号）
	 */
	USER_OPERATION_FORBIDDEN(409, 315, "USER_OPERATION_FORBIDDEN", "system.user.operation_forbidden"),

	/**
	 * 用户状态码非法
	 */
	USER_STATUS_INVALID(400, 319, "USER_STATUS_INVALID", "system.user.status_invalid"),

	/**
	 * 两次输入的密码不一致
	 */
	PASSWORD_CONFIRM_MISMATCH(422, 320, "PASSWORD_CONFIRM_MISMATCH", "system.user.password_confirm_mismatch"),

	/**
	 * 旧密码不正确
	 */
	OLD_PASSWORD_INCORRECT(422, 321, "OLD_PASSWORD_INCORRECT", "system.user.old_password_incorrect"),

	/**
	 * 用户部门关联已存在
	 */
	USER_DEPT_DUPLICATE(409, 322, "USER_DEPT_DUPLICATE", "system.user.org_relation_duplicate"),

	/**
	 * 用户岗位关联已存在
	 */
	USER_POST_DUPLICATE(409, 325, "USER_POST_DUPLICATE", "system.user.org_relation_duplicate"),

	/**
	 * Excel 导入文件格式或读取失败
	 */
	IMPORT_FILE_INVALID(422, 340, "IMPORT_FILE_INVALID", "system.import.file_invalid"),

	/**
	 * Excel 导入存在行级数据错误（部分行未通过校验）
	 */
	IMPORT_ROW_VALIDATION_FAILED(422, 341, "IMPORT_ROW_VALIDATION_FAILED", "system.import.row_validation_failed");

	private final int httpStatus;

	private final int code;

	private final String error;

	private final String messageKey;

	SystemAdminResultCode(int httpStatus, int code, String error, String messageKey) {
		this.httpStatus = httpStatus;
		this.code = code;
		this.error = error;
		this.messageKey = messageKey;
	}

}
