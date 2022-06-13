package com.changgou.seckill.task;


import com.alibaba.fastjson.JSON;
import com.changgou.seckill.dao.SeckillGoodsMapper;
import com.changgou.seckill.pojo.SeckillGoods;
import com.changgou.seckill.pojo.SeckillOrder;
import com.changgou.seckill.utils.SeckillStatus;
import entity.IdWorker;
import org.springframework.amqp.AmqpException;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.core.RedisTemplate;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class MultiThreadingCreateOrder {

    @Autowired
    private RedisTemplate redisTemplate;

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private SeckillGoodsMapper seckillGoodsMapper;

    @Autowired
    private IdWorker idWorker;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private Environment env;

    @Async
    public void createOrder() {
//        try {
//            System.out.println("开始查询下单相关业务，模拟业务处理时间.....");
//            Thread.sleep(2000);
//            System.out.println("完成查询下单相关业务，模拟业务处理时间.....");
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }

//        //便于测试我们这里的参数先写死
//        //时间区间
//        String time = "2019052510";
//        //用户登录名
//        String username="zhangsan";
//        //用户抢购商品
//        Long id = 1131814847898587136L;

        //从队列中获取排队信息-左进右出
        SeckillStatus seckillStatus = (SeckillStatus) redisTemplate.boundListOps("SeckillOrderQueue").rightPop();


//        //超卖方式一：队列-只能处理超卖问题，不能解决正确库存数显示问题
//        Object sgoods = redisTemplate.boundListOps("SeckillGoodsCountList_" + seckillStatus.getGoodsId()).rightPop();
//        //如果队列没数据，说明没有库存了
//        if(sgoods == null){
//            //清理排队标示
//            redisTemplate.boundHashOps("UserQueueCount").delete(seckillStatus.getUsername());
//            //清理抢单标示
//            redisTemplate.boundHashOps("UserQueueStatus").delete(seckillStatus.getUsername());
//            return;
//        }

        //超卖方式二：库存精确显示-自减
        Long count = redisTemplate.boundHashOps("SeckillGoodsCount").increment(seckillStatus.getGoodsId(), -1);


        //发送延时消息到MQ中
        sendTimerMessage(seckillStatus);


        //如果有排队信息
        if (seckillStatus != null) {
            //时间区间
            String time = seckillStatus.getTime();
            //用户登录名
            String username = seckillStatus.getUsername();
            //用户抢购商品
            Long id = seckillStatus.getGoodsId();

            //1.获取商品数据
            SeckillGoods goods = (SeckillGoods) redisTemplate.boundHashOps("SeckillGoods_" + time).get(id);

            //2.如果没有库存，则直接抛出异常
            if (goods == null ||count <=0 ) {
                throw new RuntimeException("你来晚了一步，商品已抢购一空!");
            }
            //3.如果有库存，则创建秒杀商品订单
            SeckillOrder seckillOrder = new SeckillOrder();
            seckillOrder.setId(idWorker.nextId());
            seckillOrder.setSeckillId(id);
            seckillOrder.setMoney(goods.getCostPrice());
            seckillOrder.setUserId(username);
            seckillOrder.setCreateTime(new Date());
            seckillOrder.setStatus("0");
            //将秒杀订单存入到Redis中
            redisTemplate.boundHashOps("SeckillOrder").put(username, seckillOrder);

            //4.扣减库存
            goods.setStockCount(count.intValue());

            //5.判断当前商品是否还有库存
            if (count <= 0) {
                //并且将商品数据同步到MySQL中
                seckillGoodsMapper.updateByPrimaryKeySelective(goods);
                //如果没有库存,则清空Redis缓存中该商品
                redisTemplate.boundHashOps("SeckillGoods_" + time).delete(id);
            } else {
                //如果有库存，则将扣减库存后的goods重新放入redis
                redisTemplate.boundHashOps("SeckillGoods_" + time).put(id, goods);
            }

            //抢单成功，更新抢单状态,排队->等待支付
            seckillStatus.setStatus(2);  //1:排队中，2:秒杀等待支付,3:支付超时，4:秒杀失败,5:支付完成
            seckillStatus.setOrderId(seckillOrder.getId());  //更新订单id
            seckillStatus.setMoney(new Float(seckillOrder.getMoney()));  //记录金额
            //更新用户订单排队信息为等待支付
            redisTemplate.boundHashOps("UserQueueStatus").put(username, seckillStatus);
        }


    }

    /***
     * 发送延时消息到RabbitMQ中
     * @param seckillStatus
     */
    public void sendTimerMessage(SeckillStatus seckillStatus){
       rabbitTemplate.convertAndSend(env.getProperty("mq.pay.queue.seckillordertimerdelay"), (Object) JSON.toJSONString(seckillStatus), new MessagePostProcessor() {
           @Override
           public Message postProcessMessage(Message message) throws AmqpException {
               message.getMessageProperties().setExpiration("10000");
               return message;
           }
       });
    }

}
