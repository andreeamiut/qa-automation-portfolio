package com.qaportfolio.llm;

import com.qaportfolio.triage.TriageResult;

import java.util.ArrayList;
import java.util.List;

/**
 * Runs the golden set once per JVM and shares the results between LLM-01/02/03/06/07,
 * so the same calls are not paid for five times.
 */
public final class GoldenSetRun {

    public record CaseResult(GoldenCase golden, TriageResult result, String error) {
        public boolean ok() {
            return error == null;
        }
    }

    private static List<CaseResult> cached;

    private GoldenSetRun() {
    }

    public static synchronized List<CaseResult> results() {
        if (cached == null) {
            List<CaseResult> results = new ArrayList<>();
            for (GoldenCase golden : LlmFixtures.goldenSet()) {
                try {
                    results.add(new CaseResult(golden, LlmFixtures.triage().triage(golden.message()), null));
                } catch (RuntimeException e) {
                    // an error on one case is an evaluation result, not a reason to stop the whole set
                    results.add(new CaseResult(golden, null, e.getMessage()));
                }
            }
            cached = List.copyOf(results);
        }
        return cached;
    }
}
