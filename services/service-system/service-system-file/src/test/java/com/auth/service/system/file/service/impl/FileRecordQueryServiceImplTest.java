package com.auth.service.system.file.service.impl;

import com.auth.common.data.model.PageResponse;
import com.auth.module.file.api.model.enums.FileDeleteSource;
import com.auth.module.file.api.model.enums.StoragePlatformEnum;
import com.auth.service.system.common.service.AuditUserDisplayService;
import com.auth.service.system.file.exception.FileStorageException;
import com.auth.service.system.file.exception.FileUploadResultCode;
import com.auth.service.system.file.mapper.FileRecordMapper;
import com.auth.service.system.file.model.entity.FileRecordEntity;
import com.auth.service.system.file.model.po.FileRecordPageRowPO;
import com.auth.service.system.file.model.query.FileRecordPageQuery;
import com.auth.service.system.file.model.vo.FileRecordDetailVO;
import com.auth.service.system.file.model.vo.FileRecordPageVO;
import com.auth.service.system.file.support.FileUrlSigner;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.repository.CrudRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * {@link FileRecordQueryServiceImpl} 单元测试。
 *
 * @author Bunny
 */
@DisplayName("FileRecordQueryServiceImpl 查询")
@ExtendWith(MockitoExtension.class)
class FileRecordQueryServiceImplTest {

	@Mock
	private FileRecordMapper fileRecordMapper;

	@Mock
	private AuditUserDisplayService auditUserDisplayService;

	@Mock
	private FileUrlSigner fileUrlSigner;

	private FileRecordQueryServiceImpl fileRecordQueryService;

	@BeforeEach
	void setUp() throws Exception {
		fileRecordQueryService = new FileRecordQueryServiceImpl(auditUserDisplayService, fileUrlSigner);
		Field baseMapperField = CrudRepository.class.getDeclaredField("baseMapper");
		baseMapperField.setAccessible(true);
		baseMapperField.set(fileRecordQueryService, fileRecordMapper);
	}

	@Test
	@DisplayName("getPage：管理端有效文件分页按 isDeleted=false 查询")
	void getPageUsesActiveDeletedFlagForAdminList() {
		// 验证管理端文件列表仅查询未删除记录。
		FileRecordPageQuery query = buildBaseQuery();
		mockPageResult(query, false);

		PageResponse<FileRecordPageVO> pageResult = fileRecordQueryService.getPage(query, false);

		assertThat(pageResult.getList()).hasSize(1);
		verify(fileRecordMapper).selectListByPage(any(), eq(query), eq(false));
		verify(auditUserDisplayService).enrichAuditUsernames(any(Page.class), isNull(), isNull());
	}

	@Test
	@DisplayName("getPage：个人有效文件分页透传 ownerUserId")
	void getPagePassesOwnerUserIdForPersonalList() {
		// 验证个人文件列表会把归属用户透传给 Mapper。
		FileRecordPageQuery query = buildBaseQuery();
		query.setOwnerUserId(9001L);
		mockPageResult(query, false);

		fileRecordQueryService.getPage(query, false);

		verify(fileRecordMapper).selectListByPage(any(),
				argThat(param -> Long.valueOf(9001L).equals(param.getOwnerUserId())), eq(false));
	}

	@Test
	@DisplayName("getPage：个人回收站分页透传用户可见删除来源")
	void getPageAppliesPersonalRecycleScope() {
		// 验证个人回收站会限制删除来源与归属用户。
		FileRecordPageQuery query = buildBaseQuery();
		query.setOwnerUserId(9001L);
		query.setDeleteSources(FileDeleteSource.userRecycleSourceCodes());
		mockPageResult(query, true);

		fileRecordQueryService.getPage(query, true);

		verify(fileRecordMapper).selectListByPage(any(),
				argThat(param -> FileDeleteSource.userRecycleSourceCodes().equals(param.getDeleteSources())
						&& Long.valueOf(9001L).equals(param.getOwnerUserId())),
				eq(true));
	}

	@Test
	@DisplayName("getPage：时间范围参数按原样透传到 Mapper")
	void getPagePassesTimeRangeParamsToMapper() {
		// 验证时间范围筛选参数会传递到分页查询条件。
		Instant startTime = LocalDateTime.of(2026, 7, 1, 0, 0, 0).toInstant(java.time.ZoneOffset.UTC);
		Instant endTime = LocalDateTime.of(2026, 7, 3, 23, 59, 59).toInstant(java.time.ZoneOffset.UTC);
		FileRecordPageQuery query = buildBaseQuery();
		query.setStartTime(startTime);
		query.setEndTime(endTime);
		mockPageResult(query, false);

		fileRecordQueryService.getPage(query, false);

		verify(fileRecordMapper).selectListByPage(any(),
				argThat(param -> startTime.equals(param.getStartTime()) && endTime.equals(param.getEndTime())),
				eq(false));
	}

	@Test
	@DisplayName("getDetail：可预览类型返回详情并填充 accessUrl")
	void getDetailReturnsDetailWithSignedAccessUrl() {
		// 验证详情查询会映射实体、签名临时访问地址并补充审计用户名。
		FileRecordEntity entity = buildDetailEntity(1001L);
		when(fileRecordMapper.selectDetailById(1001L, null, null, false, null)).thenReturn(entity);
		when(fileUrlSigner.isPreviewContentTypeAllowed(entity.getContentType())).thenReturn(true);
		when(fileUrlSigner.sign(entity.getBucket(), entity.getObjectKey(), entity.getContentType(),
				entity.getStoragePlatform()))
			.thenReturn("https://minio.example.com/public/a.png?X-Amz-Signature=demo");

		FileRecordDetailVO detail = fileRecordQueryService.getDetail(1001L, null, null, false, null);

		assertThat(detail.getId()).isEqualTo(1001L);
		assertThat(detail.getAccessUrl()).isEqualTo("https://minio.example.com/public/a.png?X-Amz-Signature=demo");
		verify(auditUserDisplayService)
			.enrichAuditUsernames(org.mockito.ArgumentMatchers.<List<FileRecordDetailVO>>any(), isNull(), isNull());
	}

	@Test
	@DisplayName("getDetail：不可预览类型仍返回详情并用 signDownload 填充 accessUrl")
	void getDetailUsesSignDownloadWhenContentTypeNotPreviewable() {
		// 验证不可预览类型也签发临时访问地址，便于分享；仅跳过预览 MIME 白名单。
		FileRecordEntity entity = buildDetailEntity(1002L);
		entity.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
		entity.setObjectKey("export/a.xlsx");
		when(fileRecordMapper.selectDetailById(1002L, 9001L, null, false, null)).thenReturn(entity);
		when(fileUrlSigner.isPreviewContentTypeAllowed(entity.getContentType())).thenReturn(false);
		when(fileUrlSigner.signDownload(entity.getBucket(), entity.getObjectKey(), entity.getStoragePlatform()))
			.thenReturn("https://minio.example.com/private/export/a.xlsx?X-Amz-Signature=demo");

		FileRecordDetailVO detail = fileRecordQueryService.getDetail(1002L, 9001L, null, false, null);

		assertThat(detail.getId()).isEqualTo(1002L);
		assertThat(detail.getAccessUrl())
			.isEqualTo("https://minio.example.com/private/export/a.xlsx?X-Amz-Signature=demo");
		verify(fileUrlSigner).isPreviewContentTypeAllowed(entity.getContentType());
		verify(fileUrlSigner, never()).sign(anyString(), anyString(), anyString(), any());
		verify(fileUrlSigner).signDownload(entity.getBucket(), entity.getObjectKey(), entity.getStoragePlatform());
	}

	@Test
	@DisplayName("getDetail：记录不存在时抛出 FILE_RECORD_NOT_FOUND")
	void getDetailThrowsWhenRecordMissing() {
		// 验证详情查询未命中记录时统一返回 404 语义。
		when(fileRecordMapper.selectDetailById(2001L, 9001L, null, false, null)).thenReturn(null);

		assertThatThrownBy(() -> fileRecordQueryService.getDetail(2001L, 9001L, null, false, null))
			.isInstanceOf(FileStorageException.class)
			.satisfies(exception -> {
				FileStorageException fileException = (FileStorageException) exception;
				assertThat(fileException.getResultCode()).isEqualTo(FileUploadResultCode.FILE_RECORD_NOT_FOUND);
				assertThat(fileException.getMessageArgs()).isEmpty();
			});
	}

	@Test
	@DisplayName("getDetail：个人越权访问时按记录不存在处理")
	void getDetailTreatsOwnerMismatchAsNotFound() {
		// 验证个人详情通过 SQL 归属过滤，越权时不会暴露记录存在性。
		when(fileRecordMapper.selectDetailById(3001L, 9002L, null, false, null)).thenReturn(null);

		assertThatThrownBy(() -> fileRecordQueryService.getDetail(3001L, 9002L, null, false, null))
			.isInstanceOf(FileStorageException.class)
			.extracting(ex -> ((FileStorageException) ex).getResultCode())
			.isEqualTo(FileUploadResultCode.FILE_RECORD_NOT_FOUND);
	}

	@Test
	@DisplayName("getDetail：个人回收站透传删除范围条件")
	void getDetailPassesPersonalRecycleScopeToMapper() {
		// 验证个人回收站详情会限制私有、已删除与用户可见删除来源。
		FileRecordEntity entity = buildDetailEntity(4001L);
		entity.setIsPrivate(true);
		entity.setIsDeleted(true);
		entity.setDeleteSource(FileDeleteSource.USER_SELF.getCode());
		when(fileRecordMapper.selectDetailById(4001L, 9001L, true, true, FileDeleteSource.userRecycleSourceCodes()))
			.thenReturn(entity);
		when(fileUrlSigner.isPreviewContentTypeAllowed(entity.getContentType())).thenReturn(true);
		when(fileUrlSigner.sign(anyString(), anyString(), anyString(), any()))
			.thenReturn("https://signed.example/a.png");

		fileRecordQueryService.getDetail(4001L, 9001L, true, true, FileDeleteSource.userRecycleSourceCodes());

		verify(fileRecordMapper).selectDetailById(4001L, 9001L, true, true, FileDeleteSource.userRecycleSourceCodes());
	}

	private FileRecordPageQuery buildBaseQuery() {
		FileRecordPageQuery query = new FileRecordPageQuery();
		query.setPageIndex(1);
		query.setPageSize(20);
		return query;
	}

	private void mockPageResult(FileRecordPageQuery query, boolean deleted) {
		FileRecordPageRowPO row = new FileRecordPageRowPO();
		row.setId(2001L);
		row.setStoragePlatform(StoragePlatformEnum.MINIO);
		row.setOriginalName("demo.png");
		row.setContentType("image/png");
		row.setCreatedBy(1L);
		Page<FileRecordPageRowPO> page = new Page<>(1, 20, 1);
		page.setRecords(List.of(row));
		when(fileRecordMapper.selectListByPage(any(), eq(query), eq(deleted))).thenReturn(page);
	}

	private FileRecordEntity buildDetailEntity(Long id) {
		FileRecordEntity entity = new FileRecordEntity();
		entity.setId(id);
		entity.setStoragePlatform(StoragePlatformEnum.MINIO);
		entity.setContentType("image/png");
		entity.setBucket("public");
		entity.setObjectKey("a.png");
		entity.setCreatedBy(1L);
		return entity;
	}

}
