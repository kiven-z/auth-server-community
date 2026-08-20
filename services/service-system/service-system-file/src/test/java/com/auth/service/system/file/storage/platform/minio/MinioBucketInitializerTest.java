package com.auth.service.system.file.storage.platform.minio;

import com.auth.module.file.api.model.enums.StoragePlatformEnum;
import com.auth.service.system.file.config.properties.FileUploadProperties;
import com.auth.service.system.file.exception.FileStorageException;
import com.auth.service.system.file.storage.S3PlatformProfileResolverFixtures;
import com.auth.service.system.file.storage.core.s3.S3ClientManager;
import com.auth.service.system.file.storage.core.s3.S3ExceptionClassifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.awscore.exception.AwsErrorDetails;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * {@link MinioBucketInitializer} 单元测试。
 *
 * @author Bunny
 */
@DisplayName("MinioBucketInitializer 走 S3 协议初始化")
@ExtendWith(MockitoExtension.class)
class MinioBucketInitializerTest {

	private static final String BUCKET = "test-bucket";

	@Mock
	private S3Client s3Client;

	@Mock
	private S3ClientManager clientManager;

	private MinioBucketInitializer initializer;

	@BeforeEach
	void setUp() {
		FileUploadProperties properties = new FileUploadProperties();
		properties.getMinio().setBucket(BUCKET);
		when(clientManager.getClient(StoragePlatformEnum.MINIO)).thenReturn(s3Client);
		initializer = new MinioBucketInitializer(S3PlatformProfileResolverFixtures.defaultResolver(properties),
				clientManager, new S3ExceptionClassifier());
	}

	@Test
	@DisplayName("ensureBucketReady：桶存在时跳过创建与策略")
	void ensureBucketReady_skipsWhenBucketExists() {
		// 验证 headBucket 成功即视为存在，不再重复创建或覆盖策略。
		when(s3Client.headBucket(any(HeadBucketRequest.class))).thenReturn(HeadBucketResponse.builder().build());

		assertThatCode(() -> initializer.ensureBucketReady()).doesNotThrowAnyException();

		verify(s3Client, never()).createBucket(any(CreateBucketRequest.class));
		verify(s3Client, never()).putBucketPolicy(any(PutBucketPolicyRequest.class));
	}

	@Test
	@DisplayName("ensureBucketReady：桶不存在时创建并应用公开前缀策略")
	void ensureBucketReady_createsBucketAndAppliesPolicy() {
		// 验证 NoSuchBucket 时按 create + putBucketPolicy 顺序补齐。
		when(s3Client.headBucket(any(HeadBucketRequest.class)))
			.thenThrow(NoSuchBucketException.builder().statusCode(404).message("not found").build());

		assertThatCode(() -> initializer.ensureBucketReady()).doesNotThrowAnyException();

		verify(s3Client, times(1)).createBucket(any(CreateBucketRequest.class));
		verify(s3Client, times(1)).putBucketPolicy(any(PutBucketPolicyRequest.class));
	}

	@Test
	@DisplayName("ensureBucketReady：非 not-found 错误抛业务异常")
	void ensureBucketReady_throwsOnOtherError() {
		// 验证 AccessDenied 等错误不会静默吞掉，会向外抛出业务异常。
		S3Exception denied = (S3Exception) S3Exception.builder()
			.statusCode(403)
			.awsErrorDetails(AwsErrorDetails.builder().errorCode("AccessDenied").build())
			.message("Access Denied")
			.build();
		doThrow(denied).when(s3Client).headBucket(any(HeadBucketRequest.class));

		assertThatThrownBy(() -> initializer.ensureBucketReady()).isInstanceOf(FileStorageException.class);
	}

}
