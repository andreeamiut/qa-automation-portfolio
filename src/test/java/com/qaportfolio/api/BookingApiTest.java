package com.qaportfolio.api;

import com.qaportfolio.api.models.Booking;
import com.qaportfolio.api.models.CreatedBooking;
import com.qaportfolio.testdata.TestData;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;

@Tag("api")
class BookingApiTest {

    private static final BookingClient bookings = new BookingClient();
    private static String token;

    @BeforeAll
    static void authenticate() {
        token = new AuthClient().validToken();
    }

    /** Each test creates its own booking: no ordering dependencies. */
    private static CreatedBooking givenBooking(Booking booking) {
        return bookings.create(booking).then().statusCode(200).extract().as(CreatedBooking.class);
    }

    @Test
    @Tag("smoke")
    @DisplayName("API-04 create booking: contract + data echoed back")
    void createBooking() {
        Booking booking = TestData.booking();

        CreatedBooking created = bookings.create(booking)
                .then()
                .statusCode(200)
                .body(matchesJsonSchemaInClasspath("schemas/booking_created.json"))
                .extract().as(CreatedBooking.class);

        assertThat(created.bookingid()).isPositive();
        assertThat(created.booking()).isEqualTo(booking);
    }

    @Test
    @DisplayName("API-05 get booking by id")
    void getBookingById() {
        Booking booking = TestData.booking();
        int id = givenBooking(booking).bookingid();

        Booking fetched = bookings.get(id)
                .then()
                .statusCode(200)
                .body(matchesJsonSchemaInClasspath("schemas/booking.json"))
                .extract().as(Booking.class);

        assertThat(fetched).isEqualTo(booking);
    }

    @Test
    @DisplayName("API-06 full update (PUT) with token")
    void updateBooking() {
        Booking original = TestData.booking();
        int id = givenBooking(original).bookingid();
        Booking changed = original.withFirstname(TestData.uniqueFirstName()).withTotalprice(1234);

        bookings.update(id, changed, token).then().statusCode(200);

        assertThat(bookings.get(id).as(Booking.class)).isEqualTo(changed);
    }

    @Test
    @DisplayName("API-07 partial update (PATCH) changes only the fields sent")
    void partialUpdateBooking() {
        Booking original = TestData.booking();
        int id = givenBooking(original).bookingid();

        bookings.partialUpdate(id, Map.of("additionalneeds", "Late checkout"), token)
                .then()
                .statusCode(200)
                .body("additionalneeds", equalTo("Late checkout"))
                .body("firstname", equalTo(original.firstname()));
    }

    @Test
    @DisplayName("API-08 update without token is forbidden and leaves data unchanged")
    void updateWithoutTokenForbidden() {
        Booking original = TestData.booking();
        int id = givenBooking(original).bookingid();

        bookings.update(id, original.withFirstname("Hacker"), null).then().statusCode(403);

        assertThat(bookings.get(id).as(Booking.class)).isEqualTo(original);
    }

    @Test
    @DisplayName("API-09 delete: 201, then the booking no longer exists")
    void deleteBooking() {
        int id = givenBooking(TestData.booking()).bookingid();

        // restful-booker answers 201 Created to DELETE: documented behaviour of the demo
        bookings.delete(id, token).then().statusCode(201);
        bookings.get(id).then().statusCode(404);
    }
}
