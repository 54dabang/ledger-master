package com.ledger.business.util;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import lombok.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 返回响应，统一封装实体
 *
 * @param <T> 数据实体泛型
 */
@Getter
@ToString
@EqualsAndHashCode
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@ApiModel(value = "返回响应", description = "返回响应，统一封装实体")
public class Result<T> {

    public static final String CODE_SUC = "0"; //成功
    public static final String CODE_FAIL = "1"; //失败
    public static final String CODE_ERROR = "2";//异常
    public static Map<String, String> codeMap = new HashMap<String, String>();
    static {
        codeMap.put(CODE_SUC, "成功");
        codeMap.put(CODE_FAIL, "失败");
        codeMap.put(CODE_ERROR,"请求异常");
    }

    @ApiModelProperty(value = "用户提示", example = "操作成功！")
    private String msg;

    /**
     * 错误码<br>
     * 调用成功时，为 null。<br>
     * 示例：A0211
     */
    @ApiModelProperty(value = "错误码")
    private String code;


    /**
     * 数据实体（泛型）<br>
     * 当接口没有返回数据时，为 null。
     */
    @ApiModelProperty(value = "数据实体（泛型）")
    private T data;



    public static <T> Result<T> success(T data) {
        return new Result<>(codeMap.get(CODE_SUC),CODE_SUC,data);
    }




    public static <T> Result<T> fail(T data) {
        return new Result<>(codeMap.get(CODE_FAIL),CODE_FAIL,data);
    }

    public static <T> Result<T> error(String msg,T data) {
        return new Result<>(msg,CODE_ERROR,data);
    }
    public static <T> Result<T> fail(String msg, String code, T data) {
        return new Result<>(msg, code,data);
    }

}


