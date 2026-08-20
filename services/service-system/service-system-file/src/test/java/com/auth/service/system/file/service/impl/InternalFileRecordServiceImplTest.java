package com.auth.service.system.file.service.impl;

import com.auth.module.file.api.model.enums.FileDeleteSource;
import com.auth.module.file.api.model.enums.StoragePlatformEnum;
import com.auth.module.file.api.model.request.OwnedFileAssertByUrlRequest;
import com.auth.module.file.api.model.request.OwnedFileDeleteByUrlRequest;
import com.auth.service.system.file.exception.FileStorageException;
import com.auth.service.system.file.exception.FileUploadResultCode;
import com.auth.service.system.file.mapper.FileRecordMapper;
import com.auth.service.system.file.model.entity.FileRecordEntity;
import com.auth.service.system.file.storage.core.StoragePlatformFacade;
import com.auth.service.system.file.storage.core.StoragePlatformFacadeRegistry;
import com.auth.service.system.file.storage.core.provider.FileStorageProvider;
import com.baomidou.mybatisplus.extension.repository.CrudRepository;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * {@link FileRecordServiceImpl} 内部接口能力单元测试。
 */
@DisplayName("FileRecordServiceImpl 内部接口能力")
@ExtendWith(MockitoExtension.class)
class InternalFileRecordServiceImplTest {

	private static final Long OWNER_USER_ID = 9001L;

	private static final String AVATAR_URL = "https://cdn.example.com/public/avatar/20260702/a.png";

	private static final String AVATAR_OBJECT_KEY = "public/avatar/20260702/a.png";

	@Mock
	private FileRecordMapper fileRecordMapper;

	@Mock
	private StoragePlatformFacadeRegistry facadeRegistry;

	@Mock
	private StoragePlatformFacade storagePlatformFacade;

	@Mock
	private FileStorageProvider minioProvider;

	private FileRecordServiceImpl fileRecordService;

	private static FileRecordEntity buildEntity() {
		FileRecordEntity entity = new FileRecordEntity();
		entity.setId(100L);
		entity.setStoragePlatform(StoragePlatformEnum.MINIO);
		entity.setBucket("public");
		entity.setObjectKey(AVATAR_OBJECT_KEY);
		entity.setUrl(AVATAR_URL);
		entity.setIsPrivate(false);
		entity.setBizType("avatar");
		entity.setCreatedBy(OWNER_USER_ID);
		return entity;
	}

	@BeforeEach
	void setUp() throws Exception {
		fileRecordService = spy(new FileRecordServiceImpl(facadeRegistry));
		Field baseMapperField = CrudRepository.class.getDeclaredField("baseMapper");
		baseMapperField.setAccessible(true);
		baseMapperField.set(fileRecordService, fileRecordMapper);
	}

	@Test
	@DisplayName("assertOwnedFileUrl：命中记录时不抛异常")
	void assertOwnedFileUrl_success() {
		// 按 URL 查询活跃记录
		FileRecordEntity entity = buildEntity();
		when(fileRecordMapper.selectActiveByUrlAndOwner(eq(AVATAR_URL), isNull(), eq(OWNER_USER_ID), eq("avatar")))
			.thenReturn(entity);

		OwnedFileAssertByUrlRequest assertRequest = new OwnedFileAssertByUrlRequest();
		assertRequest.setUrl(AVATAR_URL);
		assertRequest.setOwnerUserId(OWNER_USER_ID);
		assertRequest.setBizType("avatar");
		assertThatCode(() -> fileRecordService.assertOwnedFileUrl(assertRequest)).doesNotThrowAnyException();
	}

	@Test
	@DisplayName("assertOwnedFileUrl：未命中时抛出 FILE_RECORD_NOT_FOUND")
	void assertOwnedFileUrl_notFound() {
		// 未命中活跃记录
		when(fileRecordMapper.selectActiveByUrlAndOwner(eq(AVATAR_URL), isNull(), eq(OWNER_USER_ID), eq("avatar")))
			.thenReturn(null);

		OwnedFileAssertByUrlRequest assertRequest = new OwnedFileAssertByUrlRequest();
		assertRequest.setUrl(AVATAR_URL);
		assertRequest.setOwnerUserId(OWNER_USER_ID);
		assertRequest.setBizType("avatar");
		ThrowingCallable executable = () -> fileRecordService.assertOwnedFileUrl(assertRequest);
		assertThatThrownBy(executable).isInstanceOf(FileStorageException.class)
			.extracting(ex -> ((FileStorageException) ex).getResultCode())
			.isEqualTo(FileUploadResultCode.FILE_RECORD_NOT_FOUND);
	}

	@Test
	@DisplayName("tryDeleteOwnedByUrl：未命中时静默跳过")
	void tryDeleteOwnedByUrl_notFoundSkips() {
		String url = "https://cdn.example.com/public/avatar/20260702/old.png";
		String objectKey = "public/avatar/20260702/old.png";
		// 按 objectKey 查询未命中
		when(fileRecordMapper.selectActiveByUrlAndOwner(isNull(), eq(objectKey), eq(OWNER_USER_ID), eq("avatar")))
			.thenReturn(null);

		OwnedFileDeleteByUrlRequest deleteRequest = new OwnedFileDeleteByUrlRequest();
		deleteRequest.setUrl(url);
		deleteRequest.setOwnerUserId(OWNER_USER_ID);
		deleteRequest.setDeleteSource(FileDeleteSource.SYSTEM_ACTION.getCode());
		deleteRequest.setBizType("avatar");
		fileRecordService.tryDeleteOwnedByUrl(deleteRequest);

		verify(facadeRegistry, never()).resolve(any());
		verify(fileRecordMapper, never()).deletePhysicallyByIds(anyList(), anyBoolean());
		verify(fileRecordMapper, never()).softDeleteByIds(anyList(), anyString(), any());
		verify(fileRecordService, never()).updateBatchById(anyList());
	}

	@Test
	@DisplayName("tryDeleteOwnedByUrl：命中记录时执行删除")
	void tryDeleteOwnedByUrl_deletesWhenFound() {
		FileRecordEntity entity = buildEntity();
		// 按 objectKey 查询命中
		when(fileRecordMapper.selectActiveByUrlAndOwner(isNull(), eq(AVATAR_OBJECT_KEY), eq(OWNER_USER_ID),
				eq("avatar")))
			.thenReturn(entity);
		when(facadeRegistry.resolve(StoragePlatformEnum.MINIO)).thenReturn(storagePlatformFacade);
		when(storagePlatformFacade.provider()).thenReturn(minioProvider);

		OwnedFileDeleteByUrlRequest deleteRequest = new OwnedFileDeleteByUrlRequest();
		deleteRequest.setUrl(AVATAR_URL);
		deleteRequest.setOwnerUserId(OWNER_USER_ID);
		deleteRequest.setDeleteSource(FileDeleteSource.SYSTEM_ACTION.getCode());
		deleteRequest.setBizType("avatar");
		fileRecordService.tryDeleteOwnedByUrl(deleteRequest);

		verify(minioProvider).delete("public", AVATAR_OBJECT_KEY);
		verify(fileRecordMapper).deletePhysicallyByIds(List.of(100L), false);
		verify(fileRecordMapper, never()).softDeleteByIds(anyList(), anyString(), any());
		verify(fileRecordService, never()).updateBatchById(anyList());
	}

}
