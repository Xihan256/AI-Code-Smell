package cn.scut.aicodesmell.common;

/**
 * @author wanghy
 */
public enum OrderEntityStatus {
    STATUS_NEW(1),
    STATUS_PROCESSING(2),
    STATUS_FAILED(3),
    STATUS_FINISHED(4);

    private Integer status;

    OrderEntityStatus(int status) {
        this.status = status;
    }

    public Integer getStatus() {
        return status;
    }
}
