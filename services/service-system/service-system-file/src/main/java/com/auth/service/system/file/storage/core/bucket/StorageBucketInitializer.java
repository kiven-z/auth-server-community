package com.auth.service.system.file.storage.core.bucket;

/**
 * 存储桶初始化器
 *
 * @author Bunny
 */
public interface StorageBucketInitializer {

	/**
	 * 确保存储桶处于可用状态
	 */
	void ensureBucketReady();

}
