package specs.login;

import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.specification.*;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.Matchers.notNullValue;
import static specs.BaseSpec.baseResponseSpec;

public class LoginSpec {

    public static ResponseSpecification successLoginResponseSpec = new ResponseSpecBuilder()
            .addResponseSpecification(baseResponseSpec)
            .expectStatusCode(200)
            .expectBody(matchesJsonSchemaInClasspath("schemas/login/successful_login_response_schema.json"))
            .expectBody("access", notNullValue())
            .expectBody("refresh", notNullValue())
            .build();

    public static ResponseSpecification wrongCredentialLoginResponseSpec = new ResponseSpecBuilder()
            .addResponseSpecification(baseResponseSpec)
            .expectStatusCode(401)
            .expectBody(matchesJsonSchemaInClasspath("schemas/login/wrong_credentials_login_response_schema.json"))
            .expectBody("detail", notNullValue())
            .build();

    public static ResponseSpecification emptyFieldLoginResponseSpec = new ResponseSpecBuilder()
            .addResponseSpecification(baseResponseSpec)
            .expectStatusCode(400)
            .expectBody(matchesJsonSchemaInClasspath("schemas/login/empty_field_response_schema.json"))
            .expectBody("username", notNullValue())
            .build();
}
