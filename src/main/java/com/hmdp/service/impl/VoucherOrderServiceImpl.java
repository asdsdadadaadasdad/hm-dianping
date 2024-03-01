package com.hmdp.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.hmdp.dto.Result;
import com.hmdp.dto.UserDTO;
import com.hmdp.entity.SeckillVoucher;
import com.hmdp.entity.Voucher;
import com.hmdp.entity.VoucherOrder;
import com.hmdp.mapper.VoucherOrderMapper;
import com.hmdp.service.ISeckillVoucherService;
import com.hmdp.service.IVoucherOrderService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.service.IVoucherService;
import com.hmdp.utils.UserHolder;
import com.hmdp.utils.idcerator;
import com.hmdp.utils.ilock;
import com.hmdp.utils.ilockimpl;
import com.sun.deploy.net.HttpResponse;
import lombok.SneakyThrows;
import org.apache.coyote.Response;
import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.connection.stream.*;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
public class VoucherOrderServiceImpl extends ServiceImpl<VoucherOrderMapper, VoucherOrder>
        implements IVoucherOrderService, SmartInitializingSingleton, BeanFactoryAware {
    @Autowired
    ISeckillVoucherService iSeckillVoucherService;
    @Autowired
    IVoucherService iVoucherService;
    @Autowired
    idcerator idcerator;
    @Autowired
    StringRedisTemplate redisTemplate;
    @Autowired
    RedissonClient redissonClient;
    @Override
    public Result order(Long voucherId,HttpServletResponse response) {
        SeckillVoucher voucher = iSeckillVoucherService.getById(voucherId);
        if(voucher.getBeginTime().isAfter(LocalDateTime.now())){
            response.setStatus(401);
            return Result.fail("666");
        }
        if(voucher.getEndTime().isBefore(LocalDateTime.now())){
            response.setStatus(401);
            return Result.fail("666");
        }
        if(voucher.getStock()<1){
            response.setStatus(401);
            return Result.fail("他鰢没了");
        }
        UserDTO user = UserHolder.getUser();
        RLock lock = redissonClient.getLock(user.getId().toString());
        boolean islock = lock.tryLock();
        lock.lock();
//        redissonClient.getMultiLock(lock).lock();
//        ilockimpl lock1=new ilockimpl(user.getId().toString(),redisTemplate);
//        boolean islock1 = lock1.trylock();
        if(!islock){
            response.setStatus(401);
            return Result.fail("666");
        }
        try {
            VoucherOrderServiceImpl proxy = (VoucherOrderServiceImpl) AopContext.currentProxy();
            return proxy.createOrder(voucherId,response);
        }finally {
            lock.unlock();
        }

    }
    static final RedisScript<Long> seccScript;
    static final RedisScript<Long> seccScript1;
    static{
        seccScript=new DefaultRedisScript<>();
        ((DefaultRedisScript<Long>) seccScript).setResultType(Long.class);
        ((DefaultRedisScript<Long>) seccScript).setLocation(new ClassPathResource("secc.lua"));
        seccScript1=new DefaultRedisScript<>();
        ((DefaultRedisScript<Long>) seccScript1).setResultType(Long.class);
        ((DefaultRedisScript<Long>) seccScript1).setLocation(new ClassPathResource("seccorder.lua"));


    }
    @Override
    public Result order2(Long voucherId, HttpServletResponse response) {
        UserDTO user = UserHolder.getUser();
        Long res=redisTemplate.execute(seccScript, Collections.emptyList(),user.getId().toString(),voucherId.toString());
        System.out.println(res);
        if(res!=3){
            return Result.fail("gun");
        }
        VoucherOrder order=new VoucherOrder();
        order.setUserId(user.getId());
        order.setVoucherId(voucherId);
        order.setId(idcerator.nextid("secc:order:"));
        orderqueue.add(order);
        return Result.ok("没毛可以");
    }

    @Transactional
    public synchronized Result createOrder(Long voucherId, HttpServletResponse response) {
        UserDTO user = UserHolder.getUser();
        int count = query().eq("user_id",user.getId()).eq("voucher_id", voucherId).count();
        if(count>0){
            response.setStatus(401);
            return Result.fail("你他鰢买过了");
        }
        boolean succ = iSeckillVoucherService.update().setSql("stock=stock-1").eq(
                "voucher_id", voucherId
        ).gt("stock",0).update();
        if(!succ){
            response.setStatus(401);
            return Result.fail("666");
        }
        VoucherOrder order=new VoucherOrder();
        order.setUserId(user.getId());
        order.setVoucherId(voucherId);
        order.setId(idcerator.nextid("secc:order:"));
        save(order);
        return Result.ok("没毛");
    }

    Executor executor= Executors.newSingleThreadExecutor();
    BlockingQueue<VoucherOrder> orderqueue=new ArrayBlockingQueue<>(1000);
    VoucherOrderServiceImpl proxy;

    @PostConstruct
    void init(){
        executor.execute(()->{
            VoucherOrder order = null;
            while (true){
                try {
                    order = orderqueue.take();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                createorder1(order);
            }
        });
        executor1.execute(new voucherTask());

    }
    static final String queue_name="stream.orders";

    @Override
    public void afterSingletonsInstantiated() {
        proxy=beanFactory.getBean(VoucherOrderServiceImpl.class);
    }
    BeanFactory beanFactory;
    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory=beanFactory;
    }

    class voucherTask implements Runnable{


        @SneakyThrows
        @Override
        public void run() {
            while (true){
                List<MapRecord<String, Object, Object>> records = redisTemplate.opsForStream().read(
                        Consumer.from("g1", "c1"),
                        StreamReadOptions.empty().count(1),
                        StreamOffset.create(queue_name, ReadOffset.lastConsumed())
                );
                if(records.isEmpty()){
                    Thread.sleep(50);
                    continue;
                }
                MapRecord<String, Object, Object> record = records.get(0);
                Map<Object, Object> map = record.getValue();
                VoucherOrder order = BeanUtil.fillBeanWithMap(map, new VoucherOrder(), false);
                try {
                    proxy.createorder1(order);
                    redisTemplate.opsForStream().acknowledge(queue_name,"g1",record.getId());
                    System.out.println("成功");
                }catch (Exception e){
                    System.out.println(e);
                    handlePending();
                }
            }
        }

        private void handlePending() {
            while (true){
                List<MapRecord<String, Object, Object>> records = redisTemplate.opsForStream().read(
                        Consumer.from("g1", "c1"),
                        StreamReadOptions.empty().count(1),
                        StreamOffset.create(queue_name, ReadOffset.from("0"))
                );
                MapRecord<String, Object, Object> record = records.get(0);
                Map<Object, Object> map = record.getValue();
                VoucherOrder order = BeanUtil.fillBeanWithMap(map, new VoucherOrder(), false);
                try {
                    proxy.createorder1(order);
                    redisTemplate.opsForStream().acknowledge(queue_name,"g1",record.getId());
                    break;
                }catch (Exception e){
                    System.out.println(e);
                }
            }
        }
    }
    @Transactional
    public void createorder1(VoucherOrder order) {
        iSeckillVoucherService.update().setSql("stock=stock-1").eq(
                "voucher_id", order.getVoucherId()
        ).update();
        save(order);
    }

    Executor executor1= Executors.newSingleThreadExecutor();

    @Override
    public Result order3(Long voucherId, HttpServletResponse response) {
        UserDTO user = UserHolder.getUser();
        Long res=redisTemplate.execute(seccScript1, Collections.emptyList(),
                voucherId.toString(),user.getId().toString(),
                new Long(idcerator.nextid("secc:order:")).toString());
        if(res!=3){

            return Result.fail("gun");
        }
        return Result.ok("没毛可以");
    }

}













