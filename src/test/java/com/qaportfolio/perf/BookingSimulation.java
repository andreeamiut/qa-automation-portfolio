package com.qaportfolio.perf;

import com.qaportfolio.config.Config;
import com.qaportfolio.testdata.TestData;
import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;
import io.gatling.javaapi.http.HttpProtocolBuilder;

import java.time.Duration;
import java.time.LocalDate;

import static io.gatling.javaapi.core.CoreDsl.StringBody;
import static io.gatling.javaapi.core.CoreDsl.global;
import static io.gatling.javaapi.core.CoreDsl.jsonPath;
import static io.gatling.javaapi.core.CoreDsl.rampUsers;
import static io.gatling.javaapi.core.CoreDsl.scenario;
import static io.gatling.javaapi.http.HttpDsl.http;
import static io.gatling.javaapi.http.HttpDsl.status;

/**
 * PERF-01: browse + book + read. Run: mvn gatling:test [-DPERF_USERS=20].
 * The default load is deliberately small: restful-booker is a shared public demo.
 */
public class BookingSimulation extends Simulation {

    private final HttpProtocolBuilder httpProtocol = http
            .baseUrl(Config.apiBaseUrl())
            .acceptHeader("application/json")
            .contentTypeHeader("application/json");

    private final ScenarioBuilder browseAndBook = scenario("Browse and book")
            .exec(http("GET /booking").get("/booking").check(status().is(200)))
            .pause(Duration.ofSeconds(1))
            .exec(session -> session
                    .set("firstname", TestData.uniqueFirstName())
                    .set("checkin", LocalDate.now().plusDays(60).toString())
                    .set("checkout", LocalDate.now().plusDays(63).toString()))
            .exec(http("POST /booking")
                    .post("/booking")
                    .body(StringBody("""
                            {"firstname": "#{firstname}", "lastname": "Perf", "totalprice": 150, "depositpaid": true,
                             "bookingdates": {"checkin": "#{checkin}", "checkout": "#{checkout}"},
                             "additionalneeds": "None"}"""))
                    .check(status().is(200), jsonPath("$.bookingid").saveAs("bookingId")))
            .pause(Duration.ofSeconds(1))
            .exec(http("GET /booking/{id}").get("/booking/#{bookingId}").check(status().is(200)));

    {
        setUp(browseAndBook.injectOpen(
                rampUsers(Config.perfUsers()).during(Duration.ofSeconds(Config.perfRampSeconds()))))
                .protocols(httpProtocol)
                .assertions(
                        global().responseTime().percentile(95.0).lt(2000),
                        global().failedRequests().percent().lt(1.0));
    }
}
