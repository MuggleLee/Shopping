package com.hao.order.feign;

import com.hao.pojo.entity.TradeGoods;
import com.hao.pojo.entity.TradeUser;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient("ShopUser")
public interface UserService {

    TradeUser findOne(Long userId);
}
