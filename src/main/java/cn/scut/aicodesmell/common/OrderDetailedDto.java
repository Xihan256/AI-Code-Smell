package cn.scut.aicodesmell.common;

import lombok.*;

import java.sql.Date;
import java.util.List;

/**
 * @author wanghy
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Data
public class OrderDetailedDto {
    private String orderId;
    private String orderName;
    private Integer userId;
    private Date createTime;
    private String docUrl;
    private String codeUrl;
    private String resultUrl;
    private Double timeCost;
    private List<MatchEntity> documentComponent;
    private String mainPackage;
}
