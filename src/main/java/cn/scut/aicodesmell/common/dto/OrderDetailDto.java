package cn.scut.aicodesmell.common.dto;

import lombok.*;

/**
 * @author wanghy
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Data
public class OrderDetailDto {
    private String orderId;
    private String codeComponent;
    private double probability;
    private String componentName;
}
