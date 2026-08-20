package com.auth.service.system.file.storage.core.provider;

import com.auth.module.file.api.model.enums.StoragePlatformEnum;
import com.auth.service.system.file.config.properties.FileUploadProperties;
import com.auth.service.system.file.exception.FileStorageException;
import com.auth.service.system.file.model.value.FileUploadCommand;
import com.auth.service.system.file.model.value.StoredFile;
import com.auth.service.system.file.storage.S3PlatformProfileResolverFixtures;
import com.auth.service.system.file.storage.core.classifier.StorageExceptionClassifier;
import com.auth.service.system.file.storage.core.s3.S3ClientManager;
import com.auth.service.system.file.storage.core.s3.S3CompatibleFileStorageProvider;
import com.auth.service.system.file.storage.core.s3.S3PlatformProfileResolver;
import com.auth.service.system.file.storage.core.validator.StoragePlatformConfigValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import software.amazon.awssdk.awscore.exception.AwsErrorDetails;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.http.AbortableInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * {@link S3CompatibleFileStorageProvider} 单元测试。
 *
 * @author Bunny
 */
@DisplayName("S3CompatibleFileStorageProvider 统一存储实现")
@ExtendWith(MockitoExtension.class)
class S3CompatibleFileStorageProviderTest {

	private static final String BUCKET = "test-bucket";

	private static final String OBJECT_KEY = "images/2024/01/photo.png";

	private static final StoragePlatformEnum PLATFORM = StoragePlatformEnum.MINIO;

	private final Map<String, Boolean> headObjectExistence = new LinkedHashMap<>();

	@Mock
	private S3Client s3Client;

	@Mock
	private S3Presigner s3Presigner;

	@Mock
	private S3ClientManager clientManager;

	@Mock
	private StoragePlatformConfigValidator configValidator;

	@Mock
	private StorageExceptionClassifier classifier;

	private S3PlatformProfileResolver profileResolver;

	private S3CompatibleFileStorageProvider provider;

	private boolean headObjectStubRegistered;

	@BeforeEach
	void setUp() {
		headObjectExistence.clear();
		headObjectStubRegistered = false;
		FileUploadProperties properties = new FileUploadProperties();
		properties.getMinio().setEndpoint("http://127.0.0.1:9000");
		properties.getMinio().setAccessKey("ak");
		properties.getMinio().setSecretKey("sk");
		properties.getMinio().setBucket(BUCKET);
		profileResolver = S3PlatformProfileResolverFixtures.defaultResolver(properties);
		provider = new S3CompatibleFileStorageProvider(PLATFORM, profileResolver, clientManager, configValidator,
				classifier);
	}

	@Test
	@DisplayName("upload：调用 S3Client putObject 并返回带 etag 的 StoredFile")
	void upload_delegatesToS3Client() {
		// 验证上传路径把 MultipartFile 转成 S3 PutObjectRequest，并回传 ETag。
		when(clientManager.getClient(PLATFORM)).thenReturn(s3Client);
		byte[] bytes = "hello".getBytes();
		MockMultipartFile file = new MockMultipartFile("photo", "photo.png", "image/png", bytes);
		FileUploadCommand command = FileUploadCommand.builder()
			.storagePlatform(PLATFORM)
			.file(file)
			.objectKey(OBJECT_KEY)
			.build();
		when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
			.thenReturn(PutObjectResponse.builder().eTag("\"etag-123\"").build());

		StoredFile result = provider.upload(command);

		assertThat(result.getStoragePlatform()).isEqualTo(PLATFORM);
		assertThat(result.getBucket()).isEqualTo(BUCKET);
		assertThat(result.getObjectKey()).isEqualTo(OBJECT_KEY);
		assertThat(result.getEtag()).isEqualTo("\"etag-123\"");
		assertThat(result.getContentType()).isEqualTo("image/png");
		assertThat(result.getSize()).isEqualTo(bytes.length);
		assertThat(result.getUrl()).endsWith("/" + BUCKET + "/" + OBJECT_KEY);
		verify(configValidator, times(1)).validateOrThrow();
	}

	@Test
	@DisplayName("download：返回 S3 ResponseInputStream")
	void download_returnsS3ResponseStream() {
		// 验证下载会透传 S3 SDK 返回的输入流。
		when(clientManager.getClient(PLATFORM)).thenReturn(s3Client);
		InputStream expected = new ResponseInputStream<>(GetObjectResponse.builder().build(),
				AbortableInputStream.create(new ByteArrayInputStream("payload".getBytes())));
		when(s3Client.getObject(any(GetObjectRequest.class))).thenAnswer(inv -> expected);

		InputStream stream = provider.download(BUCKET, OBJECT_KEY);

		assertThat(stream).isSameAs(expected);
	}

	@Test
	@DisplayName("presignGetUrl：通过 S3Presigner 生成签名地址")
	void presignGetUrl_delegatesToPresigner() throws Exception {
		// 验证预签名走 Presigner 而不是 S3Client，保证签名 API 与 SDK 官方推荐一致。
		when(clientManager.getPresigner(PLATFORM)).thenReturn(s3Presigner);
		URL signed = new URL("http://127.0.0.1:9000/test-bucket/images/2024/01/photo.png?X-Amz-Signature=abc");
		PresignedGetObjectRequest presigned = mock(PresignedGetObjectRequest.class);
		when(presigned.url()).thenReturn(signed);
		when(s3Presigner.presignGetObject(any(GetObjectPresignRequest.class))).thenReturn(presigned);

		String url = provider.presignGetUrl(BUCKET, OBJECT_KEY, 300);

		assertThat(url).contains("X-Amz-Signature=abc");
	}

	@Test
	@DisplayName("delete：对象不存在时按幂等成功处理")
	void delete_succeedsWhenObjectNotFound() {
		// 验证 not-found 语义交由注入的分类器判定，不再硬编码 SDK 异常类型。
		when(clientManager.getClient(PLATFORM)).thenReturn(s3Client);
		S3Exception notFound = (S3Exception) S3Exception.builder()
			.statusCode(404)
			.awsErrorDetails(AwsErrorDetails.builder().errorCode("NoSuchKey").build())
			.build();
		doThrow(notFound).when(s3Client).deleteObject(any(DeleteObjectRequest.class));
		when(classifier.isNotFound(notFound)).thenReturn(true);

		assertThatCode(() -> provider.delete(BUCKET, OBJECT_KEY)).doesNotThrowAnyException();
		verify(classifier).isNotFound(notFound);
	}

	@Test
	@DisplayName("delete：其他错误仍抛业务异常")
	void delete_throwsWhenOtherErrorOccurs() {
		// 验证非 not-found 异常仍会阻断 DB 侧 purge。
		when(clientManager.getClient(PLATFORM)).thenReturn(s3Client);
		S3Exception denied = (S3Exception) S3Exception.builder()
			.statusCode(403)
			.awsErrorDetails(AwsErrorDetails.builder().errorCode("AccessDenied").build())
			.build();
		doThrow(denied).when(s3Client).deleteObject(any(DeleteObjectRequest.class));
		when(classifier.isNotFound(denied)).thenReturn(false);

		assertThatThrownBy(() -> provider.delete(BUCKET, OBJECT_KEY)).isInstanceOf(FileStorageException.class);
		verify(classifier).isNotFound(denied);
	}

	@Test
	@DisplayName("objectExists：对象存在时返回 true")
	void objectExistsReturnsTrueWhenObjectPresent() {
		// 验证 headObject 成功时判定对象已存在。
		when(clientManager.getClient(PLATFORM)).thenReturn(s3Client);
		stubHeadObject(OBJECT_KEY, true);

		assertThat(provider.objectExists(BUCKET, OBJECT_KEY)).isTrue();
		verify(s3Client).headObject(HeadObjectRequest.builder().bucket(BUCKET).key(OBJECT_KEY).build());
	}

	@Test
	@DisplayName("objectExists：NoSuchKey 时返回 false")
	void objectExistsReturnsFalseWhenNoSuchKey() {
		// 验证 not-found 语义返回 false 而非抛异常。
		when(clientManager.getClient(PLATFORM)).thenReturn(s3Client);
		stubHeadObject(OBJECT_KEY, false);

		assertThat(provider.objectExists(BUCKET, OBJECT_KEY)).isFalse();
	}

	@Test
	@DisplayName("objectExists：其他 S3 错误抛业务异常")
	void objectExistsThrowsWhenOtherErrorOccurs() {
		// 验证非 not-found 异常仍会翻译为业务异常。
		when(clientManager.getClient(PLATFORM)).thenReturn(s3Client);
		S3Exception denied = (S3Exception) S3Exception.builder()
			.statusCode(403)
			.awsErrorDetails(AwsErrorDetails.builder().errorCode("AccessDenied").build())
			.build();
		when(s3Client.headObject(any(HeadObjectRequest.class))).thenThrow(denied);
		when(classifier.isNotFound(denied)).thenReturn(false);

		assertThatThrownBy(() -> provider.objectExists(BUCKET, OBJECT_KEY)).isInstanceOf(FileStorageException.class);
	}

	@Test
	@DisplayName("move：copy 前校验源存在且目标不存在后执行 copy + delete")
	void move_copiesThenDeletesSource() {
		// 验证 move 在 proactive headObject 校验通过后走 S3 copyObject + deleteObject 组合。
		when(clientManager.getClient(PLATFORM)).thenReturn(s3Client);
		String sourceKey = "public/avatar/20260702/a.png";
		String destKey = "private/avatar/20260702/a.png";
		stubHeadObject(sourceKey, true);
		stubHeadObject(destKey, false);

		provider.move(BUCKET, sourceKey, destKey);

		verify(s3Client, times(2)).headObject(any(HeadObjectRequest.class));
		verify(s3Client).copyObject(CopyObjectRequest.builder()
			.sourceBucket(BUCKET)
			.sourceKey(sourceKey)
			.destinationBucket(BUCKET)
			.destinationKey(destKey)
			.build());
		verify(s3Client).deleteObject(DeleteObjectRequest.builder().bucket(BUCKET).key(sourceKey).build());
	}

	@Test
	@DisplayName("move：源对象不存在但目标已存在时按幂等成功处理")
	void move_succeedsWhenSourceAbsentAndDestinationExists() {
		// 验证重复 move 时源键缺失但目标键已存在，在 copy 前即视为已完成迁移。
		when(clientManager.getClient(PLATFORM)).thenReturn(s3Client);
		String sourceKey = "public/avatar/20260702/a.png";
		String destKey = "private/avatar/20260702/a.png";
		stubHeadObject(sourceKey, false);
		stubHeadObject(destKey, true);

		assertThatCode(() -> provider.move(BUCKET, sourceKey, destKey)).doesNotThrowAnyException();
		verify(s3Client, never()).copyObject(any(CopyObjectRequest.class));
		verify(s3Client, never()).deleteObject(any(DeleteObjectRequest.class));
	}

	@Test
	@DisplayName("move：源与目标均不存在时抛业务异常")
	void move_failsWhenSourceAbsentAndDestinationAbsent() {
		// 验证源、目标均缺失时不能静默成功，应抛出存储业务异常。
		when(clientManager.getClient(PLATFORM)).thenReturn(s3Client);
		String sourceKey = "public/avatar/20260702/a.png";
		String destKey = "private/avatar/20260702/a.png";
		stubHeadObject(sourceKey, false);
		stubHeadObject(destKey, false);

		assertThatThrownBy(() -> provider.move(BUCKET, sourceKey, destKey)).isInstanceOf(FileStorageException.class);
		verify(s3Client, never()).copyObject(any(CopyObjectRequest.class));
		verify(s3Client, never()).deleteObject(any(DeleteObjectRequest.class));
	}

	@Test
	@DisplayName("move：源与目标均存在时抛业务异常避免覆盖")
	void move_failsWhenSourceAndDestinationBothExist() {
		// 验证目标键已占用时拒绝 move，避免覆盖已有对象。
		when(clientManager.getClient(PLATFORM)).thenReturn(s3Client);
		String sourceKey = "public/avatar/20260702/a.png";
		String destKey = "private/avatar/20260702/a.png";
		stubHeadObject(sourceKey, true);
		stubHeadObject(destKey, true);

		assertThatThrownBy(() -> provider.move(BUCKET, sourceKey, destKey)).isInstanceOf(FileStorageException.class);
		verify(s3Client, never()).copyObject(any(CopyObjectRequest.class));
		verify(s3Client, never()).deleteObject(any(DeleteObjectRequest.class));
	}

	@Test
	@DisplayName("move：源与目标相同时短路跳过")
	void move_skipsWhenSourceEqualsDestination() {
		// 验证相同 objectKey 不会触发存储 SDK 调用。
		assertThatCode(() -> provider.move(BUCKET, OBJECT_KEY, OBJECT_KEY)).doesNotThrowAnyException();
		verifyNoInteractions(s3Client);
	}

	@Test
	@DisplayName("resolvePublicUrl：优先使用 publicUrl，否则回退 endpoint + bucket")
	void resolvePublicUrl_preferConfiguredDomain() {
		// 验证有 publicUrl 时不再走 endpoint 拼接。
		FileUploadProperties properties = new FileUploadProperties();
		properties.getMinio().setEndpoint("http://127.0.0.1:9000");
		properties.getMinio().setAccessKey("ak");
		properties.getMinio().setSecretKey("sk");
		properties.getMinio().setBucket(BUCKET);
		properties.getMinio().setPublicUrl("https://cdn.example.com/assets");
		profileResolver = S3PlatformProfileResolverFixtures.defaultResolver(properties);
		provider = new S3CompatibleFileStorageProvider(PLATFORM, profileResolver, clientManager, configValidator,
				classifier);

		String url = provider.resolvePublicUrl(BUCKET, OBJECT_KEY);

		assertThat(url).isEqualTo("https://cdn.example.com/assets/" + OBJECT_KEY);
	}

	@Test
	@DisplayName("resolvePublicUrl：阿里云 virtual-hosted 回退拼 bucket.endpoint")
	void resolvePublicUrl_aliyunVirtualHostedFallback() {
		FileUploadProperties properties = new FileUploadProperties();
		properties.getAliyunOss().setEndpoint("oss-cn-shanghai.aliyuncs.com");
		properties.getAliyunOss().setAccessKeyId("ak");
		properties.getAliyunOss().setAccessKeySecret("sk");
		properties.getAliyunOss().setBucket("bunny-auth");
		profileResolver = S3PlatformProfileResolverFixtures.defaultResolver(properties);
		provider = new S3CompatibleFileStorageProvider(StoragePlatformEnum.ALIYUN_OSS, profileResolver, clientManager,
				configValidator, classifier);

		String url = provider.resolvePublicUrl("bunny-auth", OBJECT_KEY);

		assertThat(url).isEqualTo("https://bunny-auth.oss-cn-shanghai.aliyuncs.com/" + OBJECT_KEY);
	}

	@Test
	@DisplayName("resolvePublicUrl：使用请求侧 bucket 覆盖配置侧 bucket")
	void resolvePublicUrl_prefersRequestBucket() {
		// 验证同一 endpoint 下多桶场景仍能拼出正确 URL。
		String customBucket = "another-bucket";
		when(clientManager.getClient(PLATFORM)).thenReturn(s3Client);
		byte[] bytes = "abc".getBytes();
		MockMultipartFile file = new MockMultipartFile("photo", "photo.png", "image/png", bytes);
		FileUploadCommand command = FileUploadCommand.builder()
			.storagePlatform(PLATFORM)
			.file(file)
			.objectKey(OBJECT_KEY)
			.build();
		when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
			.thenReturn(PutObjectResponse.builder().eTag("etag").build());

		String publicUrl = provider.resolvePublicUrl(customBucket, OBJECT_KEY);

		assertThat(publicUrl).isEqualTo("http://127.0.0.1:9000/" + customBucket + "/" + OBJECT_KEY);
		verify(configValidator).validateOrThrow();
		// 触发上传只是为了确保没有链路副作用
		assertThat(provider.upload(command).getUrl()).contains(BUCKET);
		verify(s3Client).putObject(eq(PutObjectRequest.builder()
			.bucket(BUCKET)
			.key(OBJECT_KEY)
			.contentType("image/png")
			.contentLength((long) bytes.length)
			.build()), any(RequestBody.class));
	}

	/**
	 * 按对象键模拟 headObject 存在性探测结果。
	 * @param objectKey 对象键
	 * @param exists 是否存在
	 */
	private void stubHeadObject(String objectKey, boolean exists) {
		headObjectExistence.put(objectKey, exists);
		if (headObjectStubRegistered) {
			return;
		}
		headObjectStubRegistered = true;
		NoSuchKeyException notFound = NoSuchKeyException.builder()
			.statusCode(404)
			.awsErrorDetails(AwsErrorDetails.builder().errorCode("NoSuchKey").build())
			.build();
		lenient().when(s3Client.headObject(any(HeadObjectRequest.class))).thenAnswer(invocation -> {
			HeadObjectRequest request = invocation.getArgument(0);
			Boolean keyExists = headObjectExistence.get(request.key());
			if (keyExists == null) {
				throw new IllegalStateException("Unexpected headObject key: " + request.key());
			}
			if (keyExists) {
				return HeadObjectResponse.builder().build();
			}
			throw notFound;
		});
	}

}
