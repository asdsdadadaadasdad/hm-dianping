



local userid=ARGV[1]
local voucherid=ARGV[2]
if(tonumber(redis.call('get','seccvoucher:stock:'..voucherid))<1) then
    return 1
end
if(redis.call('sismember','seccvoucher:buyer:'..voucherid,userid)==1) then
    return 2
end
redis.call('sadd','seccvoucher:buyer:'..voucherid,userid)
redis.call('incrby','seccvoucher:stock:'..voucherid,-1)
return 3