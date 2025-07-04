package utils;

public class ConstantUtils {
    public static final String HOST = EnvUtils.Env().get("host");
    public static final int PORT = Integer.parseInt(EnvUtils.Env().get("post"));
    public static final String LOGIN_API = "api/login";
    public static final String CREATE_USER_API = "api/user";
    public static final String GET_USER_API = "api/user/{userId}";
    public static final String DELETE_USER_API = "api/user/{userId}";
    public static final String CREATE_CARD_API = "api/card";


    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String X_POWER_BY_HEADER = "X-Powered-By";
    public static final String CONTENT_TYPE_HEADER = "Content-Type";
    public static final String X_POWER_BY_HEADER_VALUE = "Express";
    public static final String CONTENT_TYPE_HEADER_VALUE = "application/json; charset=utf-8";
    public static final String REQUEST_CONTENT_TYPE_HEADER = "Content-Type";
    public static final String REQUEST_CONTENT_TYPE_HEADER_VALUE = "application/json";
    public static final String EMAIL_TEMPLATE = "demoapi_%s@gmail.com";
}
