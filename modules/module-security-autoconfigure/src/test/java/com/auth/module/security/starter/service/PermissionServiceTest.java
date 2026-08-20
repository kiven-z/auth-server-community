package com.auth.module.security.starter.service;

import com.auth.module.security.autoconfigure.config.security.AuditPolicy;
import com.auth.module.security.autoconfigure.config.security.SecurityConfigProperties;
import com.auth.module.security.autoconfigure.pipeline.resolver.HandlerMethodResolver;
import com.auth.module.security.autoconfigure.service.PermissionService;
import com.auth.module.security.contract.api.authorization.AuthProfile;
import com.auth.module.security.contract.event.SecurityAuthorizationAuditPayloadEvent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PermissionServiceTest {

	private PermissionService createPermissionService(ApplicationEventPublisher publisher) {
		SecurityConfigProperties configProperties = new SecurityConfigProperties();
		configProperties.setAuditPolicy(AuditPolicy.ALL_RECORD);
		@SuppressWarnings("unchecked")
		ObjectProvider<HandlerMethodResolver> handlerMethodResolver = mock(ObjectProvider.class);
		when(handlerMethodResolver.getIfAvailable()).thenReturn(null);
		return new PermissionService(publisher, configProperties, List.of(), handlerMethodResolver);
	}

	@AfterEach
	void cleanup() {
		SecurityContextHolder.clearContext();
	}

	@SuppressWarnings("null")
	@Test
	@DisplayName("测试超级管理员应绕过权限检查")
	void superAdmin_shouldBypass() {
		ApplicationEventPublisher publisher = mock(ApplicationEventPublisher.class);
		PermissionService service = createPermissionService(publisher);

		AuthProfile profile = AuthProfile.builder()
			.userId(1L)
			.username("u1")
			.roles(List.of())
			.permissions(List.of())
			.build();
		SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(profile, null));

		assertTrue(service.decide("any:perm"));
		var eventCaptor = org.mockito.ArgumentCaptor.forClass(SecurityAuthorizationAuditPayloadEvent.class);
		verify(publisher, atLeastOnce()).publishEvent(eventCaptor.capture());
		assertNotNull(eventCaptor.getValue());
	}

	@Test
	@DisplayName("测试正常权限应匹配")
	void normalPermission_shouldMatch() {
		ApplicationEventPublisher publisher = mock(ApplicationEventPublisher.class);
		PermissionService service = createPermissionService(publisher);

		AuthProfile profile = AuthProfile.builder()
			.userId(2L)
			.username("u2")
			.roles(List.of())
			.permissions(List.of("sys:user:*"))
			.build();
		SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(profile, null));

		assertTrue(service.decide("sys:user:add"));
	}

	@Test
	@DisplayName("测试拒绝时应返回false")
	void denied_shouldReturnFalse() {
		ApplicationEventPublisher publisher = mock(ApplicationEventPublisher.class);
		PermissionService service = createPermissionService(publisher);

		AuthProfile profile = AuthProfile.builder()
			.userId(2L)
			.username("u2")
			.roles(List.of())
			.permissions(List.of("sys:role:add"))
			.build();
		SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(profile, null));

		assertFalse(service.decide("sys:user:add"));
	}

}
