package com.auth.common.ip;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

/**
 * Client IP information.
 * <p>
 * Contract:
 * <ul>
 * <li>ipAddr: the raw IP string resolved from request headers / remote address.</li>
 * <li>ipRegion: the IP region text. For internal/private IPs, it is the fixed text
 * 内网IP.</li>
 * </ul>
 * </p>
 *
 * @author Bunny
 */
@Getter
@Setter
@Builder
public class IpInfo implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * Raw IP address string (IPv4/IPv6).
	 */
	private String ipAddr;

	/**
	 * IP region text. Internal IPs are always 内网IP.
	 */
	private String ipRegion;

}
