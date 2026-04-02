package tests;

import models.login.FieldRequiredResponseModel;
import models.login.LoginBodyModel;
import models.login.SuccessfulLoginResponseModel;
import models.registration.RegistrationBodyModel;
import models.registration.SuccessfulRegistrationResponseModel;
import models.update.*;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.qameta.allure.Allure.step;
import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static specs.BaseSpec.requestSpec;
import static specs.update.UpdateSpec.*;
import static specs.login.LoginSpec.successLoginResponseSpec;
import static specs.registration.RegistrationSpec.successRegistrationResponseSpec;
import static tests.TestData.*;

public class UpdateUserTests extends TestBase {
    TestData testData = new TestData();

    @Test
    @DisplayName("Успешное обновление данных пользователя")
    public void successfulUpdateUserDataTest() {
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
            Assertions.assertThat(registrationResponse.username()).isEqualTo(testData.username);
            Assertions.assertThat(registrationResponse.id()).isNotNull();
            Assertions.assertThat(registrationResponse.remoteAddr()).matches(REGISTRATION_IP_REGEXP);
        });

        String registrationIp = registrationResponse.remoteAddr();

        SuccessfulLoginResponseModel loginResponse =
                step("Аутентификация пользователя и получение токенов", () -> {
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

        step("Проверка наличия токенов", () -> {
            String actualAccess = loginResponse.access();
            String actualRefresh = loginResponse.refresh();

            Assertions.assertThat(actualAccess).isNotEqualTo(actualRefresh);
        });

        SuccessfulUpdateResponseModel updateResponse =
                step("Обновление данных пользователя", () -> {
                    UpdateBodyModel updateData = new UpdateBodyModel(testData.username, testData.firstName,
                            testData.lastName,
                            testData.email);
                    return given(requestSpec)
                            .header("Authorization", "Bearer " + loginResponse.access())
                            .body(updateData)
                            .when()
                            .put("/users/me/")
                            .then()
                            .spec(successUpdateResponseSpec)
                            .extract()
                            .as(SuccessfulUpdateResponseModel.class);
                });

        step("Проверка корректности обновленных данных пользователя", () -> {
            Assertions.assertThat(updateResponse.id()).isEqualTo(registrationResponse.id());
            Assertions.assertThat(updateResponse.username()).isEqualTo(testData.username);
            Assertions.assertThat(updateResponse.firstName()).isEqualTo(testData.firstName);
            Assertions.assertThat(updateResponse.lastName()).isEqualTo(testData.lastName);
            Assertions.assertThat(updateResponse.email()).isEqualTo(testData.email);

            String updateIp = updateResponse.remoteAddr();
            Assertions.assertThat(updateIp).matches(REGISTRATION_IP_REGEXP); // Проверка IP после обновления
            Assertions.assertThat(registrationIp).isEqualTo(updateIp);
        });
    }

    @Test
    @DisplayName("Успешное добавление email")
    public void successfulEmailUpdateTest() {
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
            Assertions.assertThat(registrationResponse.username()).isEqualTo(testData.username);
            Assertions.assertThat(registrationResponse.id()).isNotNull();
        });

        String registrationIp = registrationResponse.remoteAddr();

        SuccessfulLoginResponseModel loginResponse =
                step("Аутентификация пользователя и получение токенов", () -> {
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

        step("Проверка наличия токенов", () -> {
            String actualAccess = loginResponse.access();
            String actualRefresh = loginResponse.refresh();

            Assertions.assertThat(actualAccess).isNotEqualTo(actualRefresh);
        });

        SuccessfulUpdateResponseModel updateResponse =
                step("Обновление email пользователя", () -> {
                    UpdateEmailBodyModel updateEmailData = new UpdateEmailBodyModel(testData.email);
                    return given(requestSpec)
                            .header("Authorization", "Bearer " + loginResponse.access())
                            .body(updateEmailData)
                            .when()
                            .patch("/users/me/")
                            .then()
                            .spec(successUpdateResponseSpec)
                            .extract()
                            .as(SuccessfulUpdateResponseModel.class);
                });

        step("Проверка корректности обновленных данных, включая email", () -> {
            Assertions.assertThat(updateResponse.id()).isEqualTo(registrationResponse.id());
            Assertions.assertThat(updateResponse.username()).isEqualTo(testData.username);
            Assertions.assertThat(updateResponse.firstName()).isEqualTo("");
            Assertions.assertThat(updateResponse.lastName()).isEqualTo("");
            Assertions.assertThat(updateResponse.email()).isEqualTo(testData.email);

            Assertions.assertThat(registrationResponse.remoteAddr()).matches(REGISTRATION_IP_REGEXP);
            String updateIp = updateResponse.remoteAddr();
            Assertions.assertThat(updateIp).matches(REGISTRATION_IP_REGEXP);
            Assertions.assertThat(registrationIp).isEqualTo(updateIp);
        });
    }

    @Test
    @DisplayName("Поле username обязательно для заполнения")
    public void usernameFieldRequiredWhenUpdatingTest() {
        SuccessfulRegistrationResponseModel registrationResponse =
                step("Регистрация нового пользователя", () -> {
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
                step("Аутентификация пользователя и получение токенов", () -> {
                    LoginBodyModel loginData = new LoginBodyModel(testData.username, testData.password);
                    return given(requestSpec)
                            .body(loginData)
                            .when()
                            .post("/auth/token/")
                            .then()
                            .spec(successLoginResponseSpec)
                            .extract().as(SuccessfulLoginResponseModel.class);
                });

        step("Проверка наличия токенов", () -> {
            String actualAccess = loginResponse.access();
            String actualRefresh = loginResponse.refresh();

            Assertions.assertThat(actualAccess).isNotEqualTo(actualRefresh);
        });

        FieldRequiredResponseModel updateWithoutUsernameResponse =
                step("Попытка обновления пользователя без указания username", () -> {
                    UpdateWithoutUsernameBodyModel updateWithoutUsernameData = new UpdateWithoutUsernameBodyModel(testData.firstName,
                            testData.lastName,
                            testData.email);
                    return given(requestSpec)
                            .header("Authorization", "Bearer " + loginResponse.access())
                            .body(updateWithoutUsernameData)
                            .when()
                            .put("/users/me/")
                            .then()
                            .spec(fieldRequiredResponseSpec)
                            .extract()
                            .as(FieldRequiredResponseModel.class);
                });

        step("Проверка, что API вернул ошибку о незаполненном поле username", () -> {
            String actualUsernameError = updateWithoutUsernameResponse.username().get(0);
            Assertions.assertThat(actualUsernameError).isEqualTo(FIELD_IS_REQUIRED);
        });
    }
}
