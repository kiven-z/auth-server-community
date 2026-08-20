package com.auth.common.ip.normalize;

import java.util.Optional;

/**
 * 正常化原始ip字符串为规范形式用于检查和搜索
 *
 * @author Bunny
 */
public interface IpNormalizer {

	/**
	 * 正常化原始ip字符串
	 * @param raw 原始ip字符串
	 * @return 正常化后的ip或空当无效时
	 */
	Optional<String> normalize(String raw);

}
