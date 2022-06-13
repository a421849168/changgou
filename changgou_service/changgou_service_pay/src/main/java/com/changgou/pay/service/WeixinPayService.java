package com.changgou.pay.service;

import java.util.Map;

public interface WeixinPayService {

    Map createNative(Map<String,String> paramMap);

    /**
     * 查询支付状态
     * @param out_trade_no 商户订单号
     * @return
     */
    public Map queryPayStatus(String out_trade_no);


    /**
     * 关闭支付
     * @param orderId
     * @return
     * @exception
     */
    Map<String,String> closePay(Long orderId) throws Exception;

}
