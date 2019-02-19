package com.pinyougou.cart.controller;


import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.pinyougou.cart.service.CartService;
import com.pinyougou.pojo.group.Cart;
import entity.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@RestController
@RequestMapping("/cart")
public class CartController {

    @Reference(timeout = 6000)
    private CartService cartService;

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private HttpServletResponse response;

    @RequestMapping("/findCartList")
    public List findCartList(){

        //获取当前登陆人
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        System.out.println("当前登陆人:"+name);
        //从cookie中提取购物车
        System.out.println("从cookie中提取购物车");
        String cartListString = util.CookieUtil.getCookieValue(request,"cartList","UTF-8");
        if(cartListString == null ||cartListString.equals("")){
            cartListString="[]";
        }
        List<Cart> cartList_cookie = JSON.parseArray(cartListString, Cart.class);
        //anonymousUser security内置用户 代表未登录
        if(name.equals("anonymousUser")){

            return cartList_cookie;
        }else{
            //已登录

            //合并购物车
            System.out.println("从redis中提取数据");
            List<Cart> cartList_redis = cartService.findCartListFromRedis(name);
            //cookie中有购物车合并
            if(cartList_cookie.size() > 0){
                List<Cart> cartList = cartService.mergeCartList(cartList_cookie,cartList_redis);

                //重新存入redis
                cartService.saveCartListToRedis(name,cartList);
                //清除cookie
                util.CookieUtil.deleteCookie(request,response,"cartList");
                return cartList;
            }

            return cartList_redis;

        }



    }

    @RequestMapping("/addGoodsToCartList")
    //跨域调用的注解 spring 4.2 版本后
    //@CrossOrigin(origins="http://localhost:9105",allowCredentials="true")
    public Result addGoodsToCartList(Long itemId,Integer num){
        try{
            //可以访问的域 * 代替所有域都可以访问 (当此方法不需要操作cookie)
            response.setHeader("Access-Control-Allow-Origin","http://localhost:9105");
            //如果操作cookie ，必须加上 这句话 并且上面不能写 *
            response.setHeader("Access-Control-Allow-Credentials", "true");

            //获取当前登陆人
            String name = SecurityContextHolder.getContext().getAuthentication().getName();
            System.out.println("当前登陆人:"+name);
            //从cookie中提取购物车(如果是刚开始没登陆 添加购物车后在登陆 原先的购物车信息也还是从cookie中取)
            List<Cart> cartList = findCartList();
            //调用服务方法操作购物车
            cartList = cartService.addGoodsToCartList(cartList,itemId,num);
            //未登陆
            if(name.equals("anonymousUser")){

                //将新的购物车存入cookie
                String cartListString = JSON.toJSONString(cartList);
                util.CookieUtil.setCookie(request,response,"cartList",cartListString,3600*24,"UTF-8");
            }else{
                cartService.saveCartListToRedis(name,cartList);
            }

            return new Result(true,"购物车成功");
        }catch (Exception e){
            e.printStackTrace();
            return new Result(false,"存入购物车失败");
        }


    }
}
