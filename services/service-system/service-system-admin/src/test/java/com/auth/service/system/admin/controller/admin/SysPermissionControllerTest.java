package com.auth.service.system.admin.controller.admin;

import com.auth.common.core.model.response.Result;
import com.auth.module.file.api.model.dto.FileExportTaskCreateResult;
import com.auth.module.file.api.model.request.FileExportTaskCreateRequest;
import com.auth.module.file.api.port.FileExportTaskOperations;
import com.auth.module.security.contract.api.AuthProfile;
import com.auth.service.system.admin.excel.permission.SysPermissionSpreadsheetService;
import com.auth.service.system.admin.model.query.permission.SysPermissionQuery;
import com.auth.service.system.admin.service.admin.SysPermissionService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link SysPermissionController} 单元测试
 *
 * @author Bunny
 */
@DisplayName("SysPermissionController 权限导出")
@ExtendWith(MockitoExtension.class)
class SysPermissionControllerTest {

	@Mock
	private SysPermissionService sysPermissionService;

	@Mock
	private SysPermissionSpreadsheetService sysPermissionSpreadsheetService;

	@Mock
	private FileExportTaskOperations fileExportTaskOperations;

	@AfterEach
	void clearSecurityContext() {
		SecurityContextHolder.clearContext();
	}

	@Test
	@DisplayName("exportAsync：按当前用户创建 SYS_PERMISSION 导出任务")
	void exportAsyncCreatesTaskForCurrentUser() {
		SysPermissionController controller = new SysPermissionController(sysPermissionService,
				sysPermissionSpreadsheetService, fileExportTaskOperations);
		AuthProfile profile = AuthProfile.builder().userId(2001L).username("permission_admin").build();
		SecurityContextHolder.getContext()
			.setAuthentication(new UsernamePasswordAuthenticationToken(profile, null, null));

		SysPermissionQuery query = new SysPermissionQuery();
		query.setPermissionCode("sys:file:exporttask:query");
		FileExportTaskCreateResult createResult = new FileExportTaskCreateResult();
		createResult.setTaskId(88001L);
		when(fileExportTaskOperations.createTask(org.mockito.ArgumentMatchers.any(FileExportTaskCreateRequest.class)))
			.thenReturn(createResult);

		Result<FileExportTaskCreateResult> response = controller.exportAsync(query, "req-perm-1");

		assertThat(response.getData()).isNotNull();
		assertThat(response.getData().getTaskId()).isEqualTo(88001L);

		ArgumentCaptor<FileExportTaskCreateRequest> requestCaptor = ArgumentCaptor
			.forClass(FileExportTaskCreateRequest.class);
		verify(fileExportTaskOperations).createTask(requestCaptor.capture());
		FileExportTaskCreateRequest captured = requestCaptor.getValue();
		assertThat(captured.getBizType()).isEqualTo("SYS_PERMISSION");
		assertThat(captured.getExportFormat()).isEqualTo("XLSX");
		assertThat(captured.getRequestId()).isEqualTo("req-perm-1");
		assertThat(captured.getCreatedBy()).isEqualTo(2001L);
		assertThat(captured.getQuerySnapshotJson()).contains("sys:file:exporttask:query");
		assertThat(captured.getColumnSnapshotJson()).contains("permissionCode");
		assertThat(captured.getFetchService()).isEqualTo("service-system");
		assertThat(captured.getFetchPath()).isEqualTo("/api/system/inner/export/page");
	}

}
