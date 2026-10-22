package com.qaportfolio.llm;

import com.qaportfolio.llm.GoldenSetRun.CaseResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("llm")
class SummaryJudgeTest {

    private static final double MIN_AVERAGE = 4.0;
    private static final int MIN_SINGLE = 3;

    @Test
    @DisplayName("LLM-06 LLM-as-judge: summaries are faithful to the message")
    void summariesAreFaithful() {
        JudgeClient judge = new JudgeClient(LlmFixtures.llm());
        Map<String, JudgeClient.Verdict> verdicts = new LinkedHashMap<>();

        for (CaseResult r : GoldenSetRun.results()) {
            if (r.ok()) {
                verdicts.put(r.golden().id(), judge.grade(r.golden().message(), r.result().summary()));
            }
        }

        double average = verdicts.values().stream().mapToInt(JudgeClient.Verdict::score).average().orElse(0);
        Map<String, JudgeClient.Verdict> poor = new LinkedHashMap<>();
        verdicts.forEach((id, v) -> {
            if (v.score() < MIN_SINGLE) {
                poor.put(id, v);
            }
        });

        assertThat(verdicts).as("judged cases").isNotEmpty();
        assertThat(poor).as("summaries scored below %d (likely invented facts)", MIN_SINGLE).isEmpty();
        assertThat(average).as("average judge score").isGreaterThanOrEqualTo(MIN_AVERAGE);
    }
}
