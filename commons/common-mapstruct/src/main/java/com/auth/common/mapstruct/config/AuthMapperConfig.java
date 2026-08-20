package com.auth.common.mapstruct.config;

import org.mapstruct.MapperConfig;
import org.mapstruct.ReportingPolicy;

/**
 * 业务 Converter 共享的 MapStruct 严格策略：目标未映射与类型转换失败在编译期报错。
 *
 * @author Bunny
 */
@MapperConfig(unmappedTargetPolicy = ReportingPolicy.ERROR, typeConversionPolicy = ReportingPolicy.ERROR)
public interface AuthMapperConfig {

}
