package com.qaportfolio.llm;

import com.qaportfolio.triage.TriageClient;
import com.qaportfolio.triage.TriageException;
import com.qaportfolio.triage.TriageResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Deterministic tests, no API calls: they run in the default regression. */
class TriageParserTest {

    @Test
    @DisplayName("parser accepts JSON wrapped in markdown fences")
    void parsesFencedJson() {
        TriageResult r = TriageClient.parse("""
                ```json
                {"category": "BILLING", "priority": "HIGH", "summary": "Guest was charged twice."}
                ```""");

        assertThat(r.category()).isEqualTo("BILLING");
        assertThat(r.priority()).isEqualTo("HIGH");
        assertThat(OutputContract.violations(r)).isEmpty();
    }

    @Test
    @DisplayName("output without JSON throws TriageException")
    void rejectsNonJson() {
        assertThatThrownBy(() -> TriageClient.parse("I cannot help with that."))
                .isInstanceOf(TriageException.class);
    }

    @Test
    @DisplayName("contract catches invented categories and personal data in the summary")
    void contractCatchesViolations() {
        TriageResult r = new TriageResult("REFUND", "CRITICAL", "Email john@example.com about it", "");

        assertThat(OutputContract.violations(r)).hasSize(3);
    }

    @Test
    @DisplayName("versioned prompt contains the canary for injection tests")
    void promptHasCanary() {
        assertThat(LlmFixtures.canary()).startsWith("CANARY-");
    }
}
