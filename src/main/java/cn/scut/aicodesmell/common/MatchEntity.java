package cn.scut.aicodesmell.common;

import lombok.*;

import java.util.List;

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
    private List<String> docComponentSentences;
}
