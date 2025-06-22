package testCase.user;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import model.dao.user.AddressDao;
import model.dao.user.UserDao;
import model.dto.user.*;
import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import testCase.TestMaster;
import utils.DbUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static net.javacrumbs.jsonunit.JsonMatchers.jsonEquals;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static utils.ConstantUtils.*;
import static utils.DateTimeUtils.parseTimeToCurrentTimeZone;
import static utils.DbUtils.getUserFromDb;

public class CreateUserTest extends TestMaster {
    private static final String[] IGNORE_FIELD = {"id", "createdAt", "updatedAt", "addresses[*].id", "addresses[*].customerId", "addresses[*].createdAt", "addresses[*].updatedAt"};
    private static List<String> ids = new ArrayList<>();

    @AfterAll
    static void tearDown(){
        for(String id: ids){
            RestAssured.given().log().all()
                    .header(CONTENT_TYPE_HEADER, REQUEST_CONTENT_TYPE_HEADER_VALUE)
                    .header(AUTHORIZATION_HEADER, token)
                    .delete(DELETE_USER_API, id)
                    .then().log().all();
        }
    }
    @Test
    void verifyCreateUserSuccessful() {

        // Create UserAddress
        UserAddressRequest userAddressRequest = UserAddressRequest.getDefault();
        // Create User
        UserRequest userRequest = UserRequest.getDefault();
        userRequest.setEmail(String.format(EMAIL_TEMPLATE, System.currentTimeMillis()));
        userRequest.setAddresses(List.of(userAddressRequest));

        LocalDateTime timeBeforeCreate = LocalDateTime.now();

        Response createUserResponse = createUser(userRequest);

        LocalDateTime timeAfterCreate = LocalDateTime.now();

        createUserResponse.then().log().all().statusCode(200);

        createUserResponse.then().header(X_POWER_BY_HEADER, equalTo(X_POWER_BY_HEADER_VALUE))
                .header(CONTENT_TYPE_HEADER, equalTo(CONTENT_TYPE_HEADER_VALUE));

        UserResponse userResponse = createUserResponse.body().as(UserResponse.class);
        assertThat(userResponse.getId(), not(emptyOrNullString()));
        assertThat(userResponse.getMessage(), equalTo("Customer created"));
        ids.add(userResponse.getId());

        // 5. double check that user existing in system or not by getUserAPI
        Response getUserResponse = getUser(userResponse);

        // verify status
        getUserResponse.then().log().all().statusCode(200);
        // verify get user response again request

        GetUserResponse actualGetUserResponse = getUserResponse.body().as(GetUserResponse.class);

        assertThat(actualGetUserResponse.getId(), equalTo(userResponse.getId()));
        assertThat(actualGetUserResponse, jsonEquals(userRequest)
                .whenIgnoringPaths(IGNORE_FIELD));


        LocalDateTime userCreateAtDate = parseTimeToCurrentTimeZone(actualGetUserResponse.getCreatedAt());
        LocalDateTime userUpdateAtDate = parseTimeToCurrentTimeZone(actualGetUserResponse.getUpdatedAt());
        assertThat(userCreateAtDate.isAfter(timeBeforeCreate), equalTo(true));
        assertThat(userCreateAtDate.isBefore(timeAfterCreate), equalTo(true));
        assertThat(userUpdateAtDate.isAfter(timeBeforeCreate), equalTo(true));
        assertThat(userUpdateAtDate.isBefore(timeAfterCreate), equalTo(true));

        for (GetUserAddressResponse addressResponse : actualGetUserResponse.getAddresses()) {
            assertThat(addressResponse.getCustomerId(), equalTo(userResponse.getId()));
            assertThat(addressResponse.getId(), not(emptyOrNullString()));

            LocalDateTime userAddressCreateAtDate = parseTimeToCurrentTimeZone(addressResponse.getCreatedAt());
            ;
            LocalDateTime userAddressUpdateAtDate = parseTimeToCurrentTimeZone(addressResponse.getUpdatedAt());
            ;
            assertThat(userAddressCreateAtDate.isAfter(timeBeforeCreate), equalTo(true));
            assertThat(userAddressCreateAtDate.isBefore(timeAfterCreate), equalTo(true));
            assertThat(userAddressUpdateAtDate.isAfter(timeBeforeCreate), equalTo(true));
            assertThat(userAddressUpdateAtDate.isBefore(timeAfterCreate), equalTo(true));


        }

    }

    @Test
    void verifyCreateUserSuccessfulWithDatabase(){
        // Create UserAddress
        UserAddressRequest userAddressRequest = UserAddressRequest.getDefault();
        // Create User
        UserRequest userRequest = UserRequest.getDefault();
        userRequest.setEmail(String.format("demoapi_%s@gmail.com", System.currentTimeMillis()));
        userRequest.setAddresses(List.of(userAddressRequest));

        LocalDateTime timeBeforeCreate = LocalDateTime.now();

        Response createUserResponse = createUser(userRequest);

        LocalDateTime timeAfterCreate = LocalDateTime.now();

        createUserResponse.then().log().all().statusCode(200);

        createUserResponse.then().header(X_POWER_BY_HEADER, equalTo(X_POWER_BY_HEADER_VALUE))
                .header(CONTENT_TYPE_HEADER, equalTo(CONTENT_TYPE_HEADER_VALUE));

        UserResponse userResponse = createUserResponse.body().as(UserResponse.class);
        assertThat(userResponse.getId(), not(emptyOrNullString()));
        assertThat(userResponse.getMessage(), equalTo("Customer created"));
        ids.add(userResponse.getId());

        // 5. double check that user existing in system or not by getUserAPI
        Response getUserResponse = getUser(userResponse);

        // verify status
        getUserResponse.then().log().all().statusCode(200);
        // 7. verify

        UserDao actualUserDb = getUserFromDb(userResponse.getId());

        assertThat(actualUserDb, jsonEquals(userRequest)
                .whenIgnoringPaths(IGNORE_FIELD));

    }

    private static Response getUser(UserResponse userResponse) {
        return RestAssured.given().log().all()
                .header(CONTENT_TYPE_HEADER, REQUEST_CONTENT_TYPE_HEADER_VALUE)
                .header(AUTHORIZATION_HEADER, token)
                .get(GET_USER_API, userResponse.getId());
    }

    public static Response createUser(UserRequest userRequest) {
        return RestAssured.given().log().all()
                .header(CONTENT_TYPE_HEADER, REQUEST_CONTENT_TYPE_HEADER_VALUE)
                .header(AUTHORIZATION_HEADER, token)
                .body(userRequest)
                .post(CREATE_USER_API);
    }

}
