-- wrk Lua script for seckill hot path (high throughput on Linux)
-- wrk -t12 -c10000 -d60s -s load-test/wrk/seckill.lua http://localhost:21000/OrderServer/seckill/order

wrk.method = "POST"
wrk.headers["Content-Type"] = "application/json"

local counter = 0
local activityId = os.getenv("ACTIVITY_ID") or "1"
local host = os.getenv("WRK_HOST") or "localhost"

request = function()
  counter = counter + 1
  local userId = string.format("wrk-%s-%d-%d", host, id, counter)
  local body = string.format('{"activityId":%s,"userId":"%s","requestId":"%s"}', activityId, userId, userId)
  return wrk.format(nil, nil, nil, body)
end

response = function(status, headers, body)
  if status ~= 200 and status ~= 409 and status ~= 429 then
    print("unexpected status: " .. status)
  end
end
