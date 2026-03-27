package tests;

import models.login.LoginBodyModel;
import models.login.FieldRequiredResponseModel;
import models.login.SuccessfulLoginResponseModel;
import models.login.WrongCredentialsResponseModel;
import models.registration.RegistrationBodyModel;
import models.registration.SuccessfulRegistrationResponseModel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.qameta.allure.Allure.step;
import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static specs.BaseSpec.requestSpec;
import static specs.login.LoginSpec.*;
import static specs.registration.RegistrationSpec.*;
import static tests.TestData.*;

public class LoginTests extends TestBase {
    TestData testData = new TestData();

    @Test
    @DisplayName("Успешная авторизация")
    public void successfulLogin() {
        SuccessfulRegistrationResponseModel registrationResponse =
                step("Регистрация пользователя", () -> {
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

        SuccessfulLoginResponseModel loginResponse =
                step("Авторизация с корректными учетными данными, проверка статус-кода 200", () -> {
                    LoginBodyModel loginData = new LoginBodyModel(testData.username, testData.password);
                    return given(requestSpec)
                            .body(loginData)
                            .when()
                            .post("/auth/token/")
                            .then()
                            .spec(successLoginResponseSpec)
                            .extract().as(SuccessfulLoginResponseModel.class);
                });

        step("Проверка токенов доступа и обновления", () -> {
            String expectedTokenPath = LOGIN_TOKEN_PREFIX;
            String actualAccess = loginResponse.access();
            String actualRefresh = loginResponse.refresh();

            assertThat(actualAccess).startsWith(expectedTokenPath);
            assertThat(actualRefresh).startsWith(expectedTokenPath);
            assertThat(actualAccess).isNotEqualTo(actualRefresh);
        });
    }

    @Test
    @DisplayName("Вход в аккаунт с неверным паролем")
    public void wrongCredentialLogin() {
        SuccessfulRegistrationResponseModel registrationResponse =
                step("Регистрация пользователя", () -> {
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

        WrongCredentialsResponseModel loginResponse =
                step("Авторизация с неверным паролем, проверка статус-кода 401", () -> {
                    LoginBodyModel loginData = new LoginBodyModel(testData.username, testData.password + "1");
                    return given(requestSpec)
                            .body(loginData)
                            .when()
                            .post("/auth/token/")
                            .then()
                            .spec(wrongCredentialLoginResponseSpec)
                            .extract().as(WrongCredentialsResponseModel.class);
                });

        step("Проверка сообщения об ошибке при неверных учетных данных", () -> {
            String actualDetail = loginResponse.detail();
            assertThat(actualDetail).isEqualTo(LOGIN_WRONG_CREDENTIALS_ERROR);
        });
    }

    @Test
    @DisplayName("Вход в аккаунт с незаполненным полем username")
    public void emptyUsernameFieldLogin() {
        SuccessfulRegistrationResponseModel registrationResponse =
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
            assertThat(registrationResponse.username()).isEqualTo(testData.username);
            assertThat(registrationResponse.firstName()).isEqualTo("");
            assertThat(registrationResponse.lastName()).isEqualTo("");
            assertThat(registrationResponse.email()).isEqualTo("");
            assertThat(registrationResponse.remoteAddr()).matches(REGISTRATION_IP_REGEXP);
        });

        FieldRequiredResponseModel emptyUsernameLoginResponse =
                step("Авторизация с незаполненным полем username, проверка статус-кода 400", () -> {
                    LoginBodyModel loginData = new LoginBodyModel("", testData.password);
                    return given(requestSpec)
                            .body(loginData)
                            .when()
                            .post("/auth/token/")
                            .then()
                            .spec(emptyFieldLoginResponseSpec)
                            .extract()
                            .as(FieldRequiredResponseModel.class);
                });

        step("Проверка сообщения об ошибке для пустого поля username", () -> {
            String actualUsername = emptyUsernameLoginResponse.username().get(0);
            assertThat(actualUsername).isEqualTo(EMPTY_FIELD_ERROR);
        });
    }
}
