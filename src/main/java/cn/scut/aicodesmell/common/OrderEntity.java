package cn.scut.aicodesmell.common;

import lombok.*;

import java.sql.Date;

/**
 * @author wanghy
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Data
public class OrderEntity {
    private String orderId;
    private Integer userId;
    private Date createTime;
    private String orderStatus;
    private String docUrl;
    private String codeUrl;
    private String resultUrl;
}
