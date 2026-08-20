-- KEYS[1]=session hash, KEYS[2]=user sessions set
-- ARGV[1]=jti
redis.call('DEL', KEYS[1])
redis.call('SREM', KEYS[2], ARGV[1])
return 1
