package com.auth.module.platform.persistence.model;

import com.auth.common.data.model.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.time.LocalDate;

/**
 * 系统用户 系统用户的实体类对象
 *
 * @author Bunny
 */
@TableName("sys_user")
@Getter
@Setter
@Accessors(chain = true)
public class UserEntity extends BaseEntity {

	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * 用户名
	 */
	private String username;

	/**
	 * 用户昵称
	 */
	private String nickname;

	/**
	 * 邮箱
	 */
	private String email;

	/**
	 * 手机号
	 */
	private String phone;

	/**
	 * 加密后的密码
	 */
	private String password;

	/**
	 * 头像URL
	 */
	private String avatar;

	/**
	 * 状态(0=禁用,1=正常,2=锁定)
	 */
	private Integer status;

	/**
	 * 性别(0=未知,1=男,2=女)
	 */
	private Integer gender;

	/**
	 * 出生日期
	 */
	private LocalDate birthday;

	/**
	 * 年龄
	 */
	private Integer age;

	/**
	 * 工号，企业内部唯一标识
	 */
	private String employeeNo;

	/**
	 * 个人简介
	 */
	private String introduction;

	/**
	 * 权限版本号
	 */
	private Long permVersion;

}
