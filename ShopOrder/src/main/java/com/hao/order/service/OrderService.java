package com.hao.order.service;

import com.hao.common.entity.Result;
import com.hao.pojo.entity.TradeOrder;

public interface OrderService {
    Result confirmOrder(TradeOrder order);
}
