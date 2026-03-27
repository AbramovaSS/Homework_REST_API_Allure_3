package tests;

import io.restassured.response.Response;
import models.login.LoginBodyModel;
import models.login.SuccessfulLoginResponseModel;
import models.logout.FieldNullResponseModel;
import models.logout.LogoutBodyModel;
import models.logout.UnauthorizedResponseModel;
import models.registration.RegistrationBodyModel;
import models.registration.SuccessfulRegistrationResponseModel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.qameta.allure.Allure.step;
import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static specs.BaseSpec.requestSpec;
import static specs.login.LoginSpec.successLoginResponseSpec;
import static specs.logout.LogoutSpec.*;
import static specs.registration.RegistrationSpec.successRegistrationResponseSpec;
import static tests.TestData.*;

public class LogoutTests extends TestBase {
    TestData testData = new TestData();

    @Test
    @DisplayName("Успешный выход из учетной записи")
    public void successfulLogout() {
        SuccessfulRegistrationResponseModel registrationResponse =
                step("Регистрация нового пользователя", () -> {
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

        SuccessfulLoginResponseModel loginResponse =
                step("Вход в аккаунт зарегистрированного пользователя", () -> {
                    LoginBodyModel loginData = new LoginBodyModel(testData.username, testData.password);
                    return given(requestSpec)
                            .body(loginData)
                            .when()
                            .post("/auth/token/")
                            .then()
                            .spec(successLoginResponseSpec)
                            .extract()
                            .as(SuccessfulLoginResponseModel.class);
                });

        String actualTokenRefresh = step("Извлечение refresh-токена из ответа", () -> {
            String refreshToken = loginResponse.refresh();
            assertThat(refreshToken).isNotBlank();
            return refreshToken;
        });

        Response logoutResponse = step("Выход из аккаунта, проверка статус-кода 200", () -> {
            LogoutBodyModel logoutData = new LogoutBodyModel(actualTokenRefresh);
            return given(requestSpec)
                    .body(logoutData)
                    .when()
                    .post("/auth/logout/")
                    .then()
                    .spec(successLogoutResponseSpec)
                    .extract()
                    .response();
        });

        step("Проверка пустого тела ответа после выхода", () -> {
            assertThat(logoutResponse.body().asString()).isEqualTo("{}");
        });
    }

    @Test
    @DisplayName("Refresh = null")
    public void transmittingZeroRefresh() {
        FieldNullResponseModel refreshNullResponse =
                step("Выполнение запроса на выход с пустым refresh-токеном", () -> {
                    LogoutBodyModel logoutData = new LogoutBodyModel(REFRESH_NULL);
                    return given(requestSpec)
                            .body(logoutData)
                            .when()
                            .post("/auth/logout/")
                            .then()
                            .spec(nullFieldLogoutResponseSpec)
                            .extract()
                            .as(FieldNullResponseModel.class);
                });

        step("Проверка ошибки для пустого refresh-токена", () -> {
            String actualRefreshError = refreshNullResponse.refresh().get(0);
            assertThat(actualRefreshError).isEqualTo(NULL_FIELD_ERROR);
        });
    }

    @Test
    @DisplayName("Невалидный refresh")
    public void passingInvalidRefresh() {
        UnauthorizedResponseModel logoutUnauthorizedResponse =
                step("Выполнение запроса на выход с невалидным refresh-токеном", () -> {
                    LogoutBodyModel logoutData = new LogoutBodyModel(REFRESH_INVALID);
                    return given(requestSpec)
                            .body(logoutData)
                            .when()
                            .post("/auth/logout/")
                            .then()
                            .spec(unauthorizedLogoutResponseSpec)
                            .extract()
                            .as(UnauthorizedResponseModel.class);
                });
        step("Проверка ошибки для невалидного refresh-токена", () -> {
            String actualDetailError = logoutUnauthorizedResponse.detail();
            String actualCodeError = logoutUnauthorizedResponse.code();
            assertThat(actualDetailError).isEqualTo(TOKEN_INVALID_ERROR);
            assertThat(actualCodeError).isEqualTo(TOKEN_NOT_VALID_ERROR);
        });
    }
}
