package com.pinyougou.cart.controller;


import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.order.service.OrderService;
import com.pinyougou.pay.service.impl.WexinPayService;
import com.pinyougou.pojo.TbPayLog;
import entity.Result;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import util.IdWorker;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/pay")
public class PayController {

    @Reference
    private WexinPayService wenxinPayService;

    @Reference
    private OrderService orderService;

    @RequestMapping("/createNative")
    public Map createNative(){
        //1.获取当前登录用户
        String userName = SecurityContextHolder.getContext().getAuthentication().getName();
        //2.获取支付日志（从缓存）
        TbPayLog payLog = orderService.searchPayLogFromRedis(userName);
        //3.调用微信支付接口
        if(payLog != null){
            return wenxinPayService.createNative(payLog.getOutTradeNo(),payLog.getTotalFee()+"");
        }else{
            return new HashMap();
        }
    }

    @RequestMapping("/queryPayStatus")
    public Result queryPayStatus(String out_trade_no){
        Result  result = null;
        int x=0;
        while(true){
            //调用查询
            Map<String,String> map = wenxinPayService.queryPayStatus(out_trade_no);
            System.out.println("查询支付结果:"+map);
            if(map == null){
                result = new Result(false,"支付发生错误");
                break;
            }

            if("SUCCESS".equals(map.get("trade_state"))){
                result = new Result(true,"支付成功");
                //修改订单状态
                orderService.updateOrderStatus(out_trade_no, map.get("transaction_id"));
                break;
            }

            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            x++;
            //限制超时时间
            if(x>=100){
                result = new Result(false,"二维码超时");
            }
        }

        return result;
    }


}
