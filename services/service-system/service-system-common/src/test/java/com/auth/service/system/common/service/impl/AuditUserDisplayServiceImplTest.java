package com.auth.service.system.common.service.impl;

import com.auth.common.core.model.response.BaseResponse;
import com.auth.module.platform.persistence.model.UserEntity;
import com.auth.service.system.common.mapper.AuditUserDisplayMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

/**
 * {@link AuditUserDisplayServiceImpl} 单元测试。
 *
 * @author Bunny
 */
@DisplayName("AuditUserDisplayServiceImpl 审计用户展示名")
@ExtendWith(MockitoExtension.class)
class AuditUserDisplayServiceImplTest {

	@Mock
	private AuditUserDisplayMapper auditUserDisplayMapper;

	@InjectMocks
	private AuditUserDisplayServiceImpl auditUserDisplayService;

	private static UserEntity userEntity(Long id, String username) {
		UserEntity entity = new UserEntity();
		entity.setId(id);
		entity.setUsername(username);
		return entity;
	}

	@Test
	@DisplayName("mapUsernamesByIds：空集合与 null 不访问数据库并返回空 Map")
	void mapUsernamesByIdsReturnsEmptyWithoutDbWhenNoIds() {
		// ── null ─────────────────────────────────────────────────
		assertThat(auditUserDisplayService.mapUsernamesByIds(null)).isEmpty();
		verifyNoInteractions(auditUserDisplayMapper);

		// ── empty collection ────────────────────────────────────
		assertThat(auditUserDisplayService.mapUsernamesByIds(Collections.emptyList())).isEmpty();
		verifyNoInteractions(auditUserDisplayMapper);
	}

	@Test
	@DisplayName("mapUsernamesByIds：过滤 null 主键并去重后批量查询")
	void mapUsernamesByIdsFiltersNullsAndDedupes() {
		UserEntity u1 = new UserEntity();
		u1.setId(1L);
		u1.setUsername("alice");
		UserEntity u2 = new UserEntity();
		u2.setId(2L);
		u2.setUsername("bob");

		when(auditUserDisplayMapper.selectByIds(List.of(1L, 2L))).thenReturn(List.of(u1, u2));

		Map<Long, String> result = auditUserDisplayService.mapUsernamesByIds(Arrays.asList(1L, null, 1L, 2L));

		assertThat(result).containsExactlyInAnyOrderEntriesOf(Map.of(1L, "alice", 2L, "bob"));
	}

	@Test
	@DisplayName("mapUsernamesByIds：全部为 null 主键时不访问数据库")
	void mapUsernamesByIdsAllNullIdsSkipsDb() {
		assertThat(auditUserDisplayService.mapUsernamesByIds(Arrays.asList(null, null))).isEmpty();
		verifyNoInteractions(auditUserDisplayMapper);
	}

	@Test
	@DisplayName("enrichAuditUsernames：为分页行填充创建人、更新人用户名")
	void enrichAuditUsernamesSetsUsernamesOnRecords() {
		BaseResponse row = new BaseResponse();
		row.setCreatedBy(1L);
		row.setUpdatedBy(2L);
		Page<BaseResponse> page = new Page<>();
		page.setRecords(List.of(row));

		UserEntity u1 = new UserEntity();
		u1.setId(1L);
		u1.setUsername("alice");
		UserEntity u2 = new UserEntity();
		u2.setId(2L);
		u2.setUsername("bob");
		when(auditUserDisplayMapper.selectByIds(List.of(1L, 2L))).thenReturn(List.of(u1, u2));

		auditUserDisplayService.enrichAuditUsernames(page, null, null);

		assertThat(row.getCreatedByName()).isEqualTo("alice");
		assertThat(row.getUpdatedByName()).isEqualTo("bob");
	}

	@Test
	@DisplayName("enrichAuditUsernames(List)：非分页列表同样填充用户名")
	void enrichAuditUsernamesListSetsUsernamesOnRecords() {
		BaseResponse row = new BaseResponse();
		row.setCreatedBy(1L);
		row.setUpdatedBy(2L);
		List<BaseResponse> list = List.of(row);

		UserEntity u1 = new UserEntity();
		u1.setId(1L);
		u1.setUsername("alice");
		UserEntity u2 = new UserEntity();
		u2.setId(2L);
		u2.setUsername("bob");
		when(auditUserDisplayMapper.selectByIds(List.of(1L, 2L))).thenReturn(List.of(u1, u2));

		auditUserDisplayService.enrichAuditUsernames(list, null, null);

		assertThat(row.getCreatedByName()).isEqualTo("alice");
		assertThat(row.getUpdatedByName()).isEqualTo("bob");
	}

	@Test
	@DisplayName("enrichAuditUsernames(IPage)：page 为 null 时不访问数据库")
	void enrichAuditUsernamesPageNullSkipsDb() {
		auditUserDisplayService.enrichAuditUsernames((Page<BaseResponse>) null, null, null);
		verifyNoInteractions(auditUserDisplayMapper);
	}

	@Test
	@DisplayName("enrichAuditUsernames(IPage)：当前页无记录时不访问用户表")
	void enrichAuditUsernamesPageEmptyRecordsSkipsDb() {
		Page<BaseResponse> page = new Page<>();
		page.setRecords(Collections.emptyList());

		auditUserDisplayService.enrichAuditUsernames(page, null, null);

		verifyNoInteractions(auditUserDisplayMapper);
	}

	@Test
	@DisplayName("enrichAuditUsernames：审计与额外用户 ID 合并为一次批量查询")
	void enrichAuditUsernamesMergesAuditAndExtraIdsIntoSingleDbCall() {
		RowWithOperator row = new RowWithOperator();
		row.setCreatedBy(1L);
		row.setUpdatedBy(2L);
		row.setOperatorId(3L);

		UserEntity u1 = userEntity(1L, "alice");
		UserEntity u2 = userEntity(2L, "bob");
		UserEntity u3 = userEntity(3L, "carol");
		when(auditUserDisplayMapper.selectByIds(List.of(1L, 2L, 3L))).thenReturn(List.of(u1, u2, u3));

		auditUserDisplayService.enrichAuditUsernames(List.of(row),
				r -> Objects.requireNonNullElse(r.getOperatorId(), 0L), RowWithOperator::setOperatorUsername);

		assertThat(row.getCreatedByName()).isEqualTo("alice");
		assertThat(row.getUpdatedByName()).isEqualTo("bob");
		assertThat(row.getOperatorUsername()).isEqualTo("carol");
		verify(auditUserDisplayMapper).selectByIds(List.of(1L, 2L, 3L));
	}

	@Test
	@DisplayName("enrichAuditUsernames：额外用户 ID 与 createdBy 相同时只查询一次该 ID")
	void enrichAuditUsernamesDedupesWhenExtraUserIdEqualsCreatedBy() {
		RowWithOperator row = new RowWithOperator();
		row.setCreatedBy(1L);
		row.setUpdatedBy(2L);
		row.setOperatorId(1L);

		UserEntity u1 = userEntity(1L, "alice");
		UserEntity u2 = userEntity(2L, "bob");
		when(auditUserDisplayMapper.selectByIds(anyList())).thenReturn(List.of(u1, u2));

		auditUserDisplayService.enrichAuditUsernames(List.of(row),
				r -> Objects.requireNonNullElse(r.getOperatorId(), 0L), RowWithOperator::setOperatorUsername);

		assertThat(row.getCreatedByName()).isEqualTo("alice");
		assertThat(row.getUpdatedByName()).isEqualTo("bob");
		assertThat(row.getOperatorUsername()).isEqualTo("alice");
		verify(auditUserDisplayMapper).selectByIds(List.of(1L, 2L));
	}

	@Getter
	@Setter
	static class RowWithOperator extends BaseResponse {

		private Long operatorId;

		private String operatorUsername;

	}

}
