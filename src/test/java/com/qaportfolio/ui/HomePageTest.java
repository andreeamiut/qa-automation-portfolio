package com.qaportfolio.ui;

import com.qaportfolio.testdata.TestData;
import com.qaportfolio.ui.pages.HomePage;
import com.qaportfolio.ui.pages.HomePage.ContactMessage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

@Tag("ui")
class HomePageTest extends BaseUiTest {

    @Test
    @Tag("smoke")
    @DisplayName("UI-01 home page shows the rooms and the contact form")
    void homePageLoads() {
        HomePage home = new HomePage(page).open();

        assertThat(home.roomsHeading().first()).isVisible();
        assertThat(home.contactForm()).isVisible();
    }

    @Test
    @DisplayName("UI-02 valid contact message is confirmed")
    void validContactMessageIsConfirmed() {
        ContactMessage message = new ContactMessage(
                "QA Tester", TestData.email(), TestData.phone(),
                TestData.uniqueSubject("Parking question"),
                "Hello, is there free parking near the hotel for a small car? Thank you.");

        HomePage home = new HomePage(page).open().sendMessage(message);

        assertThat(home.confirmation()).isVisible();
        assertThat(home.confirmation()).containsText(message.name());
    }

    @Test
    @DisplayName("UI-03 empty form shows validation errors")
    void emptyContactFormShowsValidationErrors() {
        HomePage home = new HomePage(page).open().submitContactForm();

        assertThat(home.validationErrors()).isVisible();
        assertThat(home.validationErrors()).containsText("Name may not be blank");
        assertThat(home.validationErrors()).containsText("Email may not be blank");
        assertThat(home.confirmation()).not().isVisible();
    }
}
