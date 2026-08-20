package com.auth.service.auth.controller;

import com.auth.common.core.model.response.Result;
import com.auth.module.security.autoconfigure.annotation.PublicApi;
import com.auth.service.auth.model.request.EmailLoginRequest;
import com.auth.service.auth.model.request.SmsLoginRequest;
import com.auth.service.auth.model.request.UsernamePasswordLoginRequest;
import com.auth.service.auth.model.response.RefreshTokenResponse;
import com.auth.service.auth.model.response.UserLoginResponse;
import com.auth.service.auth.model.value.login.command.EmailCommand;
import com.auth.service.auth.model.value.login.command.SmsCommand;
import com.auth.service.auth.model.value.login.command.UsernamePasswordCommand;
import com.auth.service.auth.service.AuthSessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 登录
 *
 * @author Bunny
 */
@Tag(name = "登录", description = "用户名密码、邮箱、短信登录与令牌刷新、退出")
@RequiredArgsConstructor
@RequestMapping("/api/auth")
@RestController
public class AuthController {

	private final AuthSessionService authSessionService;

	@PublicApi
	@Operation(summary = "用户名密码登录")
	@PostMapping("login/username")
	public Result<UserLoginResponse> loginByUsername(@Valid @RequestBody UsernamePasswordLoginRequest request,
			HttpServletRequest servletRequest, HttpServletResponse servletResponse) {
		UsernamePasswordCommand command = new UsernamePasswordCommand();
		command.setUsername(request.getUsername());
		command.setPassword(request.getPassword());
		command.setRememberMe(request.getRememberMe());

		UserLoginResponse data = authSessionService.completeLogin(command, servletRequest, servletResponse);
		return Result.success(data);
	}

	@PublicApi
	@Operation(summary = "邮箱登录")
	@PostMapping("login/email")
	public Result<UserLoginResponse> loginByEmail(@Valid @RequestBody EmailLoginRequest request,
			HttpServletRequest servletRequest, HttpServletResponse servletResponse) {
		EmailCommand command = new EmailCommand();
		command.setEmail(request.getEmail());
		command.setCode(request.getCode());
		command.setRememberMe(request.getRememberMe());

		UserLoginResponse data = authSessionService.completeLogin(command, servletRequest, servletResponse);
		return Result.success(data);
	}

	@PublicApi
	@Operation(summary = "短信登录")
	@PostMapping("login/sms")
	public Result<UserLoginResponse> loginBySms(@Valid @RequestBody SmsLoginRequest request,
			HttpServletRequest servletRequest, HttpServletResponse servletResponse) {
		SmsCommand command = new SmsCommand();
		command.setPhone(request.getPhone());
		command.setCode(request.getCode());
		command.setRememberMe(request.getRememberMe());

		UserLoginResponse data = authSessionService.completeLogin(command, servletRequest, servletResponse);
		return Result.success(data);
	}

	@PublicApi
	@Operation(summary = "刷新令牌")
	@PostMapping("refresh-token")
	public Result<RefreshTokenResponse> refreshToken(HttpServletRequest request, HttpServletResponse response) {
		RefreshTokenResponse data = authSessionService.completeRefresh(request, response);
		return Result.success(data);
	}

	@Operation(summary = "退出登录")
	@PostMapping("logout")
	public Result<Void> logout(HttpServletRequest request, HttpServletResponse response) {
		authSessionService.completeLogout(request, response);

		return Result.success();
	}

}
