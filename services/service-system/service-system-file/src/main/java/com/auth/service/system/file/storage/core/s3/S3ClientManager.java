package com.auth.service.system.file.storage.core.s3;

import cn.hutool.core.text.CharSequenceUtil;
import com.auth.module.file.api.model.enums.StoragePlatformEnum;
import com.auth.service.system.file.config.properties.S3PlatformProfile;
import com.auth.service.system.file.exception.FileStorageException;
import com.auth.service.system.file.exception.FileUploadResultCode;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.checksums.RequestChecksumCalculation;
import software.amazon.awssdk.core.checksums.ResponseChecksumValidation;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.net.URI;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * S3 客户端管理器
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Slf4j
@Component
public class S3ClientManager {

	private final S3PlatformProfileResolver profileResolver;

	private final Map<StoragePlatformEnum, S3Client> clientByPlatform = new ConcurrentHashMap<>();

	private final Map<StoragePlatformEnum, S3Presigner> presignerByPlatform = new ConcurrentHashMap<>();

	/**
	 * 获取指定平台的 S3 客户端（懒加载）
	 * @param platform 存储平台
	 * @return S3 客户端
	 */
	public S3Client getClient(StoragePlatformEnum platform) {
		S3Client cached = clientByPlatform.get(platform);
		if (cached != null) {
			return cached;
		}
		return clientByPlatform.computeIfAbsent(platform, this::buildClient);
	}

	/**
	 * 获取指定平台的 Presigner（懒加载）
	 * @param platform 存储平台
	 * @return S3 Presigner
	 */
	public S3Presigner getPresigner(StoragePlatformEnum platform) {
		S3Presigner cached = presignerByPlatform.get(platform);
		if (cached != null) {
			return cached;
		}
		return presignerByPlatform.computeIfAbsent(platform, this::buildPresigner);
	}

	/**
	 * 应用停机时释放所有平台的客户端资源
	 */
	@PreDestroy
	public void shutdown() {
		closeQuietly("S3Client", clientByPlatform);
		closeQuietly("S3Presigner", presignerByPlatform);
		clientByPlatform.clear();
		presignerByPlatform.clear();
	}

	private S3Client buildClient(StoragePlatformEnum platform) {
		S3PlatformProfile profile = requireProfile(platform);
		S3Configuration s3Configuration = S3Configuration.builder()
			.pathStyleAccessEnabled(profile.isPathStyleAccess())
			.build();

		return S3Client.builder()
			.endpointOverride(URI.create(profile.getEndpoint()))
			.region(Region.of(profile.getRegion()))
			.credentialsProvider(StaticCredentialsProvider
				.create(AwsBasicCredentials.create(profile.getAccessKey(), profile.getSecretKey())))
			.serviceConfiguration(s3Configuration)
			.requestChecksumCalculation(RequestChecksumCalculation.WHEN_REQUIRED)
			.responseChecksumValidation(ResponseChecksumValidation.WHEN_REQUIRED)
			.build();
	}

	private S3Presigner buildPresigner(StoragePlatformEnum platform) {
		S3PlatformProfile profile = requireProfile(platform);
		S3Configuration s3Configuration = S3Configuration.builder()
			.pathStyleAccessEnabled(profile.isPathStyleAccess())
			.build();

		return S3Presigner.builder()
			.endpointOverride(URI.create(profile.getEndpoint()))
			.region(Region.of(profile.getRegion()))
			.credentialsProvider(StaticCredentialsProvider
				.create(AwsBasicCredentials.create(profile.getAccessKey(), profile.getSecretKey())))
			.serviceConfiguration(s3Configuration)
			.build();
	}

	private S3PlatformProfile requireProfile(StoragePlatformEnum platform) {
		S3PlatformProfile profile = profileResolver.resolve(platform);
		String platformKey = "auth.file.platforms." + platform.name();
		assertNotBlank(profile.getEndpoint(), platformKey + ".endpoint");
		assertNotBlank(profile.getAccessKey(), platformKey + ".access-key");
		assertNotBlank(profile.getSecretKey(), platformKey + ".secret-key");
		assertNotBlank(profile.getBucket(), platformKey + ".bucket");
		assertNotBlank(profile.getRegion(), platformKey + ".region");
		return profile;
	}

	private void assertNotBlank(String value, String configKey) {
		if (CharSequenceUtil.isBlank(value)) {
			throw new FileStorageException(FileUploadResultCode.FILE_STORAGE_CONFIG_MISSING, configKey);
		}
	}

	private void closeQuietly(String clientKind, Map<StoragePlatformEnum, ? extends AutoCloseable> resources) {
		resources.forEach((platform, resource) -> {
			try {
				resource.close();
			}
			catch (Exception exception) {
				log.warn("Failed to close {} for platform {}: {}", clientKind, platform, exception.getMessage());
			}
		});
	}

}
