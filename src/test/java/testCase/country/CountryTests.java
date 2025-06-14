package testCase.country;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.common.mapper.TypeRef;
import io.restassured.response.Response;
import model.country.Country;
import model.country.CountryPagination;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import testCase.TestMaster;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.util.List;
import java.util.stream.Stream;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;

public class CountryTests extends TestMaster {
    private static final String API_GET_COUNTRIES = "/api/v1/countries";
    private static final String API_GET_COUNTRY = "/api/v1/countries/{code}";


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
                .get(API_GET_COUNTRY ,"VN")
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
    void verifyGetCountry(Country country){
        Response response = RestAssured.given().log().all()
                .get("/api/v1/countries/{code}", country.getCode());

        // 1. Verify status code
        response.then().log().all().statusCode(200);

        // 2. Verify header
        response.then().header("X-Powered-By", equalTo("Express"))
                .header("Content-Type", equalTo("application/json; charset=utf-8"));
        // 3. verify body

        Country actualData = response.body().as(Country.class);
        assertThat(actualData,equalToObject(country));

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

    static Stream<Arguments> getCountryWithFilterProvider(){
        return Stream.of(
                Arguments.of(">", 5000, greaterThan(5000f)),
                Arguments.of("<", 5000, lessThan(5000f)),
                Arguments.of("<=", 5000, lessThanOrEqualTo(5000f)),
                Arguments.of(">=", 5000, greaterThanOrEqualTo(5000f)),
                Arguments.of("==", 5000, equalTo(5000f))
        );
    }

    @ParameterizedTest
    @MethodSource("getCountryWithFilterProvider")
    void verifyCountryApiWithFilterGreaterThan(String operator, int gdp, Matcher expected){
        Response response = RestAssured.given().log().all()
                .queryParam("gdp",gdp)
                .queryParam("operator", operator)
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
            assertThat(country.getGdp(), expected);
        }

    }

    @Test
    void verifySchemaGetCountriesWithPagination(){
        RestAssured.given().log().all()
                .queryParam("page",1)
                .queryParam("size",4)
                .get("api/v4/countries")
                .then()
                .log().all()
                .statusCode(200)
                .assertThat().body(matchesJsonSchemaInClasspath("json-schema/countries-pagination-schema.json"));
    }

    @Test
    void verifyGetCountriesWithPagination(){
        int testSize = 4;
        Response response = getCountryApiPagagination(1, testSize);

        // 1. Verify status code
        response.then().log().all().statusCode(200);

        // 2. Verify header
        response.then().header("X-Powered-By", equalTo("Express"))
                .header("Content-Type", equalTo("application/json; charset=utf-8"));
        // 3. verify body
        CountryPagination actualDataFirstPage = response.body().as(CountryPagination.class);
        verifyPage(actualDataFirstPage, 1, testSize, testSize);

        //4. second Page
        response = getCountryApiPagagination(2, testSize);

        CountryPagination actualDataSecondPage = response.body().as(CountryPagination.class);
        verifyPage(actualDataSecondPage, 2, testSize, testSize);
        //5. verify 2 page
        assertThat(actualDataFirstPage.getData().containsAll(actualDataSecondPage.getData()),equalTo(false));
        assertThat(actualDataSecondPage.getData().containsAll(actualDataFirstPage.getData()),equalTo(false));

        //6. verify last page
        int lastPage = actualDataFirstPage.getTotal()/ testSize;
        int sizeOfLastPage = actualDataFirstPage.getTotal() % testSize;
        if(sizeOfLastPage != 0){
            lastPage++;
        }else {
            sizeOfLastPage = 4;
        }

        response = getCountryApiPagagination(lastPage, testSize);

        CountryPagination actualDataLastPage = response.body().as(CountryPagination.class);
        verifyPage(actualDataLastPage, lastPage, testSize, sizeOfLastPage);
    }

    private static void verifyPage(CountryPagination pageData, int expectedPage, int expectedSize, int expectedLength) {
        assertThat(pageData.getPage(),equalTo(expectedPage));
        assertThat(pageData.getSize(),equalTo(expectedSize));
        assertThat(pageData.getData(),hasSize(expectedLength));
    }

    private static Response getCountryApiPagagination(int page, int size) {
        return RestAssured.given().log().all()
                .queryParam("page",page)
                .queryParam("size", size)
                .get("api/v4/countries");

    }

    @Test
    void verifySchemaGetCountriesWithHeader(){
        RestAssured.given().log().all()
                .header("api-key", "private")
                .get("api/v5/countries")
                .then()
                .log().all()
                .statusCode(200)
                .assertThat().body(matchesJsonSchemaInClasspath("json-schema/countries-private-schema.json"));
    }

}
