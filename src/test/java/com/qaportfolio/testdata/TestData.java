package com.qaportfolio.testdata;

import com.qaportfolio.api.models.Booking;
import com.qaportfolio.api.models.BookingDates;

import java.time.LocalDate;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/** Unique data per test, so tests don't step on each other on shared environments. */
public final class TestData {

    private TestData() {
    }

    public static String uniqueSuffix() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    public static String uniqueFirstName() {
        return "QA" + uniqueSuffix();
    }

    public static Booking booking() {
        LocalDate checkin = LocalDate.now().plusDays(ThreadLocalRandom.current().nextInt(30, 300));
        return new Booking(
                uniqueFirstName(),
                "Automation",
                ThreadLocalRandom.current().nextInt(50, 1000),
                true,
                new BookingDates(checkin.toString(), checkin.plusDays(3).toString()),
                "Breakfast");
    }

    // --- platform contact form ---
    public static String phone() {
        return "0740" + ThreadLocalRandom.current().nextInt(1_000_000, 9_999_999);
    }

    public static String email() {
        return "qa." + uniqueSuffix() + "@example.com";
    }

    /** Unique subject, used as the key to find the message through the API. */
    public static String uniqueSubject(String prefix) {
        return prefix + " " + uniqueSuffix();
    }
}
