package com.hao.order.service.impl;

import com.alibaba.fastjson.JSON;
import com.hao.common.entity.MQEntity;
import com.hao.common.unilt.IDWorker;
import com.hao.common.entity.Result;
import com.hao.common.entity.ShopCode;
import com.hao.common.exception.CastException;
import com.hao.order.feign.CouponService;
import com.hao.order.feign.GoodsService;
import com.hao.order.feign.UserService;
import com.hao.order.service.OrderService;
import com.hao.pojo.entity.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.remoting.exception.RemotingException;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;

@Service
@Slf4j
public class OrderServiceImpl implements OrderService {

    @Autowired
    private UserService userService;

    @Autowired
    private GoodsService goodsService;

    @Autowired
    private CouponService couponService;

    @Autowired
    private IDWorker idWorker;

    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    @Value("${mq.order.topic}")
    private String topic;

    @Value("${mq.order.tag.cancel}")
    private String tag;


    @Override
    public Result confirmOrder(TradeOrder order) {

        // 1.校验订单
        checkOrder(order);

        // 2.生成预订单
        long orderId = savePreOrder(order);

        try {
            // 3.扣减库存
            reduceGoodsNum(order);

            // 4.扣减优惠券
            updateCouponStatus(order);

            // 5.使用余额
            reduceMoneyPaid(order);

            // 6.确认订单
            updateOrderStatus(order);

            // 7.返回成功状态
            return new Result(ShopCode.SHOP_SUCCESS.getSuccess(),ShopCode.SHOP_SUCCESS.getMessage());
        } catch (Exception e) {
            // 创建 MQEntity 实体
            MQEntity entity = new MQEntity();
            entity.setOrderId(orderId);
            entity.setUserId(order.getUserId());
            entity.setUserMoney(order.getMoneyPaid());
            entity.setGoodsId(order.getGoodsId());
            entity.setGoodsNum(order.getGoodsNumber());
            entity.setCouponId(order.getCouponId());
            // 发送订单确认失败消息
            try {
                sendCancelOrder(topic,tag,order.getOrderId().toString(), JSON.toJSONString(entity));
            } catch (Exception e1) {
                e1.printStackTrace();
            }
            return new Result(ShopCode.SHOP_FAIL.getSuccess(),ShopCode.SHOP_FAIL.getMessage());
        }
    }

    /**
     * 发送订单确认失败消息
     */
    private void sendCancelOrder(String topic, String tag, String keys, String body) throws InterruptedException, RemotingException, MQClientException, MQBrokerException {
        Message message = new Message(topic,tag,keys,body.getBytes());
        rocketMQTemplate.getProducer().send(message);
    }

    /**
     * 确认订单
     */
    private void updateOrderStatus(TradeOrder order) {
        order.setOrderStatus(ShopCode.SHOP_ORDER_CONFIRM.getCode());
        order.setPayStatus(ShopCode.SHOP_ORDER_PAY_STATUS_NO_PAY.getCode());
        order.setConfirmTime(new Date());
        if(!order.updateById()){
            CastException.cast(ShopCode.SHOP_ORDER_CONFIRM_FAIL);
        }
        log.info("订单:"+order.getOrderId()+"确认订单成功");
    }

    /**
     * 使用余额
     */
    private void reduceMoneyPaid(TradeOrder order) {
        BigDecimal moneyPaid = order.getMoneyPaid();
        if (moneyPaid != null && moneyPaid.compareTo(BigDecimal.ZERO) == 1) {
            TradeUserMoneyLog userMoneyLog = TradeUserMoneyLog.builder()
                    .userId(order.getUserId())
                    .orderId(order.getOrderId())
                    .useMoney(order.getMoneyPaid())
                    .moneyLogType(ShopCode.SHOP_USER_MONEY_PAID.getCode())
                    .createTime(new Date()).build();
            Result result = userService.updateMoneyPaid(userMoneyLog);
            if(result.getSuccess().equals(ShopCode.SHOP_FAIL)){
                CastException.cast(ShopCode.SHOP_USER_MONEY_REDUCE_FAIL);
            }
        }
        log.info("订单:"+order.getOrderId()+",扣减余额成功");
    }

    /**
     * 扣减优惠券
     */
    private void updateCouponStatus(TradeOrder order) {
        Long couponId = order.getCouponId();
        if (couponId == null) {
            CastException.cast(ShopCode.SHOP_COUPON_NO_EXIST);
        }
        TradeCoupon coupon = couponService.findOne(couponId);
        coupon.setUsedTime(new Date());
        coupon.setIsUsed(ShopCode.SHOP_COUPON_ISUSED.getCode());
        coupon.setOrderId(order.getOrderId());
        Result result = couponService.updateCoupon(coupon);
        if (result.getSuccess().equals(ShopCode.SHOP_FAIL.getSuccess())) {
            CastException.cast(ShopCode.SHOP_COUPON_USE_FAIL);
        }
        log.info("订单:" + order.getOrderId() + ",使用优惠券");
    }

    /**
     * 扣减库存
     */
    @Transactional
    public void reduceGoodsNum(TradeOrder order) {
//        TradeGoods goods = goodsService.findOne(order.getGoodsId());
//        int goodsNumber = goods.getGoodsNumber();
//        int orderGoods = order.getGoodsNumber();
//        if (goodsNumber < orderGoods) {
//            CastException.cast(ShopCode.SHOP_GOODS_NUM_NOT_ENOUGH);
//        } else {
//            goods.setGoodsNumber(goodsNumber - orderGoods);
//        }
//
//        // 日志记录
//        boolean insertToLog = TradeGoodsNumberLog.builder().goodsId(order.getGoodsId()).orderId(order.getOrderId()).goodsNumber(goodsNumber).logTime(new Date()).build().insert();
//        boolean updateGoods = goods.updateById();
//        if (insertToLog || updateGoods) {
//            CastException.cast(ShopCode.SHOP_REDUCE_GOODS_NUM_FAIL);
//        }
        TradeGoodsNumberLog goodsNumberLog = new TradeGoodsNumberLog();
        goodsNumberLog.setGoodsId(order.getGoodsId());
        goodsNumberLog.setOrderId(order.getOrderId());
        goodsNumberLog.setLogTime(new Date());
        Result result = goodsService.reduceGoodsNum(goodsNumberLog);
        if(result.getSuccess().equals(ShopCode.SHOP_FAIL.getSuccess())){
            CastException.cast(ShopCode.SHOP_REDUCE_GOODS_NUM_FAIL);
        }
        log.info("订单:" + order.getOrderId() + "扣减库存成功");
    }

    /**
     * 生成预订单
     */
    private long savePreOrder(TradeOrder order) {

        TradeUser user = userService.findOne(order.getUserId());

        //1. 设置订单状态为不可见
        order.setOrderStatus(ShopCode.SHOP_ORDER_NO_CONFIRM.getCode());
        //2. 设置订单ID
        long orderId = idWorker.nextId();
        order.setOrderId(orderId);
        //3. 核算订单运费
        BigDecimal shippingFee = calculateShippingFee(order.getOrderAmount());
        if (order.getShippingFee().compareTo(shippingFee) != 0) {
            CastException.cast(ShopCode.SHOP_ORDER_SHIPPINGFEE_INVALID);
        }
        //4. 核算订单总金额是否合法
        BigDecimal orderAmount = new BigDecimal(order.getGoodsNumber()).multiply(order.getGoodsPrice());
        orderAmount.add(shippingFee);
        if (order.getOrderAmount().compareTo(orderAmount) != 0) {
            CastException.cast(ShopCode.SHOP_ORDERAMOUNT_INVALID);
        }
        //5.判断用户是否使用余额
        BigDecimal moneyPaid = order.getMoneyPaid();
        if (moneyPaid != null) {
            //5.1 订单中余额是否合法
            if (moneyPaid.compareTo(BigDecimal.ZERO) < 0) {
                CastException.cast(ShopCode.SHOP_MONEY_PAID_LESS_ZERO);
            } else if (moneyPaid.compareTo(BigDecimal.ZERO) > 0) {// 订单中用户余额大于0，判断用户账号是否大于等于这笔余额
                BigDecimal userMoney = new BigDecimal(user.getUserMoney());
                if (userMoney.compareTo(moneyPaid) < 0) {
                    CastException.cast(ShopCode.SHOP_MONEY_PAID_INVALID);
                }
            } else {
                order.setMoneyPaid(BigDecimal.ZERO);
            }
        }
        //6.判断用户是否使用优惠券
        Long couponId = order.getCouponId();
        if (couponId != null) {
            //6.1 判断优惠券是否存在
            TradeCoupon coupon = couponService.findOne(couponId);
            if (coupon == null) {
                CastException.cast(ShopCode.SHOP_COUPON_NO_EXIST);
            }
            //6.2 判断优惠券是否已经被使用
            if (coupon.getIsUsed() == 1) {
                CastException.cast(ShopCode.SHOP_COUPON_ISUSED);
            }
            order.setCouponPaid(coupon.getCouponPrice());
        } else {
            order.setCouponPaid(BigDecimal.ZERO);
        }

        //7.核算订单支付金额    订单总金额-余额-优惠券金额
        BigDecimal payMoney = order.getOrderAmount().subtract(order.getMoneyPaid()).subtract(order.getCouponPaid());
        order.setPayAmount(payMoney);
        //8.设置下单时间
        order.setAddTime(new Date());
        //9.保存订单到数据库
        order.insert();
        //10.返回订单ID
        return orderId;
    }

    /**
     * 计算运费
     * 商品价格超过99包邮，否则收取10元运费
     */
    private BigDecimal calculateShippingFee(BigDecimal orderAmount) {
        if (orderAmount.compareTo(new BigDecimal(99)) == 1) {
            return BigDecimal.ZERO;
        }
        return new BigDecimal(10);
    }

    /**
     * 校验订单
     */
    private void checkOrder(TradeOrder order) {
        //1.校验订单是否存在
        if (order == null) {
            CastException.cast(ShopCode.SHOP_ORDER_INVALID);
        }
        //2.校验订单中的商品是否存在
        TradeGoods goods = goodsService.findOne(order.getGoodsId());
        if (goods == null) {
            CastException.cast(ShopCode.SHOP_GOODS_NO_EXIST);
        }
        //3.校验下单用户是否存在
        TradeUser user = userService.findOne(order.getUserId());
        if (user == null) {
            CastException.cast(ShopCode.SHOP_USER_NO_EXIST);
        }
        //4.校验商品单价是否合法
        if (order.getGoodsPrice().compareTo(goods.getGoodsPrice()) != 0) {
            CastException.cast(ShopCode.SHOP_GOODS_PRICE_INVALID);
        }
        //5.校验订单商品数量是否合法
        if (order.getGoodsNumber() >= goods.getGoodsNumber()) {
            CastException.cast(ShopCode.SHOP_GOODS_NUM_NOT_ENOUGH);
        }
        log.info("校验订单通过");
    }
}
