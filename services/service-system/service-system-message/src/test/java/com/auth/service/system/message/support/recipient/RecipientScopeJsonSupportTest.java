package com.auth.service.system.message.support.recipient;

import com.auth.service.system.message.model.enums.RecipientScopeType;
import com.auth.service.system.message.model.value.recipient.RecipientScope;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link RecipientScopeJsonSupport} 单元测试
 *
 * @author Bunny
 */
@DisplayName("RecipientScopeJsonSupport 范围 JSON")
class RecipientScopeJsonSupportTest {

	@Test
	@DisplayName("ALL：返回 null")
	void toJson_all_shouldBeNull() {
		assertThat(RecipientScopeJsonSupport.toJson(RecipientScope.builder().type(RecipientScopeType.ALL).build()))
			.isNull();
	}

	@Test
	@DisplayName("DEPT：默认 includeChildren=true")
	void toJson_dept_shouldIncludeChildrenDefault() {
		String json = RecipientScopeJsonSupport
			.toJson(RecipientScope.builder().type(RecipientScopeType.DEPT).ids(List.of(1L, 2L)).build());
		assertThat(json).contains("\"ids\":[1,2]").contains("\"includeChildren\":true");
	}

	@Test
	@DisplayName("USER：仅 ids")
	void toJson_user_shouldOnlyIds() {
		String json = RecipientScopeJsonSupport
			.toJson(RecipientScope.builder().type(RecipientScopeType.USER).ids(List.of(9L)).build());
		assertThat(json).contains("\"ids\":[9]").doesNotContain("includeChildren");
	}

	@Test
	@DisplayName("parseIds：从快照取回 ids")
	void parseIds_shouldReadIds() {
		// 与 toJson 对称：写入后能读回范围 ID
		String json = RecipientScopeJsonSupport
			.toJson(RecipientScope.builder().type(RecipientScopeType.USER).ids(List.of(9L, 10L)).build());
		assertThat(RecipientScopeJsonSupport.parseIds(json)).containsExactly(9L, 10L);
	}

	@Test
	@DisplayName("fromJson：ALL 忽略 json")
	void fromJson_all_shouldIgnoreJson() {
		RecipientScope scope = RecipientScopeJsonSupport.fromJson(RecipientScopeType.ALL, "{\"ids\":[1]}");
		assertThat(scope.getType()).isEqualTo(RecipientScopeType.ALL);
		assertThat(scope.safeIds()).isEmpty();
	}

	@Test
	@DisplayName("fromJson：DEPT 还原 ids 与 includeChildren")
	void fromJson_dept_shouldRestoreFlag() {
		String json = RecipientScopeJsonSupport.toJson(
				RecipientScope.builder().type(RecipientScopeType.DEPT).ids(List.of(3L)).includeChildren(false).build());
		RecipientScope scope = RecipientScopeJsonSupport.fromJson(RecipientScopeType.DEPT, json);
		assertThat(scope.safeIds()).containsExactly(3L);
		assertThat(scope.includeChildrenOrDefault()).isFalse();
	}

	@Test
	@DisplayName("parseIncludeChildren：空 JSON 返回 null")
	void parseIncludeChildren_blank_shouldBeNull() {
		// 无快照时前端详情不展示 includeChildren
		assertThat(RecipientScopeJsonSupport.parseIncludeChildren(null)).isNull();
		assertThat(RecipientScopeJsonSupport.parseIncludeChildren("  ")).isNull();
	}

	@Test
	@DisplayName("parseIncludeChildren：字段缺失或非布尔返回 null")
	void parseIncludeChildren_invalid_shouldBeNull() {
		// 仅 ids 的 USER 快照、非法类型均视为未指定
		assertThat(RecipientScopeJsonSupport.parseIncludeChildren("{\"ids\":[1]}")).isNull();
		assertThat(RecipientScopeJsonSupport.parseIncludeChildren("{\"includeChildren\":\"yes\"}")).isNull();
	}

	@Test
	@DisplayName("parseIncludeChildren：合法布尔按值返回")
	void parseIncludeChildren_boolean_shouldReturnValue() {
		assertThat(RecipientScopeJsonSupport.parseIncludeChildren("{\"includeChildren\":true}")).isTrue();
		assertThat(RecipientScopeJsonSupport.parseIncludeChildren("{\"includeChildren\":false}")).isFalse();
	}

}
