package com.mickey.sentibackend.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author 刘屿
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Result<T> {
    private Integer code;
    private String msg;
    private T data;

    public static <T> Result<T> buildSuccess() {
        return new Result(0, "成功", null);
    }

    public static <T> Result<T> buildSuccess(T data) {
        return new Result(0, "成功", data);
    }

    public static <T> Result<T> buildSuccess(Integer code, String msg, T data) {
        return new Result(code, msg, data);
    }

    public static <T> Result<T> buildFailed(Integer code, Throwable e) {
        return new Result(code, e.getLocalizedMessage(), null);
    }

    public String toString() {
        return "Result(code=" + this.getCode() + ", msg=" + this.getMsg() + ", data=" + this.getData() + ")";
    }

}
