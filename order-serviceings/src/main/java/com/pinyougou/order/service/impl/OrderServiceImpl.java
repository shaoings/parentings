package com.pinyougou.order.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.mapper.TbOrderItemMapper;
import com.pinyougou.mapper.TbOrderMapper;
import com.pinyougou.mapper.TbPayLogMapper;
import com.pinyougou.order.service.OrderService;
import com.pinyougou.pojo.TbOrder;
import com.pinyougou.pojo.TbOrderExample;
import com.pinyougou.pojo.TbOrderExample.Criteria;
import com.pinyougou.pojo.TbOrderItem;
import com.pinyougou.pojo.TbPayLog;
import com.pinyougou.pojo.group.Cart;
import entity.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;
import util.IdWorker;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 服务实现层
 * @author Administrator
 *
 */
@Service
@Transactional
public class OrderServiceImpl implements OrderService {

	@Autowired
	private TbOrderMapper orderMapper;


	@Autowired
	private TbPayLogMapper tbPayLogMapper;

	
	/**
	 * 查询全部
	 */
	@Override
	public List<TbOrder> findAll() {
		return orderMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbOrder> page=   (Page<TbOrder>) orderMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}


	@Autowired
	private RedisTemplate redisTemplate;


	@Autowired
	private IdWorker idWorker;


	@Autowired
	private TbOrderItemMapper orderItemMapper;
	/**
	 * 增加
	 */
	@Override
	public void add(TbOrder order) {

		//1.从redis中提取购物车列表
		List<Cart> cartList = (List<Cart>) redisTemplate.boundHashOps("cartList").get(order.getUserId());

		List<String> orderIdList = new ArrayList<>();
		//总金额
		double total_money = 0;
		//2.循环购物车列表添加订单
		for(Cart cart:cartList){
			TbOrder tbOrder = new TbOrder();
			long orderId = idWorker.nextId();
			orderIdList.add(orderId+"");

			tbOrder.setOrderId(orderId);
			//合计金额
			//tbOrder.setPayment();

			//用户名
			tbOrder.setUserId(order.getUserId());
			//支付类型
			tbOrder.setPaymentType(order.getPaymentType());
			//状态：未付款
			tbOrder.setStatus("1");
			//订单创建日期
			tbOrder.setCreateTime(new Date());
			//订单更新日期
			tbOrder.setUpdateTime(new Date());
			//地址
			tbOrder.setReceiverAreaName(order.getReceiverAreaName());
			//手机号
			tbOrder.setReceiverMobile(order.getReceiverMobile());
			//收货人
			tbOrder.setReceiver(order.getReceiver());
			//订单来源
			tbOrder.setSourceType(order.getSourceType());
			//商家ID
			tbOrder.setSellerId(cart.getSellerId());
			//循环购物车明细

			double money=0;
			for(TbOrderItem orderItem :cart.getOrderItemList()){
				orderItem.setId(idWorker.nextId());
				//订单ID
				orderItem.setOrderId(orderId);
				orderItem.setSellerId(cart.getSellerId());
				//金额累加
				money+=orderItem.getTotalFee().doubleValue();
				orderItemMapper.insert(orderItem);
			}
			tbOrder.setPayment(new BigDecimal(money));
			orderMapper.insert(tbOrder);
			total_money+=money;
		}

		//生成订单时，生成支付日志
		if("1".equals(order.getPaymentType())){
			TbPayLog payLog = new TbPayLog();
			//支付订单号
			payLog.setOutTradeNo(idWorker.nextId()+"");
			payLog.setCreateTime(new Date());
			payLog.setUserId(order.getUserId());
			//订单id集合
			payLog.setOrderList(orderIdList.toString().replace("[","").replace("]",""));
			payLog.setTotalFee((long) (total_money*100));
			//交易状态
			payLog.setTradeState("0");
			//微信支付
			payLog.setPayType("1");
			tbPayLogMapper.insert(payLog);
			//放入缓存
			redisTemplate.boundHashOps("payLog").put(order.getUserId(),payLog);

		}



		//3.清除redis中的购物车
		redisTemplate.boundHashOps("cartList").delete(order.getUserId());

	}

	
	/**
	 * 修改
	 */
	@Override
	public void update(TbOrder order){
		orderMapper.updateByPrimaryKey(order);
	}	
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public TbOrder findOne(Long id){
		return orderMapper.selectByPrimaryKey(id);
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for(Long id:ids){
			orderMapper.deleteByPrimaryKey(id);
		}		
	}
	
	
		@Override
	public PageResult findPage(TbOrder order, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbOrderExample example=new TbOrderExample();
		Criteria criteria = example.createCriteria();
		
		if(order!=null){			
						if(order.getPaymentType()!=null && order.getPaymentType().length()>0){
				criteria.andPaymentTypeLike("%"+order.getPaymentType()+"%");
			}
			if(order.getPostFee()!=null && order.getPostFee().length()>0){
				criteria.andPostFeeLike("%"+order.getPostFee()+"%");
			}
			if(order.getStatus()!=null && order.getStatus().length()>0){
				criteria.andStatusLike("%"+order.getStatus()+"%");
			}
			if(order.getShippingName()!=null && order.getShippingName().length()>0){
				criteria.andShippingNameLike("%"+order.getShippingName()+"%");
			}
			if(order.getShippingCode()!=null && order.getShippingCode().length()>0){
				criteria.andShippingCodeLike("%"+order.getShippingCode()+"%");
			}
			if(order.getUserId()!=null && order.getUserId().length()>0){
				criteria.andUserIdLike("%"+order.getUserId()+"%");
			}
			if(order.getBuyerMessage()!=null && order.getBuyerMessage().length()>0){
				criteria.andBuyerMessageLike("%"+order.getBuyerMessage()+"%");
			}
			if(order.getBuyerNick()!=null && order.getBuyerNick().length()>0){
				criteria.andBuyerNickLike("%"+order.getBuyerNick()+"%");
			}
			if(order.getBuyerRate()!=null && order.getBuyerRate().length()>0){
				criteria.andBuyerRateLike("%"+order.getBuyerRate()+"%");
			}
			if(order.getReceiverAreaName()!=null && order.getReceiverAreaName().length()>0){
				criteria.andReceiverAreaNameLike("%"+order.getReceiverAreaName()+"%");
			}
			if(order.getReceiverMobile()!=null && order.getReceiverMobile().length()>0){
				criteria.andReceiverMobileLike("%"+order.getReceiverMobile()+"%");
			}
			if(order.getReceiverZipCode()!=null && order.getReceiverZipCode().length()>0){
				criteria.andReceiverZipCodeLike("%"+order.getReceiverZipCode()+"%");
			}
			if(order.getReceiver()!=null && order.getReceiver().length()>0){
				criteria.andReceiverLike("%"+order.getReceiver()+"%");
			}
			if(order.getInvoiceType()!=null && order.getInvoiceType().length()>0){
				criteria.andInvoiceTypeLike("%"+order.getInvoiceType()+"%");
			}
			if(order.getSourceType()!=null && order.getSourceType().length()>0){
				criteria.andSourceTypeLike("%"+order.getSourceType()+"%");
			}
			if(order.getSellerId()!=null && order.getSellerId().length()>0){
				criteria.andSellerIdLike("%"+order.getSellerId()+"%");
			}
	
		}
		
		Page<TbOrder> page= (Page<TbOrder>)orderMapper.selectByExample(example);		
		return new PageResult(page.getTotal(), page.getResult());
	}

	@Override
	public TbPayLog searchPayLogFromRedis(String userId) {
		return (TbPayLog) redisTemplate.boundHashOps("payLog").get(userId);
	}

	@Override
	public void updateOrderStatus(String out_trade_no, String transaction_id) {
		//1.修改支付日志状态
		TbPayLog payLog = tbPayLogMapper.selectByPrimaryKey(out_trade_no);
		payLog.setPayTime(new Date());
		//已支付
		payLog.setTradeState("1");
		//交易号
		payLog.setTransactionId(transaction_id);
		tbPayLogMapper.updateByPrimaryKey(payLog);
		//2.修改订单状态
		//获取订单号列表
		String orderList = payLog.getOrderList();
		//获取订单号数组
		String[] orderIds = orderList.split(",");

		for(String orderId:orderIds){
			TbOrder order = orderMapper.selectByPrimaryKey( Long.parseLong(orderId) );
			if(order!=null){
				//已付款
				order.setStatus("2");
				orderMapper.updateByPrimaryKey(order);
			}
		}
		//清除redis缓存数据
		redisTemplate.boundHashOps("payLog").delete(payLog.getUserId());
	}

}
