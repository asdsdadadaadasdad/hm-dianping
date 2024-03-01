


local voucherid=ARGV[1]
local userid=ARGV[2]
local orderid=ARGV[3]
local stockKey='seccvoucher:stock:'..voucherid
local buyerKey='seccvoucher:buyer:'..voucherid
if(tonumber(redis.call('get',stockKey))<1) then
    return 1
end
if(redis.call('sismember',buyerKey,userid)==1) then
    return 2
end
redis.call('xadd','stream.orders','*','voucherId',voucherid,'userId',userid,'id',orderid)
redis.call('sadd',buyerKey,userid)
redis.call('incrby',stockKey,-1)
return 3
