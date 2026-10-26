package com.auth.service.system.admin.export.fetcher;

import com.auth.module.security.contract.bizcode.PlatformBizCodes;
import com.auth.service.system.admin.mapper.admin.user.SysUserMapper;
import com.auth.service.system.admin.model.po.user.SysUserPageRowPO;
import com.auth.service.system.admin.model.query.user.SysUserPageQuery;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * {@link SysUserExportPageFetcher} 单元测试
 *
 * @author Bunny
 */
@DisplayName("SysUserExportPageFetcher Map fieldKey 对齐")
@ExtendWith(MockitoExtension.class)
class SysUserExportPageFetcherTest {

	@Mock
	private SysUserMapper sysUserMapper;

	@InjectMocks
	private SysUserExportPageFetcher fetcher;

	@Test
	@DisplayName("bizType / queryType 契约")
	void metadata_matchesUserExportContract() {
		assertThat(fetcher.bizType()).isEqualTo(PlatformBizCodes.SYS_USER);
		assertThat(fetcher.queryType()).isEqualTo(SysUserPageQuery.class);
	}

	@Test
	@DisplayName("fetchPage：SpreadsheetRow 字段名对齐列 fieldKey")
	void fetchPage_mapsSpreadsheetFieldKeys() {
		SysUserPageRowPO po = new SysUserPageRowPO();
		po.setId(9L);
		po.setUsername("alice");
		po.setNickname("爱丽丝");
		po.setPhone("13800000000");
		po.setEmail("alice@example.com");
		po.setEmployeeNo("E001");
		po.setStatus(1);
		po.setCreatedAt(Instant.parse("2024-01-02T03:04:05Z"));
		po.setUpdatedAt(Instant.parse("2024-01-03T03:04:05Z"));

		Page<SysUserPageRowPO> page = new Page<>(1, 10, 1);
		page.setRecords(List.of(po));
		when(sysUserMapper.selectListByPage(any(), any(SysUserPageQuery.class), any())).thenReturn(page);

		IPage<Map<String, Object>> result = fetcher.fetchPage(new SysUserPageQuery(), 1, 10);

		assertThat(result.getTotal()).isEqualTo(1);
		assertThat(result.getRecords()).hasSize(1);
		Map<String, Object> row = result.getRecords().get(0);
		assertThat(row).containsEntry("username", "alice")
			.containsEntry("nickname", "爱丽丝")
			.containsEntry("phone", "13800000000")
			.containsEntry("email", "alice@example.com")
			.containsEntry("employeeNo", "E001")
			.containsEntry("statusLabel", "启用")
			.containsEntry("id", 9L)
			.containsKeys("createdAtText", "updatedAtText")
			.doesNotContainKeys("password", "createdBy", "updatedBy", "status", "avatar");
	}

}
