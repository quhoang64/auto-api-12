package login;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import model.login.LoginResponse;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class LoginTest {
    private static final String LOGIN_API = "api/login";
    private static final String X_POWER_BY_HEADER = "X-Powered-By";
    private static final String CONTENT_TYPE_HEADER = "Content-Type";
    private static final String X_POWER_BY_HEADER_VALUE = "Express";
    private static final String CONTENT_TYPE_HEADER_VALUE = "application/json; charset=utf-8";
    private static final String REQUEST_CONTENT_TYPE_HEADER = "Content-Type";
    private static final String REQUEST_CONTENT_TYPE_HEADER_VALUE = "application/json";


    @BeforeAll
    static void setUp(){
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = 3000;
    }

    @Test
    void verifySchemaLoginApi(){
        LoginRequest loginRequest = new LoginRequest("1234567890", "staff");
        RestAssured.given().log().all()
                .header(REQUEST_CONTENT_TYPE_HEADER,REQUEST_CONTENT_TYPE_HEADER_VALUE)
                .body(loginRequest)
                .post(LOGIN_API)
                .then()
                .log().all()
                .statusCode(200)
                .assertThat().body(matchesJsonSchemaInClasspath("json-schema/login-schema.json"));
    }

    @Test
    void verifyLoginSuccessful(){
        LoginRequest loginRequest = new LoginRequest("1234567890", "staff");
        Response response = RestAssured.given().log().all()
                .header(CONTENT_TYPE_HEADER,REQUEST_CONTENT_TYPE_HEADER_VALUE)
                .body(loginRequest)
                .post(LOGIN_API);
        // 1. Verify status
        response.then().log().all().statusCode(200);
        // 2. Verify header
        response.then().header(X_POWER_BY_HEADER, equalTo(X_POWER_BY_HEADER_VALUE))
                .header(CONTENT_TYPE_HEADER, equalTo(CONTENT_TYPE_HEADER_VALUE));
        // 3. VERIFY BODY
        LoginResponse actual = response.body().as(LoginResponse.class);
        assertThat(actual.getToken(), not(emptyOrNullString()));
        assertThat(actual.getTimeout(),equalTo(120000));

    }

    static Stream<Arguments> loginProvider(){
        return Stream.of(
                Arguments.of(new LoginRequest("", "1234567890")),
                Arguments.of(new LoginRequest(null, "1234567890")),
                Arguments.of(new LoginRequest("staff", "")),
                Arguments.of(new LoginRequest("staff", null)),
                Arguments.of(new LoginRequest("staff", "1234"))
        );
    }

    @ParameterizedTest
    @MethodSource("loginProvider")
    void verifyLoginFail(LoginRequest loginRequest){
        Response response = RestAssured.given().log().all()
                .header(CONTENT_TYPE_HEADER,REQUEST_CONTENT_TYPE_HEADER_VALUE)
                .body(loginRequest)
                .post(LOGIN_API);
        // 1. Verify status
        response.then().log().all().statusCode(401);
        // 2. Verify header
        response.then().header(X_POWER_BY_HEADER, equalTo(X_POWER_BY_HEADER_VALUE))
                .header(CONTENT_TYPE_HEADER, equalTo(CONTENT_TYPE_HEADER_VALUE));
        // 3. VERIFY BODY
        LoginError error = response.body().as(LoginError.class);
        assertThat(error.getMessage(), equalTo("Invalid credentials"));

    }
}
