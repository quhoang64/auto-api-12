package testCase;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import model.login.LoginRequest;
import model.login.LoginResponse;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

import java.time.LocalDateTime;

import static utils.ConstantUtils.*;

public class TestMaster {
    public static String token;
    private static int tokenTimeout;
    private static LocalDateTime retrieveTokenMoment;

    @BeforeAll
    static void globalSetUp() {
        RestAssured.baseURI = HOST;
        RestAssured.port = PORT;
    }

    @BeforeEach
    public void globalForEach() {
        if (StringUtils.isAllBlank(token) || retrieveTokenMoment.plusNanos(tokenTimeout).isAfter(LocalDateTime.now())) {
            LoginRequest loginRequest = new LoginRequest("staff", "1234567890");
            Response response = RestAssured.given().log().all()
                    .header(CONTENT_TYPE_HEADER, REQUEST_CONTENT_TYPE_HEADER_VALUE)
                    .body(loginRequest)
                    .post(LOGIN_API);
            response.then().log().all().statusCode(200);
            LoginResponse loginResponse = response.body().as(LoginResponse.class);
            token = loginResponse.getToken();
            tokenTimeout = loginResponse.getTimeout() * 80 / 100;
            retrieveTokenMoment = LocalDateTime.now();
        }
    }

}
