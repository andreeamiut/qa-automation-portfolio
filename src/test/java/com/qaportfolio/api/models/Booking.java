package com.qaportfolio.api.models;

public record Booking(String firstname, String lastname, int totalprice, boolean depositpaid,
                      BookingDates bookingdates, String additionalneeds) {

    public Booking withFirstname(String newFirstname) {
        return new Booking(newFirstname, lastname, totalprice, depositpaid, bookingdates, additionalneeds);
    }

    public Booking withTotalprice(int newTotalprice) {
        return new Booking(firstname, lastname, newTotalprice, depositpaid, bookingdates, additionalneeds);
    }
}
