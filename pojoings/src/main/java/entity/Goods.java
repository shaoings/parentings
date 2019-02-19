package entity;

import com.pinyougou.pojo.TbGoods;
import com.pinyougou.pojo.TbGoodsDesc;
import com.pinyougou.pojo.TbItem;

import java.io.Serializable;
import java.util.List;

public class Goods implements Serializable {

    /**
     *商品spu
     */
    private TbGoods goods;

    /**
     *商品扩展
     */
    private TbGoodsDesc goodsDesc;

    /**
     *商品sku
     */
    private List<TbItem> itemList;

    public TbGoods getGoods() {
        return goods;
    }

    public TbGoodsDesc getGoodsDesc() {
        return goodsDesc;
    }

    public void setGoodsDesc(TbGoodsDesc goodsDesc) {
        this.goodsDesc = goodsDesc;
    }

    public void setGoods(TbGoods goods) {
        this.goods = goods;
    }


    public List<TbItem> getItemList() {
        return itemList;
    }

    public void setItemList(List<TbItem> itemList) {
        this.itemList = itemList;
    }
}
