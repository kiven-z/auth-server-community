package com.auth.service.system.admin.excel.permission;

import com.auth.service.system.admin.mapper.admin.permission.SysPermissionMapper;
import com.auth.service.system.admin.service.admin.SysPermissionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link SysPermissionSpreadsheetService} 单元测试
 *
 * @author Bunny
 */
@DisplayName("SysPermissionSpreadsheetService 权限导入")
@ExtendWith(MockitoExtension.class)
class SysPermissionSpreadsheetServiceTest {

	@Mock
	private SysPermissionMapper sysPermissionMapper;

	@Mock
	private SysPermissionService sysPermissionService;

	private SysPermissionSpreadsheetService spreadsheetService;

	@BeforeEach
	void setUp() {
		PermissionSheetImporter permissionSheetImporter = new PermissionSheetImporter(sysPermissionMapper,
				sysPermissionService);
		spreadsheetService = new SysPermissionSpreadsheetService(permissionSheetImporter);
	}

	@Test
	@DisplayName("导入模板下载返回非空 Excel 字节")
	void downloadImportTemplate_returnsNonEmptyExcelBytes() throws IOException {
		ResponseEntity<byte[]> response = spreadsheetService.downloadImportTemplate();

		assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
		assertThat(response.getBody()).isNotNull().isNotEmpty();
	}

}
