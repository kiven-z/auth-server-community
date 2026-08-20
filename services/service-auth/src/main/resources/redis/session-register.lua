-- KEYS[1]=session hash, KEYS[2]=user sessions set, KEYS[3]=online users zset
-- ARGV[1]=jti, ARGV[2]=loginAt score, ARGV[3]=ttl seconds (0=skip EXPIRE), ARGV[4]=userId, ARGV[5..]=hash field/value pairs
local jti = ARGV[1]
local loginAt = tonumber(ARGV[2])
local ttlSeconds = tonumber(ARGV[3])
local userId = ARGV[4]

for i = 5, #ARGV, 2 do
	redis.call('HSET', KEYS[1], ARGV[i], ARGV[i + 1])
end

if ttlSeconds > 0 then
	redis.call('EXPIRE', KEYS[1], ttlSeconds)
end

redis.call('SADD', KEYS[2], jti)
redis.call('ZADD', KEYS[3], loginAt, userId)
return 1
