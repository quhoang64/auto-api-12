package testCase;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeAll;

import static utils.ConstantUtils.HOST;
import static utils.ConstantUtils.PORT;

public class TestMaster {
    @BeforeAll
    static void globalSetUp(){
        RestAssured.baseURI = HOST;
        RestAssured.port = PORT;
    }
}
