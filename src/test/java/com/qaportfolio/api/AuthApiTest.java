package com.qaportfolio.api;

import com.qaportfolio.api.models.Credentials;
import com.qaportfolio.config.Config;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.Matchers.blankOrNullString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

@Tag("api")
class AuthApiTest {

    private final AuthClient auth = new AuthClient();

    @Test
    @Tag("smoke")
    @DisplayName("API-01 health check returns 201")
    void healthCheck() {
        auth.ping().then().statusCode(201);
    }

    @Test
    @Tag("smoke")
    @DisplayName("API-02 valid credentials receive a token")
    void validCredentialsReturnToken() {
        auth.createToken(new Credentials(Config.apiUser(), Config.apiPassword()))
                .then()
                .statusCode(200)
                .body(matchesJsonSchemaInClasspath("schemas/auth_token.json"))
                .body("token", not(blankOrNullString()));
    }

    @Test
    @DisplayName("API-03 wrong credentials are rejected without a token")
    void invalidCredentialsRejected() {
        // restful-booker answers 200 with the reason in the body, not 401: test the actual behaviour
        auth.createToken(new Credentials(Config.apiUser(), "wrong-password"))
                .then()
                .statusCode(200)
                .body("reason", equalTo("Bad credentials"))
                .body("token", nullValue());
    }
}
