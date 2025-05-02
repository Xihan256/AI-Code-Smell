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
    private String orderName;
    private Integer userId;
    private Date createTime;
    private String orderStatus;
    private String docUrl;
    private String codeUrl;
    private String resultUrl;
    private Double timeCost;
    private String documentComponent;
    private String mainPackage;
}
