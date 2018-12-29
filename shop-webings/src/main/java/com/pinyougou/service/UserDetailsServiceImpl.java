package com.pinyougou.service;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pojo.TbSeller;
import com.pinyougou.sellergoods.service.SellerService;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.ArrayList;
import java.util.List;


/**
 * SpringSecutity的认证类(在springSecurity的配置文件中直接调用)
 */
public class UserDetailsServiceImpl implements UserDetailsService {

    //远程调用dubbox服务
    private SellerService sellerService;

    public void setSellerService(SellerService sellerService) {
        this.sellerService = sellerService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        System.out.println("经过了UserDetailsServiceImpl");
        //构架一个角色列表
        List<GrantedAuthority> grantAuths = new ArrayList();
        grantAuths.add(new SimpleGrantedAuthority("ROLE_SELLER"));//添加角色
        try{
        //得到商家对象
        TbSeller seller = sellerService.findOne(username);
        System.out.println(seller);
        if(seller != null){
            if(seller.getStatus().equals("1")){
                return new User(username,seller.getPassword(),grantAuths); //第三个参数代表该账号的角色集合
            }

        }
        }catch (Exception e){
            e.printStackTrace();
            throw  e;
        }
        return null;
    }
}
