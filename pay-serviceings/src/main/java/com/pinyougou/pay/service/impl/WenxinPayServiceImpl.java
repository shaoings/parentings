package com.pinyougou.pay.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.wxpay.sdk.WXPayUtil;
import org.springframework.beans.factory.annotation.Value;
import util.HttpClient;

import java.util.HashMap;
import java.util.Map;

@Service
public class WenxinPayServiceImpl implements WexinPayService{

    @Value("${appid}")
    private String appid;

    @Value("${partner}")
    private String partner;

    @Value("${partnerkey}")
    private String partnerkey;

    @Value("${notifyurl}")
    private String notifyurl;

    @Override
    public Map createNative(String out_trade_no, String total_fee) {
        //1.参数封装
        Map param = new HashMap();
        //公众号ID
        param.put("appid",appid);
        //商户
        param.put("mch_id",partner);
        //随机字符串
        param.put("nonce_str", WXPayUtil.generateNonceStr());
        //签名会自动产生，调用工具类
        //商品描述
        param.put("body","品优购");
        //商户订单号(这里调用的是日志的订单编号),这样就能将不同商家的订单合成一条支付
        param.put("out_trade_no",out_trade_no);
        //标价金额,以分为单位
        param.put("total_fee",total_fee);
        //终端IP
        param.put("spbill_create_ip","127.0.0.1");
        //通知地址(回调地址)
        param.put("notify_url",notifyurl);
        param.put("trade_type","NATIVE");

        try {
            //partnerkey商户key
            String paramXML = WXPayUtil.generateSignedXml(param,partnerkey);
            System.out.println("paramXML:"+paramXML);


            //2.发送请求() 统一下单地址
            HttpClient httpClient = new HttpClient("https://api.mch.weixin.qq.com/pay/unifiedorder");
            httpClient.setHttps(true);
            httpClient.setXmlParam(paramXML);
            httpClient.post();
            //3.获取结果

            String xmlResult = httpClient.getContent();
            System.out.println("xmlResutl:"+xmlResult);

            Map<String,String> mapResult = WXPayUtil.xmlToMap(xmlResult);
            Map map = new HashMap();
            //生成支付二维码链接
            map.put("code_url",mapResult.get("code_url"));
            map.put("out_trade_no",out_trade_no);
            map.put("total_fee",total_fee);
            System.out.println("二维码信息:"+map);
            //前端根据map信息生成二维码
            return map;
        } catch (Exception e) {
            e.printStackTrace();
            return new HashMap();
        }
    }

    @Override
    public Map queryPayStatus(String out_trade_no) {
        //1.封装参数
        Map param = new HashMap();
        param.put("appid",appid);
        param.put("mch_id",partner);
        param.put("out_trade_no",out_trade_no);
        param.put("nonce_str",WXPayUtil.generateNonceStr());
        try {
            String paramXML = WXPayUtil.generateSignedXml(param,partnerkey);
            //2.发送请求
            HttpClient httpClient = new HttpClient("https://api.mch.weixin.qq.com/pay/orderquery");
            httpClient.setHttps(true);
            httpClient.setXmlParam(paramXML);
            httpClient.post();

            //3.获取结果
            String xmlResult = httpClient.getContent();
            System.out.println("xmlResutl:"+xmlResult);

            Map<String,String> mapResult = WXPayUtil.xmlToMap(xmlResult);
            System.out.println("查询调用查询api,返回结果:"+mapResult);

            return mapResult;
        } catch (Exception e) {
            e.printStackTrace();
        }



        return null;
    }

    @Override
    public Map closePay(String out_trade_no) {
        //1.封装参数
        Map param = new HashMap();
        param.put("appid",appid);
        param.put("mch_id",partner);
        param.put("out_trade_no",out_trade_no);
        param.put("nonce_str",WXPayUtil.generateNonceStr());
        String url="https://api.mch.weixin.qq.com/pay/closeorder";
        try {
            String paramXML = WXPayUtil.generateSignedXml(param,partnerkey);
            //2.发送请求
            HttpClient httpClient = new HttpClient(url);
            httpClient.setHttps(true);
            httpClient.setXmlParam(paramXML);
            httpClient.post();

            //3.获取结果
            String xmlResult = httpClient.getContent();
            System.out.println("xmlResutl:"+xmlResult);

            Map<String,String> mapResult = WXPayUtil.xmlToMap(xmlResult);
            System.out.println("查询调用查询api,返回结果:"+mapResult);

            return mapResult;
        } catch (Exception e) {
            e.printStackTrace();
        }



        return null;
    }
}
