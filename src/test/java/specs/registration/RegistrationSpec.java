package specs.registration;

import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.specification.ResponseSpecification;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.Matchers.notNullValue;
import static specs.BaseSpec.baseResponseSpec;

public class RegistrationSpec {
    public static ResponseSpecification successRegistrationResponseSpec = new ResponseSpecBuilder()
            .addResponseSpecification(baseResponseSpec)
            .expectStatusCode(201)
            .expectBody(matchesJsonSchemaInClasspath(
                    "schemas/registration/successful_registration_response_schema.json"))
            .expectBody("id", notNullValue())
            .expectBody("username", notNullValue())
            .expectBody("remoteAddr", notNullValue())
            .build();

    public static ResponseSpecification negativeRegistrationResponseSpec = new ResponseSpecBuilder()
            .addResponseSpecification(baseResponseSpec)
            .expectStatusCode(400)
            .expectBody(matchesJsonSchemaInClasspath(
                    "schemas/registration/existing_registration_response_schema.json"))
            .expectBody("username", notNullValue())
            .build();
}
