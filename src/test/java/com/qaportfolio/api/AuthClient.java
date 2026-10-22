package com.qaportfolio.api;

import com.qaportfolio.api.models.Credentials;
import com.qaportfolio.config.Config;
import io.restassured.response.Response;

public class AuthClient extends BaseApiClient {

    public AuthClient() {
        super(Config.apiBaseUrl());
    }

    public Response ping() {
        return request().get("/ping");
    }

    public Response createToken(Credentials credentials) {
        return request().body(credentials).post("/auth");
    }

    public String validToken() {
        return createToken(new Credentials(Config.apiUser(), Config.apiPassword()))
                .then().statusCode(200)
                .extract().path("token");
    }
}
