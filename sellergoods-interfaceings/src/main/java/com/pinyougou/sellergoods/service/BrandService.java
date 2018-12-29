package com.pinyougou.sellergoods.service;

import com.pinyougou.pojo.TbBrand;
import entity.PageResult;

import java.util.List;
import java.util.Map;

public interface BrandService {

    List<TbBrand> findAll();

    PageResult  findPage(int num,int pageSize);

    void add(TbBrand tbBrand);

    void update(TbBrand tbBrand);

    TbBrand findOne(long id);

    void delete(long[] ids);

    PageResult findPage(TbBrand tbBrand,int pageNum,int pageSize);

    /**
     * 品牌下拉框数据
     */
    List<Map> selectOptionList();
}
