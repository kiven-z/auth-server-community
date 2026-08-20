package com.auth.service.system.admin.model.po.user;

import lombok.Builder;
import lombok.Value;
import lombok.experimental.Accessors;

import java.util.Set;

/**
 * 库内已占用的用户业务键集合
 *
 * @author Bunny
 */
@Value
@Builder
@Accessors(fluent = true)
public class UserBusinessKeysExisting {

	Set<String> usernames;

	Set<String> emails;

	Set<String> phones;

	Set<String> employeeNos;

	public static UserBusinessKeysExisting empty() {
		return UserBusinessKeysExisting.builder()
			.usernames(Set.of())
			.emails(Set.of())
			.phones(Set.of())
			.employeeNos(Set.of())
			.build();
	}

}