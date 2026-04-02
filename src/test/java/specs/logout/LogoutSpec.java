package specs.logout;

import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.specification.ResponseSpecification;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.Matchers.*;
import static specs.BaseSpec.baseResponseSpec;

public class LogoutSpec {
    public static ResponseSpecification successLogoutResponseSpec = new ResponseSpecBuilder()
            .addResponseSpecification(baseResponseSpec)
            .expectStatusCode(200)
            .build();

    public static ResponseSpecification nullFieldLogoutResponseSpec = new ResponseSpecBuilder()
            .addResponseSpecification(baseResponseSpec)
            .expectStatusCode(400)
            .expectBody(matchesJsonSchemaInClasspath("schemas/logout/null_field_logout_response_schema.json"))
            .expectBody("refresh", notNullValue())
            .build();

    public static ResponseSpecification unauthorizedLogoutResponseSpec = new ResponseSpecBuilder()
            .addResponseSpecification(baseResponseSpec)
            .expectStatusCode(401)
            .expectBody(matchesJsonSchemaInClasspath("schemas/logout/unauthorized_logout_response_schema.json"))
            .expectBody("detail", notNullValue())
            .expectBody("code", notNullValue())
            .build();
}
