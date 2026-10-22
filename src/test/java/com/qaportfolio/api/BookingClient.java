package com.qaportfolio.api;

import com.qaportfolio.api.models.Booking;
import com.qaportfolio.config.Config;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import java.util.Map;

public class BookingClient extends BaseApiClient {

    public BookingClient() {
        super(Config.apiBaseUrl());
    }

    public Response list() {
        return request().get("/booking");
    }

    public Response create(Booking booking) {
        return request().body(booking).post("/booking");
    }

    public Response get(int id) {
        return request().get("/booking/{id}", id);
    }

    public Response update(int id, Booking booking, String token) {
        return authorized(token).body(booking).put("/booking/{id}", id);
    }

    public Response partialUpdate(int id, Map<String, Object> fields, String token) {
        return authorized(token).body(fields).patch("/booking/{id}", id);
    }

    public Response delete(int id, String token) {
        return authorized(token).delete("/booking/{id}", id);
    }

    /** token == null sends the request unauthenticated (for negative tests). */
    private RequestSpecification authorized(String token) {
        RequestSpecification req = request();
        return token == null ? req : req.cookie("token", token);
    }
}
