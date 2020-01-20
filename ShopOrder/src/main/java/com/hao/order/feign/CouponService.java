package com.hao.order.feign;

import com.hao.common.entity.Result;
import com.hao.pojo.entity.TradeCoupon;
import com.hao.pojo.entity.TradeUser;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient("ShopCoupon")
public interface CouponService {

    @GetMapping("/coupon/getById")
    TradeCoupon findOne(Long couponId);

    @PostMapping("/coupon/updateCoupon")
    Result updateCoupon(TradeCoupon coupon);
}
