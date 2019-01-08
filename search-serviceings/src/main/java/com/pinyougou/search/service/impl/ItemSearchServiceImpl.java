package com.pinyougou.search.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.search.service.ItemSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.Criteria;
import org.springframework.data.solr.core.query.Query;
import org.springframework.data.solr.core.query.SimpleQuery;
import org.springframework.data.solr.core.query.result.ScoredPage;


import javax.crypto.Cipher;
import java.util.HashMap;
import java.util.Map;

@Service
public class ItemSearchServiceImpl implements ItemSearchService {

    @Autowired
    private SolrTemplate solrTemplate;

    @Override
    public Map<String, Object> search(Map searchMap) {
        Map<String,Object> map = new HashMap<>();
        Query query = new SimpleQuery("*:*");
        //查询条件
        Criteria criteria = new Criteria("item_keywords").is(searchMap.get("item_keywords"));
        //criteria = criteria.and("item_title").contains("s"); 添加条件
        query.addCriteria(criteria);

        //分页查询
        // query.setOffset(); 开始索引 默认为0
        // query.setRows();   每页记录数 默认为10
        ScoredPage<TbItem> page = solrTemplate.queryForPage(query,TbItem.class);
        return null;
    }
}
