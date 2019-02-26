package com.pinyougou.seckill.controller;


import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pay.service.impl.WexinPayService;
import com.pinyougou.pojo.TbSeckillOrder;
import com.pinyougou.seckill.service.SeckillOrderService;
import entity.PageResult;
import entity.Result;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/pay")
public class PayController {

    @Reference
    private WexinPayService wenxinPayService;

    @Reference
    private SeckillOrderService seckillOrderService;

    @RequestMapping("/createNative")
    public Map createNative(){
        //1.获取当前登录用户
        String userName = SecurityContextHolder.getContext().getAuthentication().getName();
        //2.获取秒杀订单
        TbSeckillOrder tbSeckillOrder = seckillOrderService.searchOrderFromRedisByUserId(userName);
        //3.调用微信支付接口
        if(tbSeckillOrder != null){
            return wenxinPayService.createNative(tbSeckillOrder.getId()+"",(long)(tbSeckillOrder.getMoney().doubleValue()*100)+"");
        }else{
            return new HashMap();
        }
    }

    @RequestMapping("/queryPayStatus")
    public Result queryPayStatus(String out_trade_no){
        String userName = SecurityContextHolder.getContext().getAuthentication().getName();

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
                //保存订单
                seckillOrderService.saveOrderFromRedisToDb(userName,Long.valueOf(out_trade_no),map.get("transaction_id"));
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
                //关闭支付
                Map mapResult = wenxinPayService.closePay(out_trade_no);
                if(mapResult != null && "FAIL".equals( mapResult.get("return_code"))){
                    if("ORDERPAID".equals(mapResult.get("err_code"))){
                        result = new Result(true,"支付成功");
                        //保存订单
                        seckillOrderService.saveOrderFromRedisToDb(userName,Long.valueOf(out_trade_no),map.get("transaction_id"));

                    }
                }

                //删除订单
                seckillOrderService.deleteOrderFromRedis(userName,Long.valueOf(out_trade_no));
                System.out.println("删除成功");

            }
        }

        return result;
    }


}
