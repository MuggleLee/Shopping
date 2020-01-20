package com.hao.order.feign;

import com.hao.common.entity.Result;
import com.hao.pojo.entity.TradeGoods;
import com.hao.pojo.entity.TradeUser;
import com.hao.pojo.entity.TradeUserMoneyLog;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient("ShopUser")
public interface UserService {

    @GetMapping("/user/getById")
    TradeUser findOne(Long userId);

    @PostMapping("/user/updateMoneyPaid")
    Result updateMoneyPaid(TradeUserMoneyLog userMoneyLog);
}
