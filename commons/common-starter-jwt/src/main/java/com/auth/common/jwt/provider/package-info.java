/**
 * <p>
 * 模板方法抽象类，封装 JWT 生成的公共流程（构建 Claims、签名前准备），要求子类提供解析器和签名算法
 *
 * @see com.auth.common.jwt.provider.AbstractJwtTokenProvider
 * </p>
 *
 * <p>
 * HS256 实现，使用对称密钥进行签名和验证
 * @see com.auth.common.jwt.provider.HmacJwtTokenProvider
 * </p>
 *
 * <p>
 * RS256 实现，使用 RSA 私钥签名、公钥验证
 * @see com.auth.common.jwt.provider.RsaJwtTokenProvider
 * </p>
 */
package com.auth.common.jwt.provider;