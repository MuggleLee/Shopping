package com.hao.pojo.entity;

import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Data
@Builder
public class TradeUserMoneyLog extends Model<TradeUserMoneyLog> implements Serializable {

    private Long userId;

    private Long orderId;

    private Integer moneyLogType;

    private BigDecimal useMoney;

    private Date createTime;

}