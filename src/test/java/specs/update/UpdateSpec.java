package specs.update;

import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.specification.ResponseSpecification;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.Matchers.notNullValue;
import static specs.BaseSpec.baseResponseSpec;

public class UpdateSpec {
    public static ResponseSpecification successUpdateResponseSpec = new ResponseSpecBuilder()
            .addResponseSpecification(baseResponseSpec)
            .expectStatusCode(200)
            .expectBody(matchesJsonSchemaInClasspath("schemas/update/successful_update_response_schema.json"))
            .expectBody("id", notNullValue())
            .expectBody("username", notNullValue())
            .build();

    public static ResponseSpecification fieldRequiredResponseSpec = new ResponseSpecBuilder()
            .addResponseSpecification(baseResponseSpec)
            .expectStatusCode(400)
            .expectBody(matchesJsonSchemaInClasspath("schemas/login/empty_field_response_schema.json"))
            .expectBody("username", notNullValue())
            .build();
}
