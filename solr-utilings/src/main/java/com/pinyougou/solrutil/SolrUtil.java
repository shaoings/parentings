package com.pinyougou.solrutil;

import com.alibaba.fastjson.JSON;
import com.pinyougou.mapper.TbItemMapper;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojo.TbItemExample;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
//@component （把普通pojo实例化到spring容器中，相当于配置文件中的
//<bean id="" class=""/>）
public class SolrUtil {
    @Autowired
    private TbItemMapper tbItemMapper;

    @Autowired
    private SolrTemplate solrTemplate;

    public void impotItemData(){
        TbItemExample example = new TbItemExample();
        TbItemExample.Criteria criteria = example.createCriteria();
        criteria.andStatusEqualTo("1");//审核通过的才导入的
        List<TbItem> itemList = tbItemMapper.selectByExample(example);

        for (TbItem item:itemList){
            System.out.println(item.getId()+" " +item.getTitle()+" ");
            Map specMap = JSON.parseObject(item.getSpec(), Map.class);//从数据库中提取规格JSON字符串转换为map
            item.setSpecMap(specMap);
        }
        System.out.println("----结束----");

        solrTemplate.saveBeans(itemList);
        solrTemplate.commit();
    }

    public static void main(String[] args) {
        ApplicationContext context = new ClassPathXmlApplicationContext("classpath*:spring/applicationContext*.xml");
        SolrUtil solrUtil = (SolrUtil) context.getBean("solrUtil");
        solrUtil.impotItemData();//不能直接调用，要通过spring

    }
}
