package com.qaportfolio.ui.selenium;

import com.qaportfolio.config.Config;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

/** Same flow as in Playwright, with Selenium: shows the POM is not tied to a single tool. */
@Tag("ui")
class ContactFormSeleniumTest {

    private WebDriver driver;
    private WebDriverWait wait;

    @BeforeEach
    void startDriver() {
        ChromeOptions options = new ChromeOptions();
        if (Config.headless()) {
            options.addArguments("--headless=new");
        }
        options.addArguments("--window-size=1366,900");
        // Selenium Manager downloads the matching driver automatically
        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterEach
    void quitDriver() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    @DisplayName("UI-05 Selenium: too-short subject and message are rejected")
    void tooShortFieldsRejected() {
        driver.get(Config.uiBaseUrl());

        type(By.id("name"), "QA Selenium");
        type(By.id("email"), "qa.selenium@example.com");
        type(By.id("phone"), "07401234567");
        type(By.id("subject"), "Hi");          // minimum 5 characters
        type(By.id("description"), "Short");   // minimum 20 characters
        WebElement submit = driver.findElement(By.xpath("//form[.//*[@id='name']]//button[normalize-space()='Submit']"));
        // the form sits at the bottom of a long page; Bootstrap enables smooth scrolling, so scroll
        // instantly or Selenium clicks while the page is still moving
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].scrollIntoView({block: 'center', behavior: 'instant'});", submit);
        wait.until(ExpectedConditions.elementToBeClickable(submit)).click();

        WebElement errors = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".alert-danger")));
        assertThat(errors.getText())
                .contains("Subject must be between 5 and 100 characters")
                .contains("Message must be between 20 and 2000 characters");
    }

    private void type(By locator, String text) {
        WebElement field = wait.until(ExpectedConditions.elementToBeClickable(locator));
        field.clear();
        field.sendKeys(text);
    }
}
