package com.hao.order.feign;

import com.hao.pojo.entity.TradeGoods;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient("ShopGoods")
public interface GoodsService {
    TradeGoods findOne(Long goodsId);
}
