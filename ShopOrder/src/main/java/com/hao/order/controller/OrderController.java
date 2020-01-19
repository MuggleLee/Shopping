package com.hao.order.controller;

import com.hao.common.entity.Result;
import com.hao.order.service.OrderService;
import com.hao.pojo.entity.TradeOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OrderController {

    @Autowired
    private OrderService orderService;

    @PostMapping
    public Result confirm(@RequestBody TradeOrder order){
        return orderService.confirmOrder(order);
    }
}
