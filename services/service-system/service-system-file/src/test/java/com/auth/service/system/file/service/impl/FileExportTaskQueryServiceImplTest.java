package com.auth.service.system.file.service.impl;

import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.common.exception.code.SystemCommonResultCode;
import com.auth.service.system.common.service.AuditUserDisplayService;
import com.auth.service.system.file.mapper.FileExportTaskMapper;
import com.auth.service.system.file.model.entity.FileExportTaskEntity;
import com.auth.service.system.file.model.enums.FileExportTaskStatus;
import com.auth.service.system.file.model.vo.FileExportTaskDetailVO;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * {@link FileExportTaskQueryServiceImpl} 单元测试
 *
 * @author Bunny
 */
@DisplayName("FileExportTaskQueryServiceImpl 查询服务")
@ExtendWith(MockitoExtension.class)
class FileExportTaskQueryServiceImplTest {

	@Mock
	private FileExportTaskMapper fileExportTaskMapper;

	@Mock
	private AuditUserDisplayService auditUserDisplayService;

	@BeforeAll
	static void initMybatisPlusTableInfo() {
		TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""),
				FileExportTaskEntity.class);
	}

	@Test
	@DisplayName("getDetail：本人任务返回查询快照且不含权限/租约字段")
	void getDetailReturnsOwnedTaskWithoutOpsFields() {
		FileExportTaskQueryServiceImpl service = newService();

		FileExportTaskEntity entity = new FileExportTaskEntity();
		setBaseId(entity);
		entity.setBizType("SYS_USER");
		entity.setExportFormat("XLSX");
		entity.setQuerySnapshotJson("{\"username\":\"alice\"}");
		entity.setScopeSnapshotJson("{\"userId\":9,\"dataScopes\":null}");
		entity.setStatus(FileExportTaskStatus.FAILED.name());
		entity.setErrorCode("EXPORT_FAILED");
		entity.setErrorMessage("boom");
		entity.setLockedBy("worker-1");
		entity.setCreatedBy(9001L);
		when(fileExportTaskMapper.selectOne(any())).thenReturn(entity);

		FileExportTaskDetailVO detail = service.getDetail(5001L, 9001L);

		assertThat(detail.getBizType()).isEqualTo("SYS_USER");
		assertThat(detail.getQuerySnapshotJson()).isEqualTo("{\"username\":\"alice\"}");
		assertThat(detail.getStatus()).isEqualTo(FileExportTaskStatus.FAILED.name());
		assertThat(detail.getErrorCode()).isEqualTo("EXPORT_FAILED");
		assertThat(detail.getErrorMessage()).isEqualTo("boom");
		assertThat(detail.getClass().getDeclaredFields()).extracting(Field::getName)
			.doesNotContain("scopeSnapshotJson", "lockedBy", "leaseUntil");
	}

	@Test
	@DisplayName("getDetail：非本人或不存在时抛出 DATA_UNAVAILABLE")
	void getDetailThrowsWhenTaskMissing() {
		FileExportTaskQueryServiceImpl service = newService();
		when(fileExportTaskMapper.selectOne(any())).thenReturn(null);

		assertThatThrownBy(() -> service.getDetail(5002L, 9002L)).isInstanceOf(SystemBusinessException.class)
			.extracting(ex -> ((SystemBusinessException) ex).getResultCode())
			.isEqualTo(SystemCommonResultCode.DATA_UNAVAILABLE);
	}

	private FileExportTaskQueryServiceImpl newService() {
		FileExportTaskQueryServiceImpl service = new FileExportTaskQueryServiceImpl(auditUserDisplayService);
		ReflectionTestUtils.setField(service, "baseMapper", fileExportTaskMapper);
		ReflectionTestUtils.setField(service, "entityClass", FileExportTaskEntity.class);
		return service;
	}

	private void setBaseId(FileExportTaskEntity entity) {
		try {
			Field idField = entity.getClass().getSuperclass().getDeclaredField("id");
			idField.setAccessible(true);
			idField.set(entity, (Long) 5001L);
		}
		catch (ReflectiveOperationException exception) {
			throw new IllegalStateException(exception);
		}
	}

}
