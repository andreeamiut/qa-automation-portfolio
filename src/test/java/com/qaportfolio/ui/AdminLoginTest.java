package com.qaportfolio.ui;

import com.qaportfolio.config.Config;
import com.qaportfolio.ui.pages.AdminLoginPage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.assertj.core.api.Assertions.assertThat;

@Tag("ui")
class AdminLoginTest extends BaseUiTest {

    @Test
    @DisplayName("UI-04a admin logs in with valid credentials")
    void adminLogsIn() {
        AdminLoginPage login = new AdminLoginPage(page).open();

        int status = login.loginAs(Config.adminUser(), Config.adminPassword());

        assertThat(status).as("login status").isEqualTo(200);
        assertThat(login.loginButton()).isHidden();
    }

    @Test
    @DisplayName("UI-04b wrong credentials give no access to the admin panel")
    void wrongPasswordStaysOnLogin() {
        AdminLoginPage login = new AdminLoginPage(page).open();

        int status = login.loginAs(Config.adminUser(), "wrong-password");

        assertThat(status).as("login status").isEqualTo(401);
        assertThat(login.loginButton()).isVisible();
    }
}
