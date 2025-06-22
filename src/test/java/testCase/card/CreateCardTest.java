package testCase.card;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import model.dto.card.CreateCardRequest;
import model.dto.card.CreateCardResponse;
import model.dto.user.UserAddressRequest;
import model.dto.user.UserRequest;
import model.dto.user.UserResponse;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import testCase.TestMaster;
import utils.MockUtils;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.equalToObject;
import static testCase.user.CreateUserTest.createUser;
import static utils.ConstantUtils.*;
import static utils.ConstantUtils.DELETE_USER_API;

public class CreateCardTest extends TestMaster {
    private static List<String> ids = new ArrayList<>();

    @BeforeAll
    static void setUp(){
        MockUtils.startAllMockServer();
    }

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
    void verifyCreateCardSuccessful(){
        // Create UserAddress
        UserAddressRequest userAddressRequest = UserAddressRequest.getDefault();
        // Create User
        UserRequest userRequest = UserRequest.getDefault();
        userRequest.setEmail(String.format(EMAIL_TEMPLATE, System.currentTimeMillis()));
        userRequest.setAddresses(List.of(userAddressRequest));
        Response createUserResponse = createUser(userRequest);
        createUserResponse.then().log().all().statusCode(200);
        UserResponse createUserObj = createUserResponse.body().as(UserResponse.class);
        // Create Card
        CreateCardRequest cardRequest = new CreateCardRequest(createUserObj.getId(), "SILVER");
        Response createCardResponse = RestAssured.given().log().all()
                .header(AUTHORIZATION_HEADER, token)
                .header(CONTENT_TYPE_HEADER, CONTENT_TYPE_HEADER_VALUE)
                .body(cardRequest)
                .post(CREATE_CARD_API);
        // verify status
        createCardResponse.then().log().all().statusCode(200);
        // verify header
        createCardResponse.then().header(X_POWER_BY_HEADER, equalTo(X_POWER_BY_HEADER_VALUE))
                .header(CONTENT_TYPE_HEADER, equalTo(CONTENT_TYPE_HEADER_VALUE));
        // Verify Body
        CreateCardResponse actualCard = createCardResponse.body().as(CreateCardResponse.class);
        CreateCardResponse expectedCard = new CreateCardResponse(String.format("%s %s", userRequest.getLastName(), userRequest.getFirstName()),
        "1111 2222 3333 4444", "01-23-2028");
        assertThat(actualCard, equalToObject(expectedCard));


    }
}
