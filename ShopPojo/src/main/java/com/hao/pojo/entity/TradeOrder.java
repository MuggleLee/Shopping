package com.hao.pojo.entity;


import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Data
@Builder
public class TradeOrder extends Model<TradeOrder> implements Serializable {
    private Long orderId;

    private Long userId;

    private Integer orderStatus;

    private Integer payStatus;

    private Integer shippingStatus;

    private String address;

    private String consignee;

    private Long goodsId;

    private Integer goodsNumber;

    private BigDecimal goodsPrice;

    private Long goodsAmount;

    private BigDecimal shippingFee;

    private BigDecimal orderAmount;

    private Long couponId;

    private BigDecimal couponPaid;

    private BigDecimal moneyPaid;

    private BigDecimal payAmount;

    private Date addTime;

    private Date confirmTime;

    private Date payTime;

}