package cn.scut.aicodesmell.common;

import lombok.*;

/**
 * @author wanghy
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Data
public class MatchEntity {
    private String docComponent;
    private Double probability;
    private String codeComponent;
}
