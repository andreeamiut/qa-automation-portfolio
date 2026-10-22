package com.qaportfolio.integration;

import com.qaportfolio.api.PlatformMessageClient;
import com.qaportfolio.llm.LlmFixtures;
import com.qaportfolio.llm.OutputContract;
import com.qaportfolio.testdata.TestData;
import com.qaportfolio.triage.TriageResult;
import com.qaportfolio.ui.BaseUiTest;
import com.qaportfolio.ui.pages.HomePage;
import com.qaportfolio.ui.pages.HomePage.ContactMessage;
import io.restassured.path.json.JsonPath;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/** Full flow: guest writes in the UI -> message reaches the API -> the LLM service triages it. */
@Tag("integration")
@Tag("llm")
class ContactTriageIntegrationTest extends BaseUiTest {

    @Test
    @DisplayName("INT-03 message sent from the UI, read through the API, triaged correctly by the LLM")
    void contactMessageIsTriagedEndToEnd() {
        LlmFixtures.requireCredentials();
        String subject = TestData.uniqueSubject("No hot water");
        ContactMessage sent = new ContactMessage(
                "QA Integration", TestData.email(), TestData.phone(), subject,
                "We are staying in room 2 right now and there has been no hot water since last night. "
                        + "Reception says they cannot fix it until Monday. This is unacceptable, please sort it out today.");

        // UI
        HomePage home = new HomePage(page).open().sendMessage(sent);
        com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat(home.confirmation()).isVisible();

        // API: the message was stored unchanged
        PlatformMessageClient messages = new PlatformMessageClient().login();
        int id = waitForMessage(messages, subject);
        JsonPath stored = messages.get(id);
        assertThat(stored.getString("name")).isEqualTo(sent.name());
        assertThat(stored.getString("description")).isEqualTo(sent.description());

        // LLM: triage the stored message
        TriageResult triage = LlmFixtures.triage().triage(stored.getString("description"));
        assertThat(OutputContract.violations(triage)).isEmpty();
        assertThat(triage.category()).isEqualTo("COMPLAINT");
        assertThat(triage.priority()).isIn("HIGH", "URGENT");
    }

    /** The message shows up in the list asynchronously; poll with a timeout instead of a fixed sleep. */
    private static int waitForMessage(PlatformMessageClient messages, String subject) {
        Instant deadline = Instant.now().plus(Duration.ofSeconds(15));
        while (Instant.now().isBefore(deadline)) {
            Optional<Integer> id = messages.findIdBySubject(subject);
            if (id.isPresent()) {
                return id.get();
            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        throw new AssertionError("Message with subject '" + subject + "' not found via API");
    }
}
