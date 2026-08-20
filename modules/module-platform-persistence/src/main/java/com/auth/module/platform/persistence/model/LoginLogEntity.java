package com.auth.module.platform.persistence.model;

import com.auth.common.data.model.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.time.Instant;

/**
 * 登录日志
 *
 * @author Bunny
 */
@TableName("log_login")
@Getter
@Setter
@Accessors(chain = true)
public class LoginLogEntity extends BaseEntity {

	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * 用户 ID
	 */
	private Long userId;

	/**
	 * 登录账号（冗余，便于查询）
	 */
	private String username;

	/**
	 * 结果码：0=成功，1=凭证错误，2=账号锁定，3=验证码错误，4=账号禁用
	 */
	private Integer loginResult;

	/**
	 * 失败原因详情
	 */
	private String failureReason;

	/**
	 * 登录时间
	 */
	private Instant loginTime;

	/**
	 * 登录 IP
	 */
	private String loginIp;

	/**
	 * 登录地区（由 IP 解析）
	 */
	private String loginRegion;

	/**
	 * 浏览器/设备 User-Agent
	 */
	private String userAgent;

	/**
	 * 设备类型：PC / MOBILE / TABLET
	 */
	private String deviceType;

	/**
	 * 操作系统
	 */
	private String osType;

	/**
	 * 浏览器类型
	 */
	private String browserType;

	/**
	 * 事件类型：LOGIN_PASSWORD / LOGIN_EMAIL / LOGIN_SMS / REFRESH_TOKEN / LOGOUT
	 */
	private String loginType;

	/**
	 * 登录成功后的会话 ID
	 */
	private String sessionId;

}
