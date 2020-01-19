package com.hao.order.feign;

import com.hao.pojo.entity.TradeCoupon;
import com.hao.pojo.entity.TradeUser;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient("ShopCoupon")
public interface CouponService {

    TradeCoupon findOne(Long couponId);
}
