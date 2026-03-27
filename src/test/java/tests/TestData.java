package tests;

import net.datafaker.Faker;

import java.util.Locale;

public class TestData {
    Faker faker = new Faker(new Locale("en"));
    public String username = faker.name().firstName(),
            password = faker.credentials().password(),
            longUsername = faker.lorem().characters(151),
            firstName = faker.name().firstName(),
            lastName = faker.name().lastName(),
            email = faker.internet().emailAddress();

    public static final String REFRESH_NULL = null;

    public static final String REFRESH_INVALID = "string";

    public static final String LOGIN_WRONG_CREDENTIALS_ERROR = "Invalid username or password.";

    public static final String REGISTRATION_EXISTING_USER_ERROR =
            "A user with that username already exists.";

    public static final String EMPTY_FIELD_ERROR =
            "This field may not be blank.";

    public static final String MORE_THAN_150_CHARACTERS_ERROR =
            "Ensure this field has no more than 150 characters.";

    public static final String FIELD_IS_REQUIRED =
            "This field is required.";

    public static final String NULL_FIELD_ERROR =
            "This field may not be null.";

    public static final String TOKEN_INVALID_ERROR =
            "Token is invalid";

    public static final String TOKEN_NOT_VALID_ERROR =
            "token_not_valid";

    public static final String REGISTRATION_IP_REGEXP =
            "^((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)\\.){3}"
                    + "(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)$";

    public static final String LOGIN_TOKEN_PREFIX = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9";
}
