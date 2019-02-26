package com.pinyougou.task;

import com.pinyougou.mapper.TbSeckillGoodsMapper;
import com.pinyougou.pojo.TbSeckillGoods;
import com.pinyougou.pojo.TbSeckillGoodsExample;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

@Component
public class SeckillTask {


    @Autowired
    private TbSeckillGoodsMapper seckillGoodsMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    @Scheduled(cron="0/10 * * * * ?")
    public void refreshSeckillGoods(){
        System.out.println("秒杀商品增量更新，放入缓存中没有的数据");

        //查询缓存中的秒杀商品ID集合
        List goodsIdList =new ArrayList(redisTemplate.boundHashOps("seckillGoods").keys()) ;


        TbSeckillGoodsExample example = new TbSeckillGoodsExample();
        TbSeckillGoodsExample.Criteria criteria = example.createCriteria();
        //审核通过的商品
        criteria.andStatusEqualTo("1");
        //库存大于0
        criteria.andStockCountGreaterThan(0);
        //开始日期
        criteria.andStartTimeLessThanOrEqualTo(new Date());
        //结束日期
        criteria.andEndTimeGreaterThan(new Date());
        //排除缓存中已经存在的商品ID集合
        if(goodsIdList.size()>0){
            criteria.andIdNotIn(goodsIdList);
        }

        List<TbSeckillGoods> tbSeckillGoodsList = seckillGoodsMapper.selectByExample(example);
        //将列表数据装入缓存
        for (TbSeckillGoods seckillGoods:tbSeckillGoodsList){
            redisTemplate.boundHashOps("seckillGoods").put(seckillGoods.getId(),seckillGoods);
            System.out.println("增量更像秒杀商品Id:"+tbSeckillGoodsList);
        }
    }

    /**
     * 移除缓存中的商品
     */
    @Scheduled(cron="* * * * * ?")
    public  void removeSeckillGoods(){
        //查询处缓存中的数据，扫描每条记录，判断时间，如果当前时间超过了截至时间，移除数据
        List<TbSeckillGoods> seckillGoodsList = redisTemplate.boundHashOps("seckillGoods").values();
        System.out.println("执行了清除秒杀商品的任务");
        for(TbSeckillGoods seckillGoods :seckillGoodsList){
            if(seckillGoods.getEndTime().getTime() < new Date().getTime()){

                //同步数据库
                seckillGoodsMapper.updateByPrimaryKey(seckillGoods);

                //清除缓存
                redisTemplate.boundHashOps("seckillGoods").delete(seckillGoods.getId());
                System.out.println("秒杀商品"+seckillGoods.getId()+"已过期");
            }
        }
        System.out.println("执行秒杀商品任务……end");
    }

}
