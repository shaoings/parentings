package com.pinyougou.shop.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pojo.TbBrand;
import com.pinyougou.sellergoods.service.BrandService;
import entity.PageResult;
import entity.Result;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/brand")
public class BrandController {

    @Reference
    private BrandService brandService;


    @RequestMapping("/findAll")
    public List<TbBrand> findAll(){
       return  brandService.findAll();
    }

    @RequestMapping("/findPage")
    public PageResult findPage(int page, int rows){
        return brandService.findPage(page,rows);
    }

    @RequestMapping("/add")
    public Result add(@RequestBody TbBrand tbBrand){
        try {
            brandService.add(tbBrand);
            return new Result(true, "增加成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "增加失败");
        }

    }

    @RequestMapping("/update")
    public Result update(@RequestBody TbBrand tbBrand){
        try {
            brandService.update(tbBrand);
            return new Result(true, "修改成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "修改失败");
        }
    }

    @RequestMapping("/findOne")
    public TbBrand findOne(long id){
        return brandService.findOne(id);
    }

    @RequestMapping("/delete")
    public Result delete(long[] ids){
        try {

            brandService.delete(ids);
            return new Result(true, "删除成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "删除失败");
        }
    }

    //条件查询
    @RequestMapping("/search")
    public PageResult search(@RequestBody(required=false) TbBrand tbBrand,int page, int rows){
        return brandService.findPage(tbBrand,page,rows);
    }

    /**
     * 下拉选
     * @return
     */
    @RequestMapping("/selectOptionList")
    public List<Map> selectOptionList(){
        return brandService.selectOptionList();
    }


}
