package testCase.user;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import model.login.LoginRequest;
import model.login.LoginResponse;
import model.user.*;
import org.junit.jupiter.api.Test;
import testCase.TestMaster;

import java.net.URI;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.TimeZone;

import static net.javacrumbs.jsonunit.JsonMatchers.jsonEquals;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static utils.ConstantUtils.*;
import static utils.DateTimeUtils.parseTimeToCurrentTimeZone;

public class CreateUserTest extends TestMaster {
    @Test
    void verifyCreateUserSuccessful() {
        LoginRequest loginRequest = new LoginRequest("staff", "1234567890");
        Response response = RestAssured.given().log().all()
                .header(CONTENT_TYPE_HEADER, REQUEST_CONTENT_TYPE_HEADER_VALUE)
                .body(loginRequest)
                .post(LOGIN_API);
        // 1. Verify status
        response.then().log().all().statusCode(200);

        LoginResponse loginResponse = response.body().as(LoginResponse.class);
        // Create UserAddress
        UserAddressRequest userAddressRequest = new UserAddressRequest();
        userAddressRequest.setStreetNumber("123");
        userAddressRequest.setStreet("Main St");
        userAddressRequest.setWard("Ward 7");
        userAddressRequest.setDistrict("District 7");
        userAddressRequest.setCity("Thu Duc");
        userAddressRequest.setState("Ho Chi Minh");
        userAddressRequest.setZip("34341");
        userAddressRequest.setCountry("VN");
        // Create User
        UserRequest userRequest = new UserRequest();
        userRequest.setFirstName("Donal");
        userRequest.setLastName("Trumb");
        userRequest.setMiddleName("Leo");
        userRequest.setBirthday("01-02-1992");
        userRequest.setEmail(String.format("demoapi_%s@gmail.com", System.currentTimeMillis()));
        userRequest.setPhone("0971844992");
        userRequest.setAddresses(List.of(userAddressRequest));

        LocalDateTime timeBeforeCreate = LocalDateTime.now();

        Response createUserResponse = RestAssured.given().log().all()
                .header(CONTENT_TYPE_HEADER, REQUEST_CONTENT_TYPE_HEADER_VALUE)
                .header(AUTHORIZATION_HEADER, String.format("Bearer %s", loginResponse.getToken()))
                .body(userRequest)
                .post(CREATE_USER_API);

        LocalDateTime timeAfterCreate = LocalDateTime.now();


        createUserResponse.then().log().all().statusCode(200);

        createUserResponse.then().header(X_POWER_BY_HEADER, equalTo(X_POWER_BY_HEADER_VALUE))
                .header(CONTENT_TYPE_HEADER, equalTo(CONTENT_TYPE_HEADER_VALUE));

        UserResponse userResponse = createUserResponse.body().as(UserResponse.class);
        assertThat(userResponse.getId(), not(emptyOrNullString()));
        assertThat(userResponse.getMessage(), equalTo("Customer created"));

        // 5. double check that user existing in system or not by getUserAPI
        Response getUserResponse = RestAssured.given().log().all()
                .header(CONTENT_TYPE_HEADER, REQUEST_CONTENT_TYPE_HEADER_VALUE)
                .header(AUTHORIZATION_HEADER, String.format("Bearer %s", loginResponse.getToken()))
                .get(GET_USER_API, userResponse.getId());

        // verify status
        getUserResponse.then().log().all().statusCode(200);
        // verify get user response again request

        GetUserResponse actualGetUserResponse = getUserResponse.body().as(GetUserResponse.class);

        assertThat(actualGetUserResponse.getId(), equalTo(userResponse.getId()));
        assertThat(actualGetUserResponse, jsonEquals(userRequest)
                .whenIgnoringPaths("id", "createdAt", "updatedAt", "addresses[*].id", "addresses[*].customerId", "addresses[*].createdAt", "addresses[*].updatedAt"));


        LocalDateTime userCreateAtDate = parseTimeToCurrentTimeZone(actualGetUserResponse.getCreatedAt());
        LocalDateTime userUpdateAtDate = parseTimeToCurrentTimeZone(actualGetUserResponse.getUpdatedAt());
        assertThat(userCreateAtDate.isAfter(timeBeforeCreate),equalTo(true));
        assertThat(userCreateAtDate.isBefore(timeAfterCreate),equalTo(true));
        assertThat(userUpdateAtDate.isAfter(timeBeforeCreate),equalTo(true));
        assertThat(userUpdateAtDate.isBefore(timeAfterCreate),equalTo(true));

        for(GetUserAddressResponse addressResponse : actualGetUserResponse.getAddresses()){
            assertThat(addressResponse.getCustomerId(), equalTo(userResponse.getId()));
            assertThat(addressResponse.getId(), not(emptyOrNullString()));

            LocalDateTime userAddressCreateAtDate = parseTimeToCurrentTimeZone(addressResponse.getCreatedAt());;
            LocalDateTime userAddressUpdateAtDate = parseTimeToCurrentTimeZone(addressResponse.getUpdatedAt());;
            assertThat(userAddressCreateAtDate.isAfter(timeBeforeCreate),equalTo(true));
            assertThat(userAddressCreateAtDate.isBefore(timeAfterCreate),equalTo(true));
            assertThat(userAddressUpdateAtDate.isAfter(timeBeforeCreate),equalTo(true));
            assertThat(userAddressUpdateAtDate.isBefore(timeAfterCreate),equalTo(true));


        }

    }

}
