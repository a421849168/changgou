package com.changgou;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

@SpringBootApplication(exclude={DataSourceAutoConfiguration.class})
@EnableEurekaClient
public class WeixinPayApplication {

    @Autowired
    private Environment env;

    public static void main(String[] args) {
        SpringApplication.run(WeixinPayApplication.class,args);
    }


    //创建交换机
    @Bean
    public DirectExchange orderExchange(){
        return new DirectExchange(env.getProperty("mq.pay.exchange.order"), true, false);
    }


    /****************************普通订单队列与路由key*********************************/
    //创建队列
    @Bean
    public Queue orderQueue(){
        return new Queue(env.getProperty("mq.pay.queue.order"));
    }
    /**
     * 队列绑定交换机
     * @Qualifier 可以指定spring bean的id,默认情况下id就是方法名
     * @return
     */
    @Bean
    public Binding bindingOrder(@Qualifier("orderQueue") Queue orderQueue, DirectExchange orderExchange){
        return BindingBuilder.bind(orderQueue).to(orderExchange).with(env.getProperty("mq.pay.routing.key"));
    }


    /****************************秒杀订单队列与路由key*********************************/
    //创建队列
    @Bean
    public Queue seckillQueue(){
        return new Queue(env.getProperty("mq.pay.queue.seckillorder"));
    }
    /**
     * 队列绑定交换机
     * @Qualifier 可以指定spring bean的id,默认情况下id就是方法名
     * @return
     */
    @Bean
    public Binding bindingSeckillOrder(@Qualifier("seckillQueue") Queue seckillQueue, DirectExchange orderExchange){
        return BindingBuilder.bind(seckillQueue).to(orderExchange).with(env.getProperty("mq.pay.routing.seckillkey"));
    }




    //创建交换机
    @Bean
    public DirectExchange basicExchange(){
        return new DirectExchange(env.getProperty("mq.pay.exchange.order"), true, false);
    }

    /**
     * 到期数据队列
     * @return
     */
    @Bean
    public Queue seckillOrderTimerQueue() {
        return new Queue(env.getProperty("mq.pay.queue.seckillordertimer"), true);
    }


    /**
     * 超时数据队列
     * @return
     */
    @Bean
    public Queue delaySeckillOrderTimerQueue() {
        return QueueBuilder.durable(env.getProperty("mq.pay.queue.seckillordertimerdelay"))
                .withArgument("x-dead-letter-exchange", env.getProperty("mq.pay.exchange.order"))        // 消息超时进入死信队列，绑定死信队列交换机
                .withArgument("x-dead-letter-routing-key", env.getProperty("mq.pay.queue.seckillordertimer"))   // 绑定指定的routing-key
                .build();
    }

    /***
     * 交换机与队列绑定
     * @return
     */
    @Bean
    public Binding basicBinding() {
        return BindingBuilder.bind(seckillOrderTimerQueue())
                .to(basicExchange())
                .with(env.getProperty("mq.pay.queue.seckillordertimer"));
    }

}
