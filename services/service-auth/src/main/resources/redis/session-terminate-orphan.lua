-- KEYS[1]=session hash
-- ARGV[1]=jti（保留占位，便于脚本签名与 terminate 一致）
redis.call('DEL', KEYS[1])
return 1
