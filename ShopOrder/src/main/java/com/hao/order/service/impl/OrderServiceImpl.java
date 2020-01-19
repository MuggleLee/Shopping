package com.hao.order.service.impl;

import com.hao.common.unilt.IDWorker;
import com.hao.common.entity.Result;
import com.hao.common.entity.ShopCode;
import com.hao.common.exception.CastException;
import com.hao.order.feign.CouponService;
import com.hao.order.feign.GoodsService;
import com.hao.order.feign.UserService;
import com.hao.order.service.OrderService;
import com.hao.pojo.entity.TradeCoupon;
import com.hao.pojo.entity.TradeGoods;
import com.hao.pojo.entity.TradeOrder;
import com.hao.pojo.entity.TradeUser;
import javassist.expr.Cast;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    @Override
    public Result confirmOrder(TradeOrder order) {

        // 1.校验订单
        checkOrder(order);

        // 2.生成预订单
        long orderId = savePreOrder(order);

        return null;
    }

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
            if(coupon.getIsUsed()==1){
                CastException.cast(ShopCode.SHOP_COUPON_ISUSED);
            }
            order.setCouponPaid(coupon.getCouponPrice());
        }else{
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
     *
     * @param orderAmount
     * @return
     */
    private BigDecimal calculateShippingFee(BigDecimal orderAmount) {
        if (orderAmount.compareTo(new BigDecimal(99)) == 1) {
            return BigDecimal.ZERO;
        }
        return new BigDecimal(10);
    }

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
