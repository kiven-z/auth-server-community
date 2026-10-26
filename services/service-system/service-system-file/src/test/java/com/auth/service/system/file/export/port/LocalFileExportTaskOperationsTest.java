package com.auth.service.system.file.export.port;

import com.auth.module.file.api.model.request.FileExportTaskCreateRequest;
import com.auth.module.security.contract.profile.AuthProfile;
import com.auth.service.system.file.config.properties.FileExportTaskProperties;
import com.auth.service.system.file.export.remote.RemotePageFetcher;
import com.auth.service.system.file.mapper.FileExportTaskMapper;
import com.auth.service.system.file.model.entity.FileExportTaskEntity;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * {@link LocalFileExportTaskOperations} 单元测试
 *
 * @author Bunny
 */
@DisplayName("LocalFileExportTaskOperations 进程内 Port")
@ExtendWith(MockitoExtension.class)
class LocalFileExportTaskOperationsTest {

	@Mock
	private RemotePageFetcher remotePageFetcher;

	@Mock
	private FileExportTaskMapper fileExportTaskMapper;

	@AfterEach
	void clearSecurityContext() {
		SecurityContextHolder.clearContext();
	}

	@Test
	@DisplayName("createTask：命中幂等请求返回已存在任务ID")
	void createTaskReturnsDuplicatedTaskByRequestId() {
		FileExportTaskProperties properties = new FileExportTaskProperties();
		LocalFileExportTaskOperations operations = new LocalFileExportTaskOperations(remotePageFetcher, properties,
				fileExportTaskMapper);

		FileExportTaskCreateRequest request = new FileExportTaskCreateRequest();
		request.setBizType("FILE_RECORD");
		request.setExportFormat("XLSX");
		request.setQuerySnapshotJson("{\"ownerUserId\":1}");
		request.setColumnSnapshotJson("[{\"fieldKey\":\"id\",\"header\":\"主键ID\"}]");
		request.setFetchService("service-system");
		request.setFetchPath("/api/system/inner/export/page");
		request.setRequestId("r-1");
		request.setCreatedBy(1L);
		AuthProfile profile = AuthProfile.builder().userId(1L).username("u1").build();
		SecurityContextHolder.getContext()
			.setAuthentication(new UsernamePasswordAuthenticationToken(profile, null, null));

		FileExportTaskEntity duplicatedResult = new FileExportTaskEntity();
		setBaseId(duplicatedResult);
		when(fileExportTaskMapper.selectOne(any())).thenReturn(duplicatedResult);

		Long taskId = operations.createTask(request).getTaskId();
		assertThat(taskId).isEqualTo(9001L);
	}

	private void setBaseId(FileExportTaskEntity entity) {
		try {
			Field idField = entity.getClass().getSuperclass().getDeclaredField("id");
			idField.setAccessible(true);
			idField.set(entity, (Long) 9001L);
		}
		catch (ReflectiveOperationException exception) {
			throw new IllegalStateException(exception);
		}
	}

}
