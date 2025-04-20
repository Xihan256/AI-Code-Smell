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
public class MatchComponentEntity {
    private String name;
    private double probability;
    private String codeComponent;
}
