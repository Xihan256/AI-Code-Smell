package cn.scut.aicodesmell.common.response;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author wanghy
 */
@Data
@AllArgsConstructor
public class Result {
    private String message;
    private Object data;
    private Integer code;
}