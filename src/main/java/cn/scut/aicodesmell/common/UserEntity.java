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
public class UserEntity {
    private Integer userId;
    private String userName;
    private String password;
}
