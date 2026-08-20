package com.auth.common.mapstruct.config;

import org.mapstruct.MapperConfig;
import org.mapstruct.ReportingPolicy;

/**
 * 源、目标字段一一对应的严格映射：未映射的源属性与目标属性均在编译期报错。
 *
 * @author Bunny
 */
@MapperConfig(unmappedTargetPolicy = ReportingPolicy.ERROR, unmappedSourcePolicy = ReportingPolicy.ERROR,
		typeConversionPolicy = ReportingPolicy.ERROR)
public interface AuthSymmetricMapperConfig {

}
