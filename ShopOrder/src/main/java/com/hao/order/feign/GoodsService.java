package com.hao.order.feign;

import com.hao.common.entity.Result;
import com.hao.pojo.entity.TradeGoods;
import com.hao.pojo.entity.TradeGoodsNumberLog;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient("ShopGoods")
public interface GoodsService {

    @GetMapping("/goods/getById")
    TradeGoods findOne(Long goodsId);

    @PostMapping("/goods/reduceGoodsNum")
    Result reduceGoodsNum(TradeGoodsNumberLog goodsNumberLog);
}
