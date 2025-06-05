package country;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.common.mapper.TypeRef;
import io.restassured.response.Response;
import model.Country;
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
    private static final String API_GET_COUNTRIES = "/api/v1/countries";

    @BeforeAll
    static void setUp(){
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = 3000;
    }

    @Test
    void verifySchemaGetCountries(){
        RestAssured.given().log().all()
                .get(API_GET_COUNTRIES)
                .then()
                .log().all()
                .statusCode(200)
                .assertThat().body(matchesJsonSchemaInClasspath("json-schema/countries-schema.json"));
    }

    @Test
    void verifyGetCountriesData() throws JsonProcessingException {
        Response response = RestAssured.given().log().all()
                .get(API_GET_COUNTRIES);

        // 1. Verify status code
        response.then().log().all().statusCode(200);

        // 2. Verify header
        response.then().header("X-Powered-By", equalTo("Express"))
                .header("Content-Type", equalTo("application/json; charset=utf-8"));

        // 3. Verify Body
        ObjectMapper mapper = new ObjectMapper();
        List<Country> expected = mapper.readValue(CountriesData.ALL_COUNTRIES_DATA,new TypeReference<>(){
        });

        List<Country> actual = response.body().as(new TypeRef<>() {
        });

        assertThat(actual.size(), equalTo(expected.size()));
        assertThat(actual.containsAll(expected), equalTo(true) );
        assertThat(expected.containsAll(actual), equalTo(true) );
    }

    @Test
    void verifySchemaGetCountry(){
        RestAssured.given().log().all()
                .get(API_GET_COUNTRIES ,"VN")
                .then()
                .log().all()
                .statusCode(200)
                .assertThat().body(matchesJsonSchemaInClasspath("json-schema/country-schema.json"));
    }

    static Stream<Country> countryProvider() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        List<Country> inputData = mapper.readValue(CountriesData.ALL_COUNTRIES_DATA,new TypeReference<>(){
        });
        return inputData.stream();
    }


    @ParameterizedTest
    @MethodSource("countryProvider")
    void verifyGetCountry(Country input){
        Response response = RestAssured.given().log().all()
                .get("/api/v1/countries/{code}", input.getCode());

        // 1. Verify status code
        response.then().log().all().statusCode(200);

        // 2. Verify header
        response.then().header("X-Powered-By", equalTo("Express"))
                .header("Content-Type", equalTo("application/json; charset=utf-8"));
        // 3. verify body

        Country actualData = response.body().as(Country.class);
        assertThat(actualData,equalToObject(input));

    }

    @Test
    void verifySchemaGetCountriesWithFilter(){
        RestAssured.given().log().all()
                .queryParam("gdp",5000)
                .queryParam("operator", ">")
                .get("api/v3/countries")
                .then()
                .log().all()
                .statusCode(200)
                .assertThat().body(matchesJsonSchemaInClasspath("json-schema/countries-filter-schema.json"));
    }

    @Test
    void verifyCountryApiWithFilterGreaterThan(){
        Response response = RestAssured.given().log().all()
                .queryParam("gdp",5000)
                .queryParam("operator", ">")
                .get("api/v3/countries");

        // 1. Verify status code
        response.then().log().all().statusCode(200);

        // 2. Verify header
        response.then().header("X-Powered-By", equalTo("Express"))
                .header("Content-Type", equalTo("application/json; charset=utf-8"));
        // 3. verify body

        List<Country> actual = response.body().as(new TypeRef<>() {
        });

        for(Country country : actual){
            assertThat(country.getGdp(), greaterThan(5000f));
        }
    }
}
