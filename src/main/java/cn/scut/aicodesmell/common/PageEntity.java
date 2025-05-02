package cn.scut.aicodesmell.common;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * @author wanghy
 */
@Getter
@Setter
@NoArgsConstructor
public class PageEntity<T> {
    private Integer curPage;
    private Integer pageSize;
    private Integer total;
    private List<T> data;

    public PageEntity(Integer curPage, Integer pageSize, Integer total, List<T> data) {
        this.curPage = curPage;
        this.pageSize = pageSize;
        this.total = total;
        this.data = data;
    }

}
