package com.pinyougou.search.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.search.service.ItemSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.*;
import org.springframework.data.solr.core.query.result.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ItemSearchServiceImpl implements ItemSearchService {

    @Autowired
    private SolrTemplate solrTemplate;

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public Map<String, Object> search(Map searchMap) {
        Map<String,Object> map = new HashMap<>();
       /* Query query = new SimpleQuery("*:*");
        //查询条件
        Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
        //criteria = criteria.and("item_title").contains("s"); 添加条件
        query.addCriteria(criteria);
        TbItem item = new TbItem();

        //分页查询
        // query.setOffset(); 开始索引 默认为0
        // query.setRows();   每页记录数 默认为10
        ScoredPage<TbItem> page = solrTemplate.queryForPage(query,TbItem.class);
        map.put("rows",page.getContent());*/
        //1.查询列表
        map.putAll(searchList(searchMap));//查询出来的商品

        /****查询的条件:商品分类、品牌、规格****/
        //2.分组查询商品分类列表
        List<String> categoryList = searchCategoryLsit(searchMap);
        map.put("categoryList",categoryList);
        //3.查询品牌和规格列表
        String category = (String) searchMap.get("category");
        if(!category.equals("")){
            map.putAll(searchBrandAndSpecList(category));
        }else{
            if(categoryList.size()>0){
                map.putAll(searchBrandAndSpecList((categoryList.get(0))));//取第一个商品分类的规格和品牌
            }
        }





        return map;
    }

    /**
     * 根据关键字搜索列表
     * @param searchMap
     * @return
     */
    private Map searchList(Map searchMap){
        Map map = new HashMap();
        //高亮选项设置
        HighlightQuery  query =  new SimpleHighlightQuery();
        HighlightOptions highlightOptions = new HighlightOptions().addField("item_title");//设置高亮区域
        highlightOptions.setSimplePrefix("<em style='color:red'>");//高亮前缀
        highlightOptions.setSimplePostfix("</em>");//高亮后缀
        query.setHighlightOptions(highlightOptions);//设置高亮选项
        //1.1按照关键字查询
        Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
        query.addCriteria(criteria);

        //1.2按照商品分类筛选
        if(!"".equals(searchMap.get("category"))){//选择了商品分类
            FilterQuery filterQuery = new SimpleQuery();
            Criteria filterCriteria = new Criteria("item_category").is(searchMap.get("category"));
            filterQuery.addCriteria(filterCriteria);
            query.addFilterQuery(filterQuery);
        }
        //1.3 按照品牌删选
        if(!"".equals(searchMap.get("brand"))){//选择了品牌
            FilterQuery filterQuery = new SimpleQuery();
            Criteria filterCriteria = new Criteria("item_brand").is(searchMap.get("brand"));
            filterQuery.addCriteria(filterCriteria);
            query.addFilterQuery(filterQuery);
        }

        //1.4 按照规格删选
        if(!"{}".equals(searchMap.get("spec"))&&searchMap.get("spec") != null){//选择了规格
           Map<String,String> specMap = (Map<String,String>) searchMap.get("spec");
           for(String key : specMap.keySet()){
               FilterQuery filterQuery = new SimpleQuery();
               Criteria filterCriteria = new Criteria("item_spec_"+key).is(specMap.get(key));
               filterQuery.addCriteria(filterCriteria);
               query.addFilterQuery(filterQuery);
           }
        }


        /*****获取高亮结果集*****/
        //返回一个高亮页对象
        HighlightPage<TbItem> page = solrTemplate.queryForHighlightPage(query,TbItem.class);

        //高亮入口集合
        //List<HighlightEntry<TbItem>> entryList = page.getHighlighted();

        for(HighlightEntry<TbItem> h:page.getHighlighted()){ //循环高亮入口集合
            TbItem item = h.getEntity();//获取原实体类
            if(h.getHighlights().size()>0 && h.getHighlights().get(0).
                    getSnipplets().size()>0){
                item.setTitle(h.getHighlights().get(0).getSnipplets().get(0));//设置高亮的结果
            }

            //获取高亮列表（高亮域的个数）
            //List<HighlightEntry.Highlight> highlightList = h.getHighlights(); 每个域可能存在多值
        }
        map.put("rows",page.getContent());
        return map;
    }

    /**
     * 查询分类列表
     * @param searchMap
     * @return
     */
    private List searchCategoryLsit(Map searchMap){
        List<String> list = new ArrayList<>();
        Query query = new SimpleQuery("*:*");
        //按照关键字查询
        Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
        query.addCriteria(criteria);
        //设置分组选项
        GroupOptions groupOptions = new GroupOptions().addGroupByField("item_category");
        query.setGroupOptions(groupOptions);
        //得到分组页
        GroupPage<TbItem> page = solrTemplate.queryForGroupPage(query,TbItem.class);
        //根据列得到分组结果集
        GroupResult<TbItem> groupResult = page.getGroupResult("item_category");
        //得到分组结果入口页
        Page<GroupEntry<TbItem>> groupEntries = groupResult.getGroupEntries();
        //得到分组入口集合
        List<GroupEntry<TbItem>> content = groupEntries.getContent();
        for(GroupEntry<TbItem> entry:content){
            list.add(entry.getGroupValue());//将分组的结果的名称封装到返回值中
        }
        return list;
    }


    /**
     * 查询品牌和规格列表
     * category 根据商品分类名称 得到模版Id, 在得到 品牌和规格
     * @return
     */
    private Map searchBrandAndSpecList(String category){ //商品名称
        Map map = new HashMap();
        //1.根据商品分类名称得到模版ID
        Long templateId = (long)redisTemplate.boundHashOps("itemCat").get(category);

        if(templateId != null){
            //根据模版ID 获取品牌列表
            List brandList = (List) redisTemplate.boundHashOps("brandList").get(templateId);
            map.put("brandList", brandList);
            //根据模版ID 获取规格列表
            List specList = (List) redisTemplate.boundHashOps("specList").get(templateId);
            map.put("specList",specList);
        }

        return map;
    }


}
