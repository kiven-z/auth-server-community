package com.auth.common.jwt.key;

import java.security.PrivateKey;
import java.security.PublicKey;

/**
 * RSA 签名密钥对 记录 RSA 密钥对（私钥 + 公钥）
 *
 * @author Bunny
 */
public record RsaKeyPairMaterial(PrivateKey privateKey, PublicKey publicKey) {
}
