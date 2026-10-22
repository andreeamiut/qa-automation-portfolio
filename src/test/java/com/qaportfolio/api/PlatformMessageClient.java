package com.qaportfolio.api;

import com.qaportfolio.config.Config;
import io.restassured.path.json.JsonPath;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Contact messages in restful-booker-platform (admin endpoint, cookie authentication). */
public class PlatformMessageClient extends BaseApiClient {

    private String token;

    public PlatformMessageClient() {
        super(Config.platformApiUrl());
    }

    public PlatformMessageClient login() {
        token = request()
                .body(Map.of("username", Config.adminUser(), "password", Config.adminPassword()))
                .post("/auth/login")
                .then().statusCode(200)
                .extract().path("token");
        return this;
    }

    public Optional<Integer> findIdBySubject(String subject) {
        List<Map<String, Object>> messages = request().cookie("token", token)
                .get("/message")
                .then().statusCode(200)
                .extract().jsonPath().getList("messages");
        return messages.stream()
                .filter(m -> subject.equals(m.get("subject")))
                .map(m -> ((Number) m.get("id")).intValue())
                .findFirst();
    }

    public JsonPath get(int id) {
        return request().cookie("token", token)
                .get("/message/{id}", id)
                .then().statusCode(200)
                .extract().jsonPath();
    }
}
