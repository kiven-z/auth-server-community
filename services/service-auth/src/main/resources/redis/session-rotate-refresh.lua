-- KEYS[1]=session hash
-- ARGV[1]=requestHash
-- ARGV[2]=newRefreshHash
-- ARGV[3]=newRefreshExpiresAtMs
-- ARGV[4]=ttlSeconds
-- ARGV[5]=nowMs
-- ARGV[6]=graceMs
-- ARGV[7]=accessToken
-- ARGV[8]=refreshToken
-- ARGV[9]=accessExpiresAtMs
-- return: 1=ROTATED, 2=REUSED, 0=EXPIRED/MISSING, -1=MISMATCH
local sessionKey = KEYS[1]
local requestHash = ARGV[1]
local newHash = ARGV[2]
local newExpiresAt = ARGV[3]
local ttlSeconds = tonumber(ARGV[4])
local nowMs = tonumber(ARGV[5])
local graceMs = tonumber(ARGV[6])

if redis.call('EXISTS', sessionKey) == 0 then
	return 0
end

local expiresAt = tonumber(redis.call('HGET', sessionKey, 'refreshTokenExpiresAt') or '0')
if expiresAt == nil or expiresAt <= nowMs then
	return 0
end

local currentHash = redis.call('HGET', sessionKey, 'refreshTokenHash')
if currentHash ~= false and currentHash ~= nil and currentHash ~= '' and currentHash == requestHash then
	redis.call('HSET', sessionKey, 'previousRefreshTokenHash', currentHash)
	redis.call('HSET', sessionKey, 'refreshTokenHash', newHash)
	redis.call('HSET', sessionKey, 'refreshTokenExpiresAt', newExpiresAt)
	redis.call('HSET', sessionKey, 'refreshRotatedAt', tostring(nowMs))
	redis.call('HSET', sessionKey,
		'lastAccessToken', ARGV[7],
		'lastRefreshToken', ARGV[8],
		'lastAccessExpiresAt', ARGV[9])
	if ttlSeconds ~= nil and ttlSeconds > 0 then
		redis.call('EXPIRE', sessionKey, ttlSeconds)
	end
	return 1
end

local previousHash = redis.call('HGET', sessionKey, 'previousRefreshTokenHash')
local rotatedAt = tonumber(redis.call('HGET', sessionKey, 'refreshRotatedAt') or '0')
local lastAccessToken = redis.call('HGET', sessionKey, 'lastAccessToken')
local lastRefreshToken = redis.call('HGET', sessionKey, 'lastRefreshToken')
local lastAccessExpiresAt = redis.call('HGET', sessionKey, 'lastAccessExpiresAt')
if previousHash ~= false and previousHash ~= nil and previousHash ~= ''
	and previousHash == requestHash
	and rotatedAt ~= nil and rotatedAt > 0
	and (nowMs - rotatedAt) <= graceMs
	and lastAccessToken ~= false and lastAccessToken ~= nil and lastAccessToken ~= ''
	and lastRefreshToken ~= false and lastRefreshToken ~= nil and lastRefreshToken ~= ''
	and lastAccessExpiresAt ~= false and lastAccessExpiresAt ~= nil and lastAccessExpiresAt ~= '' then
	return 2
end

return -1
