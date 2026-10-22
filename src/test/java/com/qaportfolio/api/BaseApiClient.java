package com.qaportfolio.api;

import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.LogDetail;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;

import static io.restassured.RestAssured.given;

public abstract class BaseApiClient {

    private final RequestSpecification spec;

    protected BaseApiClient(String baseUrl) {
        this.spec = new RequestSpecBuilder()
                .setBaseUri(baseUrl)
                .setContentType(ContentType.JSON)
                // restful-booker answers 418 unless Accept is exactly application/json;
                // ContentType.JSON would send a list of several JSON media types
                .setAccept("application/json")
                .log(LogDetail.METHOD)
                .log(LogDetail.URI)
                // attaches each request/response to the Allure report
                .addFilter(new AllureRestAssured())
                .build();
    }

    protected RequestSpecification request() {
        return given().spec(spec);
    }
}
