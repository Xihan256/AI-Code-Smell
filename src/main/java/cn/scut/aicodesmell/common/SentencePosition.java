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
public class SentencePosition {
    private String sentence;
    private int start;
    private int end;
}
