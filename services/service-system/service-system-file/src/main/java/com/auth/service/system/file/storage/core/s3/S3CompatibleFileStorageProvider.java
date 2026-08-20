package com.auth.service.system.file.storage.core.s3;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.auth.module.file.api.model.enums.StoragePlatformEnum;
import com.auth.service.system.file.config.properties.S3PlatformProfile;
import com.auth.service.system.file.exception.FileStorageException;
import com.auth.service.system.file.exception.FileUploadResultCode;
import com.auth.service.system.file.model.value.FileUploadCommand;
import com.auth.service.system.file.model.value.StoredFile;
import com.auth.service.system.file.storage.core.classifier.StorageExceptionClassifier;
import com.auth.service.system.file.storage.core.provider.FileStorageProvider;
import com.auth.service.system.file.storage.core.validator.StoragePlatformConfigValidator;
import com.auth.service.system.file.utils.StorageUrlUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.awscore.exception.AwsErrorDetails;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.time.Duration;
import java.util.function.Supplier;

/**
 * 基于 AWS SDK v2 的 S3 兼容存储 Provider
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Slf4j
public class S3CompatibleFileStorageProvider implements FileStorageProvider {

	private static final String DEFAULT_CONTENT_TYPE = "application/octet-stream";

	private final StoragePlatformEnum platform;

	private final S3PlatformProfileResolver profileResolver;

	private final S3ClientManager s3ClientManager;

	private final StoragePlatformConfigValidator configValidator;

	private final StorageExceptionClassifier storageExceptionClassifier;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public StoredFile upload(FileUploadCommand command) {
		return execute(() -> {
			S3PlatformProfile profile = profileResolver.resolve(platform);
			String contentType = CharSequenceUtil.blankToDefault(command.getFile().getContentType(),
					DEFAULT_CONTENT_TYPE);
			String bucket = profile.getBucket();
			String objectKey = command.getObjectKey();
			long size = command.getFile().getSize();

			try (InputStream inputStream = command.getFile().getInputStream()) {
				PutObjectRequest putRequest = PutObjectRequest.builder()
					.bucket(bucket)
					.key(objectKey)
					.contentType(contentType)
					.contentLength(size)
					.build();
				PutObjectResponse response = s3ClientManager.getClient(platform)
					.putObject(putRequest, RequestBody.fromInputStream(inputStream, size));

				return StoredFile.builder()
					.storagePlatform(platform)
					.bucket(bucket)
					.objectKey(objectKey)
					.url(resolvePublicUrl(bucket, objectKey))
					.originalName(command.getFile().getOriginalFilename())
					.extension(FileUtil.extName(command.getFile().getOriginalFilename()))
					.contentType(contentType)
					.size(size)
					.etag(response.eTag())
					.build();
			}
			catch (IOException ioException) {
				throw new UncheckedIOException(ioException);
			}
		});
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public InputStream download(String bucket, String objectKey) {
		return execute(() -> s3ClientManager.getClient(platform)
			.getObject(GetObjectRequest.builder().bucket(bucket).key(objectKey).build()));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String presignGetUrl(String bucket, String objectKey, int expireSeconds) {
		return execute(() -> {
			S3Presigner presigner = s3ClientManager.getPresigner(platform);
			GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
				.signatureDuration(Duration.ofSeconds(expireSeconds))
				.getObjectRequest(request -> request.bucket(bucket).key(objectKey))
				.build();
			PresignedGetObjectRequest presigned = presigner.presignGetObject(presignRequest);
			return presigned.url().toString();
		});
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String resolvePublicUrl(String bucket, String objectKey) {
		configValidator.validateOrThrow();
		S3PlatformProfile profile = profileResolver.resolve(platform);
		return StorageUrlUtil.resolveObjectUrl(profile, bucket, objectKey);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean objectExists(String bucket, String objectKey) {
		return execute(() -> {
			try {
				s3ClientManager.getClient(platform)
					.headObject(HeadObjectRequest.builder().bucket(bucket).key(objectKey).build());
				return true;
			}
			catch (NoSuchKeyException exception) {
				return false;
			}
			catch (S3Exception exception) {
				if (storageExceptionClassifier.isNotFound(exception)) {
					return false;
				}
				throw exception;
			}
		});
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void delete(String bucket, String objectKey) {
		try {
			s3ClientManager.getClient(platform)
				.deleteObject(DeleteObjectRequest.builder().bucket(bucket).key(objectKey).build());
		}
		catch (Exception exception) {
			if (storageExceptionClassifier.isNotFound(exception)) {
				log.warn("Storage object already absent, skip delete: bucket={}, objectKey={}, reason={}", bucket,
						objectKey, exception.getMessage());
				return;
			}
			throw new FileStorageException(FileUploadResultCode.FILE_UPLOAD_FAILED, platform.name(),
					exception.getMessage());
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void move(String bucket, String sourceObjectKey, String destinationObjectKey) {
		if (sourceObjectKey.equals(destinationObjectKey)) {
			return;
		}
		execute(() -> {
			boolean sourceExists = objectExists(bucket, sourceObjectKey);
			boolean destinationExists = objectExists(bucket, destinationObjectKey);

			if (!sourceExists) {
				if (destinationExists) {
					return;
				}
				throw S3Exception.builder()
					.statusCode(404)
					.awsErrorDetails(AwsErrorDetails.builder().errorCode("NoSuchKey").build())
					.message("Source object not found: " + sourceObjectKey)
					.build();
			}
			if (destinationExists) {
				throw S3Exception.builder()
					.statusCode(409)
					.awsErrorDetails(AwsErrorDetails.builder().errorCode("ObjectAlreadyExists").build())
					.message("Destination object already exists: " + destinationObjectKey)
					.build();
			}

			s3ClientManager.getClient(platform)
				.copyObject(CopyObjectRequest.builder()
					.sourceBucket(bucket)
					.sourceKey(sourceObjectKey)
					.destinationBucket(bucket)
					.destinationKey(destinationObjectKey)
					.build());

			try {
				s3ClientManager.getClient(platform)
					.deleteObject(DeleteObjectRequest.builder().bucket(bucket).key(sourceObjectKey).build());
			}
			catch (S3Exception exception) {
				if (storageExceptionClassifier.isNotFound(exception)) {
					return;
				}
				throw exception;
			}
		});
	}

	/**
	 * 执行存储操作，并将 SDK 异常翻译为业务异常。
	 * @param action 存储操作
	 * @param <T> 返回值类型
	 * @return 操作结果
	 */
	private <T> T execute(Supplier<T> action) {
		try {
			return action.get();
		}
		catch (Exception exception) {
			throw new FileStorageException(FileUploadResultCode.FILE_UPLOAD_FAILED, platform.name(),
					exception.getMessage());
		}
	}

	/**
	 * 执行无返回值的存储操作，并将 SDK 异常翻译为业务异常。
	 * @param action 存储操作
	 */
	private void execute(Runnable action) {
		try {
			action.run();
		}
		catch (Exception exception) {
			throw new FileStorageException(FileUploadResultCode.FILE_UPLOAD_FAILED, platform.name(),
					exception.getMessage());
		}
	}

}
