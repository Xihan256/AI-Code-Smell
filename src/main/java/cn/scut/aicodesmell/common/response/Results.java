package cn.scut.aicodesmell.common.response;

/**
 * @author wanghy
 */
public class Results {
    public static Result ok(Object data) {
        return new Result("ok", data, 200);
    }

    /*
        400
     */
    public static Result paramWrong(String message) {
        return new Result(message, null, 40001);
    }

    public static Result userNameError(String message) {
        return new Result(message, null, 40002);
    }

    /*
       403
    */
    public static Result notLogin() {
        return new Result("未登录!", null, 40300);
    }

    /*
       500
    */
    public static Result internalError(String message) {
        return new Result(message, null, 50001);
    }
}
