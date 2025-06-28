package testCase.graphql;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import model.dto.graphql.QueryRequest;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import utils.IFileUtils;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static net.javacrumbs.jsonunit.JsonMatchers.jsonEquals;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static utils.ConstantUtils.*;

public class CountryQueryTest {
    @BeforeAll
    static void setup(){
        RestAssured.baseURI = "https://countries.trevorblades.com";
    }

    @Test
    void verifyCountryQuery() throws IOException {
        String queryFilePath = "graphql/graphql-query/country-query.graphql";
        String queryString = IFileUtils.readFileFromResources(queryFilePath);
        QueryRequest queryRequest = new QueryRequest();
        queryRequest.setQuery(queryString);
        Map<String, String> variables = new HashMap<>();
        variables.put("code", "VN");
        queryRequest.setVariables(variables);

        Response response = RestAssured.given().log().all()
                .header(CONTENT_TYPE_HEADER, "application/json; charset=utf-8")
                .body(queryRequest)
                .post();

        // Schema
        // Status code
        response.then().log().all().statusCode(200);
        // Headers
        response.then().header(CONTENT_TYPE_HEADER, equalTo(CONTENT_TYPE_HEADER_VALUE));
        // Body
        String actual = response.body().asString();
        String expectedPath = "graphql/expected/countryQueryExpected.json";
        String expected = IFileUtils.readFileFromResources(expectedPath);
        assertThat(actual, jsonEquals(expected));

    }
}
