package com.auth.service.system.file.storage.core.provider;

/**
 * 文件存储策略接口
 *
 * <p>
 * 该接口作为组合接口保留，避免一次性改动所有调用点
 * </p>
 *
 * @author Bunny
 */
public interface FileStorageProvider extends FileStorageReadOps, FileStorageWriteOps {

}
