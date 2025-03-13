package cn.scut.aicodesmell.factory;

import cn.scut.aicodesmell.common.PageEntity;

/**
 * @author wanghy
 */
public class PageFactory {
    public static <T> PageEntity<T> buildPage(Integer total, Integer curPage, Integer pageSize) {
        PageEntity<T> pageEntity = new PageEntity<>();
        pageEntity.setPageSize(pageSize);
        pageEntity.setCurPage(curPage);
        pageEntity.setTotal(total);
        return pageEntity;
    }

    public static PagePO pageHelper(Integer curPage, Integer pageSize) {
        int offset = pageSize * (curPage - 1);
        int limit = pageSize;
        return new PagePO(limit, offset);
    }

    public static class PagePO {
        public Integer limit;
        public Integer offset;

        public PagePO(Integer limit, Integer offset) {
            this.limit = limit;
            this.offset = offset;
        }
    }
}
