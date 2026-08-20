package com.auth.module.security.starter.service;

import com.auth.module.security.core.matcher.PermissionMatcher;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PermissionMatcherTest {

	@Test
	@DisplayName("测试全局通配符应匹配任何权限")
	void globalWildcard_shouldMatchAnything() {
		assertTrue(PermissionMatcher.matches("*", "sys:user:add"));
		assertTrue(PermissionMatcher.matches("*:*", "sys:user:add"));
		assertTrue(PermissionMatcher.matches("*:*:*", "sys:user:add"));
		assertTrue(PermissionMatcher.matches("*:*:*:*", "sys:file:recycle:query"));
		assertTrue(PermissionMatcher.matches("sys:user:add", "sys:user:add"));
		assertFalse(PermissionMatcher.matches("sys:user:add", "sys:user:del"));
		assertTrue(PermissionMatcher.matches("sys:*:*", "sys:user:add"));
		assertTrue(PermissionMatcher.matches("sys:user:*", "sys:user:add"));
		assertTrue(PermissionMatcher.matches("SYS:USER:*", "sys:user:add"));
		assertFalse(PermissionMatcher.matches("sys:user:*", "sys:role:add"));
	}

}
