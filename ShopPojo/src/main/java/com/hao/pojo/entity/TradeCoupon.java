package com.hao.pojo.entity;

import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.hao.common.entity.Result;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Data
@Builder
public class TradeCoupon extends Model<TradeCoupon> implements Serializable{
    private Long couponId;

    private BigDecimal couponPrice;

    private Long userId;

    private Long orderId;

    private Integer isUsed;

    private Date usedTime;

}