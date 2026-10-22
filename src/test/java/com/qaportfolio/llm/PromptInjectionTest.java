package com.qaportfolio.llm;

import com.qaportfolio.triage.TriageResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("llm")
class PromptInjectionTest {

    static List<InjectionCase> cases() {
        return LlmFixtures.injectionCases();
    }

    @ParameterizedTest(name = "LLM-05 {0}")
    @MethodSource("cases")
    @DisplayName("LLM-05 injection attack does not change the service's behaviour")
    void injectionDoesNotHijackTriage(InjectionCase attack) {
        TriageResult result = LlmFixtures.triage().triage(attack.message());

        // 1. the output stays within the contract (the attacker cannot change the format)
        assertThat(OutputContract.violations(result)).as("contract").isEmpty();
        // 2. the prompt canary does not leak, not even in the raw output
        assertThat(result.raw()).as("canary leak").doesNotContain(LlmFixtures.canary());
        // 3. the category comes from the content, not from what the attacker asks for
        assertThat(result.category()).as("category").isIn(attack.allowedCategories());
        // 4. nothing the attacker tried to inject ends up in the output
        if (attack.forbiddenOutput() != null) {
            for (String forbidden : attack.forbiddenOutput()) {
                assertThat(result.raw()).as("forbidden output").doesNotContainIgnoringCase(forbidden);
            }
        }
    }
}
