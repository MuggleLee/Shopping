package com.hao.pojo.entity;

import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TradeGoodsNumberLog extends Model<TradeGoodsNumberLog> implements Serializable {

    private Long goodsId;

    private Long orderId;

    private Integer goodsNumber;

    private Date logTime;

}