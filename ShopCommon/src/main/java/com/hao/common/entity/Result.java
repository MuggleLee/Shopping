package com.hao.common.entity;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * 结果实体类
 */
@Getter
@Setter
public class Result implements Serializable {
    private Boolean success;
    private String message;

    public Result() {
    }

    public Result(Boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    @Override
    public String toString() {
        return "Result{" +
                "success=" + success +
                ", message='" + message + '\'' +
                '}';
    }
}
