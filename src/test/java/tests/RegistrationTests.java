package tests;

import models.login.FieldRequiredResponseModel;
import models.registration.RegistrationBodyModel;
import models.registration.SuccessfulRegistrationResponseModel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.qameta.allure.Allure.step;
import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static specs.BaseSpec.requestSpec;
import static specs.registration.RegistrationSpec.*;
import static tests.TestData.*;

public class RegistrationTests extends TestBase {
    TestData testData = new TestData();

    @Test
    @DisplayName("Регистрация с валидными данными")
    public void successfulRegistration() {
        SuccessfulRegistrationResponseModel registrationResponse =
                step("Регистрация пользователя, проверка статус-кода 201", () -> {
                    RegistrationBodyModel registrationData = new RegistrationBodyModel(testData.username, testData.password);
                    return given(requestSpec)
                            .body(registrationData)
                            .when()
                            .post("/users/register/")
                            .then()
                            .spec(successRegistrationResponseSpec)
                            .extract()
                            .as(SuccessfulRegistrationResponseModel.class);
                });

        step("Проверка данных пользователя после регистрации", () -> {
            assertThat(registrationResponse.username()).isEqualTo(testData.username);
            assertThat(registrationResponse.firstName()).isEqualTo("");
            assertThat(registrationResponse.lastName()).isEqualTo("");
            assertThat(registrationResponse.email()).isEqualTo("");
            assertThat(registrationResponse.remoteAddr()).matches(REGISTRATION_IP_REGEXP);
        });
    }

    @Test
    @DisplayName("Регистрация существующего пользователя")
    public void existingUserWrongRegistration() {
        SuccessfulRegistrationResponseModel firstRegistrationResponse =
                step("Регистрация пользователя", () -> {
                    RegistrationBodyModel registrationData = new RegistrationBodyModel(testData.username,
                            testData.password);
                    return given(requestSpec)
                            .body(registrationData)
                            .when()
                            .post("/users/register/")
                            .then()
                            .spec(successRegistrationResponseSpec)
                            .extract()
                            .as(SuccessfulRegistrationResponseModel.class);
                });

        step("Проверка данных пользователя после регистрации", () -> {
            assertThat(firstRegistrationResponse.username()).isEqualTo(testData.username);
        });

        FieldRequiredResponseModel secondRegistrationResponse =
                step("Повторная регистрация пользователя, проверка статус-кода 400", () -> {
                    RegistrationBodyModel registrationData = new RegistrationBodyModel(testData.username,
                            testData.password);
                    return given(requestSpec)
                            .body(registrationData)
                            .when()
                            .post("/users/register/")
                            .then()
                            .spec(negativeRegistrationResponseSpec)
                            .extract()
                            .as(FieldRequiredResponseModel.class);
                });

        step("Проверка сообщения об ошибке при регистрации существующего пользователя", () -> {
            String actualError = secondRegistrationResponse.username().get(0);
            assertThat(actualError).isEqualTo(REGISTRATION_EXISTING_USER_ERROR);
        });
    }

    @Test
    @DisplayName("Регистрация с пустым полем username")
    public void emptyUsernameFieldRegistration() {
        FieldRequiredResponseModel emptyUserResponseModel =
                step("Регистрация пользователя с незаполненным полем username", () -> {
                    RegistrationBodyModel registrationData = new RegistrationBodyModel("", testData.password);
                    return given(requestSpec)
                            .body(registrationData)
                            .when()
                            .post("/users/register/")
                            .then()
                            .spec(negativeRegistrationResponseSpec)
                            .extract()
                            .as(FieldRequiredResponseModel.class);
                });

        step("Проверка сообщения об ошибке для пустого поля username при регистрации", () -> {
            String actualError = emptyUserResponseModel.username().get(0);
            assertThat(actualError).isEqualTo(EMPTY_FIELD_ERROR);
        });
    }

    @Test
    @DisplayName("Регистрация c username длиной 151 символ")
    public void inputMoreThan150CharactersRegistration() {
        FieldRequiredResponseModel longUserResponseModel =
                step("Регистрация пользователя с username длиной 151 символ, проверка статус-кода 400", () -> {
                    RegistrationBodyModel registrationData = new RegistrationBodyModel(testData.longUsername,
                            testData.password);
                    return given(requestSpec)
                            .body(registrationData)
                            .when()
                            .post("/users/register/")
                            .then()
                            .spec(negativeRegistrationResponseSpec)
                            .extract()
                            .as(FieldRequiredResponseModel.class);
                });

        step("Проверка сообщения об ошибке для слишком длинного username при регистрации", () -> {
            String actualError = longUserResponseModel.username().get(0);
            assertThat(actualError).isEqualTo(MORE_THAN_150_CHARACTERS_ERROR);
        });
    }
}
