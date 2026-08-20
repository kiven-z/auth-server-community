package com.auth.common.jwt.support;

import cn.hutool.core.collection.CollUtil;
import io.jsonwebtoken.Claims;
import lombok.experimental.UtilityClass;

import java.util.Collection;

/**
 * aud 声明读取：从 Claims 读取 aud（字符串或数组），返回第一个值
 *
 * @author Bunny
 */
@UtilityClass
public class JwtAudienceCodec {

	/**
	 * 获取第一个受众
	 * @param claims 声明
	 * @return 第一个受众
	 */
	public static String first(Claims claims) {
		Object aud = claims.get(Claims.AUDIENCE);
		if (aud == null) {
			return null;
		}
		if (aud instanceof String s) {
			return s;
		}
		if (aud instanceof Collection<?> col && CollUtil.isNotEmpty(col)) {
			return String.valueOf(col.iterator().next());
		}
		return aud.toString();
	}

}