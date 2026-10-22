package com.qaportfolio.ui.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.qaportfolio.config.Config;

public class AdminLoginPage {

    private final Page page;

    public AdminLoginPage(Page page) {
        this.page = page;
    }

    public AdminLoginPage open() {
        page.navigate(Config.uiBaseUrl() + "/admin");
        return this;
    }

    /**
     * Submits the form and returns the HTTP status of the login call.
     * The navbar always shows a Logout button, even logged out, so the UI alone can't tell success from failure.
     */
    public int loginAs(String username, String password) {
        page.locator("#username").fill(username);
        page.locator("#password").fill(password);
        return page.waitForResponse(
                response -> response.url().contains("/api/auth/login"),
                () -> page.locator("#doLogin").click()
        ).status();
    }

    public Locator loginButton() {
        return page.locator("#doLogin");
    }
}
