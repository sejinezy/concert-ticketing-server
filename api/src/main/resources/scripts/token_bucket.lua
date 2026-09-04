local key = KEYS[1]

local capacity = tonumber(ARGV[1])
local refillRate = tonumber(ARGV[2])
local requestedTokens = tonumber(ARGV[3])
local nowMillis = tonumber(ARGV[4])

-- 현재 bucket 상태 조회
local data = redis.call(
    'HMGET',
    key,
    'tokens',
    'lastRefillTime'
)

local tokens = tonumber(data[1])
local lastRefillTime = tonumber(data[2])

-- 최초 요청이면 bucket을 가득 채운 상태로 시작
if tokens == nil then
    tokens = capacity
end

if lastRefillTime == nil then
    lastRefillTime = nowMillis
end

-- 마지막 refill 이후 경과 시간(ms → second)
local elapsedSeconds = math.max(
    0,
    nowMillis - lastRefillTime
) / 1000

-- 경과 시간만큼 token refill
local refillTokens = elapsedSeconds * refillRate

tokens = math.min(
    capacity,
    tokens + refillTokens
)

local allowed = 0

-- 요청에 필요한 token이 있으면 차감
if tokens >= requestedTokens then
    tokens = tokens - requestedTokens
    allowed = 1
end

-- 현재 상태 저장
redis.call(
    'HSET',
    key,
    'tokens',
    tostring(tokens)
)

redis.call(
    'HSET',
    key,
    'lastRefillTime',
    tostring(nowMillis)
)

return allowed