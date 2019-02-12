package com.pinyougou.page.service.impl;


import com.pinyougou.mapper.TbGoodsDescMapper;
import com.pinyougou.mapper.TbGoodsMapper;
import com.pinyougou.mapper.TbItemCatMapper;
import com.pinyougou.mapper.TbItemMapper;
import com.pinyougou.page.service.ItemPageService;
import com.pinyougou.pojo.*;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfig;

import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ItemPageServiceImpl implements ItemPageService {


    @Autowired
    /*
    spring对freemarker 支持类 在spring 的配置文件中
     */
    private FreeMarkerConfig freeMarkerConfig;

    @Value("${pagedir}")
    private  String pagedir;

    @Autowired
    private TbGoodsMapper goodsMapper;

    @Autowired
    private TbGoodsDescMapper tbGoodsDescMapper;

    @Autowired
    private TbItemCatMapper tbItemCatMapper;

    @Autowired
    private TbItemMapper tbItemMapper;
    @Override
    public boolean genItemHtml(Long goodsId) {
        Configuration configuration = freeMarkerConfig.getConfiguration();

        try {
            Template template = configuration.getTemplate("item.ftl");
            //创建数据模型
            Map dataModel = new HashMap<>();
            //1.商品主表数据
            TbGoods goods = goodsMapper.selectByPrimaryKey(goodsId);
            dataModel.put("goods",goods);
            //2.商品扩展表
            TbGoodsDesc goodsDesc = tbGoodsDescMapper.selectByPrimaryKey(goodsId);
            dataModel.put("goodsDesc",goodsDesc);
            //3.读取商品分类
            String itemCat1 = tbItemCatMapper.selectByPrimaryKey(goods.getCategory1Id()).getName();
            String itemCat2 = tbItemCatMapper.selectByPrimaryKey(goods.getCategory2Id()).getName();
            String itemCat3 = tbItemCatMapper.selectByPrimaryKey(goods.getCategory3Id()).getName();
            //4.sku列表
            TbItemExample example = new TbItemExample();
            TbItemExample.Criteria criteria = example.createCriteria();
            //状态为有效
            criteria.andStatusEqualTo("1");
            //指定SPU ID
            criteria.andGoodsIdEqualTo(goodsId);
            //按照状态降序，保证第一个为默认
            example.setOrderByClause("is_default desc");
            List<TbItem> itemList = tbItemMapper.selectByExample(example);

            dataModel.put("itemList",itemList);
            dataModel.put("itemCat1",itemCat1);
            dataModel.put("itemCat2",itemCat2);
            dataModel.put("itemCat3",itemCat3);

            Writer out = new FileWriter(pagedir+ File.separator+goodsId+".html");
            //输出
            template.process(dataModel,out);
            out.close();

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean deleteItemHtml(Long[] goodsId) {

        try {
            for (Long goodId:goodsId){
                new File(pagedir+goodId+".html").delete();
            }
            return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }

    }
}
