package country;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.common.mapper.TypeRef;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;

public class CountryTests {

    @BeforeAll
    static void setUp(){
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = 3000;
    }

    @Test
    void verifySchemaGetCountries(){
        RestAssured.given().log().all()
                .get("/api/v1/countries")
                .then()
                .log().all()
                .statusCode(200)
                .assertThat().body(matchesJsonSchemaInClasspath("json-schema/countries-schema.json"));
    }

    @Test
    void verifyGetCountriesData() throws JsonProcessingException {
        Response response = RestAssured.given().log().all()
                .get("/api/v1/countries");

        // 1. Verify status code
        response.then().log().all().statusCode(200);

        // 2. Verify header
        response.then().header("X-Powered-By", equalTo("Express"))
                .header("Content-Type", equalTo("application/json; charset=utf-8"));

        // 3. Verify Body
        ObjectMapper mapper = new ObjectMapper();
        List<Map<String, String>> expected = mapper.readValue(CountriesData.ALL_COUNTRIES_DATA,new TypeReference<List<Map<String, String>>>(){
        });

        List<Map<String, String>> actual = response.body().as(new TypeRef<List<Map<String, String>>>() {
        });
        assertThat(actual.size(), equalTo(expected.size()));
        assertThat(actual.containsAll(expected), equalTo(true) );
        assertThat(expected.containsAll(actual), equalTo(true) );
    }

    @Test
    void verifySchemaGetCountry(){
        RestAssured.given().log().all()
                .get("/api/v1/countries/VN")
                .then()
                .log().all()
                .statusCode(200)
                .assertThat().body(matchesJsonSchemaInClasspath("json-schema/country-schema.json"));
    }

    static Stream<Map<String, String>> countryProvider() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        List<Map<String, String>> inputData = mapper.readValue(CountriesData.ALL_COUNTRIES_DATA,new TypeReference<List<Map<String, String>>>(){
        });
        return inputData.stream();
    }


    @ParameterizedTest
    @MethodSource("countryProvider")
    void verifyGetCountry(Map<String, String> input){
        Response response = RestAssured.given().log().all()
                .get("/api/v1/countries/{code}", input.get("code"));

        // 1. Verify status code
        response.then().log().all().statusCode(200);

        // 2. Verify header
        response.then().header("X-Powered-By", equalTo("Express"))
                .header("Content-Type", equalTo("application/json; charset=utf-8"));
        // 3. verify body

        Map<String, String> actualData = response.body().as(new TypeRef<Map<String, String>>() {
        });
        assertThat(actualData,equalToObject(input));

    }

}
