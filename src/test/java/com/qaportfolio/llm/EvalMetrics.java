package com.qaportfolio.llm;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.qaportfolio.llm.GoldenSetRun.CaseResult;
import com.qaportfolio.triage.TriageClient;

import java.util.List;
import java.util.function.Predicate;

/** Aggregated metrics over the golden set. Parse/call errors count as wrong answers. */
public record EvalMetrics(
        int cases,
        @JsonProperty("valid_output_rate") double validOutputRate,
        @JsonProperty("category_accuracy") double categoryAccuracy,
        @JsonProperty("priority_accuracy") double priorityAccuracy,
        @JsonProperty("priority_within_one") double priorityWithinOne) {

    public static EvalMetrics of(List<CaseResult> results) {
        return new EvalMetrics(
                results.size(),
                rate(results, r -> r.ok() && OutputContract.violations(r.result()).isEmpty()),
                rate(results, r -> r.ok() && r.golden().expectedCategory().equals(r.result().category())),
                rate(results, r -> r.ok() && r.golden().expectedPriority().equals(r.result().priority())),
                rate(results, r -> r.ok() && priorityDistance(r) <= 1));
    }

    static int priorityDistance(CaseResult r) {
        int expected = TriageClient.PRIORITIES.indexOf(r.golden().expectedPriority());
        int actual = TriageClient.PRIORITIES.indexOf(r.result().priority());
        return actual < 0 ? Integer.MAX_VALUE : Math.abs(expected - actual);
    }

    private static double rate(List<CaseResult> results, Predicate<CaseResult> hit) {
        if (results.isEmpty()) {
            return 0;
        }
        return (double) results.stream().filter(hit).count() / results.size();
    }
}
