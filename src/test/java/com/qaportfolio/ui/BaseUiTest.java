package com.qaportfolio.ui;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.Tracing;
import com.qaportfolio.config.Config;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;

import java.nio.file.Paths;

/** One browser per class, a clean context per test, Playwright trace in target/traces. */
public abstract class BaseUiTest {

    private static Playwright playwright;
    private static Browser browser;

    private BrowserContext context;
    protected Page page;

    @BeforeAll
    static void launchBrowser() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(Config.headless()));
    }

    @AfterAll
    static void closeBrowser() {
        if (playwright != null) {
            playwright.close();
        }
    }

    @BeforeEach
    void createContext() {
        context = browser.newContext(new Browser.NewContextOptions().setViewportSize(1366, 900));
        context.tracing().start(new Tracing.StartOptions().setScreenshots(true).setSnapshots(true));
        page = context.newPage();
    }

    @AfterEach
    void closeContext(TestInfo testInfo) {
        String name = testInfo.getTestClass().map(Class::getSimpleName).orElse("ui")
                + "-" + testInfo.getTestMethod().map(m -> m.getName()).orElse("test");
        // open with: npx playwright show-trace target/traces/<name>.zip
        context.tracing().stop(new Tracing.StopOptions().setPath(Paths.get("target", "traces", name + ".zip")));
        context.close();
    }
}
