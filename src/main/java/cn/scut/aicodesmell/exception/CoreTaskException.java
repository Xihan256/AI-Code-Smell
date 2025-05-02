package cn.scut.aicodesmell.exception;

/**
 * @author wanghy
 */
public class CoreTaskException extends RuntimeException {
    public String orderId;

    public CoreTaskException(String orderId) {
        this.orderId = orderId;
    }
}
