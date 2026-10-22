package com.qaportfolio.ui.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.qaportfolio.config.Config;

import java.util.regex.Pattern;

/** restful-booker-platform home page: rooms + contact form. */
public class HomePage {

    private final Page page;

    public HomePage(Page page) {
        this.page = page;
    }

    public HomePage open() {
        page.navigate(Config.uiBaseUrl());
        return this;
    }

    public Locator roomsHeading() {
        return page.getByRole(AriaRole.HEADING, new Page.GetByRoleOptions().setName(Pattern.compile("rooms", Pattern.CASE_INSENSITIVE)));
    }

    public Locator contactForm() {
        return page.locator("#name").locator("xpath=ancestor::form");
    }

    public HomePage fillContactForm(ContactMessage message) {
        page.locator("#name").fill(message.name());
        page.locator("#email").fill(message.email());
        page.locator("#phone").fill(message.phone());
        page.locator("#subject").fill(message.subject());
        page.locator("#description").fill(message.description());
        return this;
    }

    public HomePage submitContactForm() {
        contactForm().getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName("Submit")).click();
        return this;
    }

    public HomePage sendMessage(ContactMessage message) {
        return fillContactForm(message).submitContactForm();
    }

    /** Confirmation message: "Thanks for getting in touch <name>!" */
    public Locator confirmation() {
        return page.getByText(Pattern.compile("Thanks for getting in touch", Pattern.CASE_INSENSITIVE));
    }

    public Locator validationErrors() {
        return page.locator(".alert-danger");
    }

    public record ContactMessage(String name, String email, String phone, String subject, String description) {
    }
}
