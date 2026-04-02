package specs;

import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;

import static allure.CustomAllureListener.withCustomTemplate;
import static io.restassured.RestAssured.with;
import static io.restassured.filter.log.LogDetail.ALL;
import static io.restassured.http.ContentType.JSON;

public class BaseSpec {
    public static RequestSpecification requestSpec = with()
            .filter(withCustomTemplate())
            .log().all()
            .contentType(JSON);

    public static ResponseSpecification baseResponseSpec = new ResponseSpecBuilder()
            .log(ALL)
            .build();
}
