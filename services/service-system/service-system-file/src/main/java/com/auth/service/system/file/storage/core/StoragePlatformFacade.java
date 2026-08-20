package com.auth.service.system.file.storage.core;

import com.auth.module.file.api.model.enums.StoragePlatformEnum;
import com.auth.service.system.file.storage.core.bucket.StorageBucketInitializer;
import com.auth.service.system.file.storage.core.capability.StoragePlatformCapability;
import com.auth.service.system.file.storage.core.classifier.StorageExceptionClassifier;
import com.auth.service.system.file.storage.core.provider.FileStorageProvider;
import com.auth.service.system.file.storage.core.validator.StoragePlatformConfigValidator;

import java.util.Optional;

/**
 * 存储平台门面，聚合同平台的存储能力实现。
 *
 * @author Bunny
 */
public interface StoragePlatformFacade {

	/**
	 * 门面所属平台
	 * @return 存储平台
	 */
	StoragePlatformEnum platform();

	/**
	 * 获取文件存储能力
	 * @return 文件存储能力
	 */
	FileStorageProvider provider();

	/**
	 * 获取存储桶初始化能力
	 * @return 存储桶初始化能力
	 */
	StorageBucketInitializer initializer();

	/**
	 * 获取平台配置校验能力
	 * @return 平台配置校验能力
	 */
	StoragePlatformConfigValidator validator();

	/**
	 * 获取平台异常分类器
	 * @return 存储异常分类器
	 */
	StorageExceptionClassifier exceptionClassifier();

	/**
	 * 按类型解析厂商私有能力；未实现该能力的平台返回 {@link Optional#empty()}。
	 * @param capabilityType 能力接口类型
	 * @param <T> 能力类型
	 * @return 能力实例（可能为空）
	 */
	default <T extends StoragePlatformCapability> Optional<T> capability(Class<T> capabilityType) {
		return Optional.empty();
	}

}
