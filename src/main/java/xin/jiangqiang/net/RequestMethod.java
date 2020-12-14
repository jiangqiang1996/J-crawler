package xin.jiangqiang.net;

/**
 * @author jiangqiang
 * @date 2020/12/14 9:56
 */
public enum RequestMethod {
    GET("GET"), HEAD("HEAD"), POST("POST"), DELETE("DELETE"), PUT("PUT"), PATCH("PATCH");
    private final String method;

    RequestMethod(String method) {
        this.method = method;
    }

    public String getMethod() {
        return method;
    }
}
