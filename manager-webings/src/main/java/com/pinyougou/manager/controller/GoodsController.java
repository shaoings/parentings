package com.pinyougou.manager.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.pinyougou.pojo.TbGoods;
import com.pinyougou.pojo.TbItem;

import com.pinyougou.sellergoods.service.GoodsService;
import entity.Goods;
import entity.PageResult;
import entity.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import java.util.List;

/**
 * controller
 * @author Administrator
 *
 */
@RestController
@RequestMapping("/goods")
public class GoodsController {

	@Reference
	private GoodsService goodsService;

	/*@Reference(timeout = 100000)
	private ItemSearchService itemSearchService;*/

	//用于导入solr索引库的消息（点对点）
	@Autowired
	private Destination queueSolrDeleteDestination;

	//用于生成商品详细页的消息目标（发布订阅）
	@Autowired
	private Destination topicPageDestination;

	@Autowired
	private  Destination topicPageDeleteDestination;
	/**
	 * 返回全部列表
	 * @return
	 */
	@RequestMapping("/findAll")
	public List<TbGoods> findAll(){			
		return goodsService.findAll();
	}
	
	
	/**
	 * 返回全部列表
	 * @return
	 */
	@RequestMapping("/findPage")
	public PageResult  findPage(int page,int rows){			
		return goodsService.findPage(page, rows);
	}
	
	/**
	 * 增加
	 * @param goods
	 * @return
	 */
	@RequestMapping("/add")
	public Result add(@RequestBody(required = false) Goods goods){
		//获取登录名
		String sellerId = SecurityContextHolder.getContext().getAuthentication().getName();
		try {
			//设置商家Id
			goods.getGoods().setSellerId(sellerId);
			goodsService.add(goods);
			return new Result(true, "增加成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "增加失败");
		}
	}
	
	/**
	 * 修改
	 * @param goods
	 * @return
	 */
	@RequestMapping("/update")
	public Result update(@RequestBody Goods goods){
		try {
			goodsService.update(goods);
			return new Result(true, "修改成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "修改失败");
		}
	}	
	
	/**
	 * 获取实体
	 * @param id
	 * @return
	 */
	@RequestMapping("/findOne")
	public Goods findOne(Long id){
		return goodsService.findOne(id);		
	}




	/**
	 * 批量删除
	 * @param ids
	 * @return
	 */
	@RequestMapping("/delete")
	public Result delete(final Long [] ids){
		try {
			goodsService.delete(ids);
			//从索引中删除
			//itemSearchService.deleteByGoodsIds(Arrays.asList(ids));
			jmsTemplate.send(queueSolrDeleteDestination, new MessageCreator() {
				@Override
				public Message createMessage(Session session) throws JMSException {
					return session.createObjectMessage(ids);
				}
			});

			//删除每个服务器上的商品详细页
			jmsTemplate.send(topicPageDeleteDestination, new MessageCreator() {
				@Override
				public Message createMessage(Session session) throws JMSException {
					return session.createObjectMessage(ids);
				}
			});

			return new Result(true, "删除成功");

		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "删除失败");
		}
	}
	
		/**
	 * 查询+分页
	 * @param
	 * @param page
	 * @param rows
	 * @return
	 */
	@RequestMapping("/search")
	public PageResult search(@RequestBody TbGoods goods, int page, int rows  ){
		//从springSecurity 中获取商家ID
		//String sellerId = SecurityContextHolder.getContext().getAuthentication().getName();
		//goods.setSellerId(sellerId);

		return goodsService.findPage(goods, page, rows);
	}

	@Autowired
	private JmsTemplate jmsTemplate;
	@Autowired
	private Destination queueSolrDestination;
	/**
	 * 更新状态
	 * @param ids
	 * @param status
	 */
	@RequestMapping("/updateStatus")
	public Result updateStatus(Long[] ids, String status){
		try {
			goodsService.updateStatus(ids, status);
			//如果是审核通过
			if("1".equals(status)){

				//1.导入到索引库
				//得到需要导入的
				List<TbItem> itemList = goodsService.findItemListByGoodsIdandStatus(ids,status);
				//导入到solr
				//itemSearchService.importList(itemList);
				//转换为json
				final String jsonString = JSON.toJSONString(itemList);
				System.out.println("jsonString:"+jsonString);
				jmsTemplate.send(queueSolrDestination, new MessageCreator() {
					@Override
					public Message createMessage(Session session) throws JMSException {

						return session.createTextMessage(jsonString);
					}
				});

				//2.生成商品详细页
				for(final long goodsId:ids){
				//itemPageService.genItemHtml(goodsId);
				//通过activeMQ解除耦合
					jmsTemplate.send(topicPageDestination, new MessageCreator() {
						@Override
						public Message createMessage(Session session) throws JMSException {

							return session.createTextMessage(goodsId+"");
						}
					});
				}
			}

			return new Result(true, "成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "失败");
		}
	}

	/*@Reference(timeout = 40000)
	private ItemPageService itemPageService;

	@RequestMapping("/genHtml")
	public void getHtml(Long goodsId){
		itemPageService.genItemHtml(goodsId);
	}*/
	
}
