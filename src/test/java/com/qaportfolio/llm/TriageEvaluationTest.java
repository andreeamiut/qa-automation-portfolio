package com.qaportfolio.llm;

import com.fasterxml.jackson.databind.JsonNode;
import com.qaportfolio.config.Config;
import com.qaportfolio.llm.GoldenSetRun.CaseResult;
import com.qaportfolio.triage.TriageResult;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("llm")
class TriageEvaluationTest {

    private static final Path REPORT_DIR = Path.of("target", "llm-eval");

    @Test
    @DisplayName("LLM-01 every response follows the output contract")
    void everyOutputMatchesContract() {
        Map<String, List<String>> failures = new LinkedHashMap<>();
        for (CaseResult r : GoldenSetRun.results()) {
            List<String> problems = r.ok() ? OutputContract.violations(r.result()) : List.of("error: " + r.error());
            if (!problems.isEmpty()) {
                failures.put(r.golden().id(), problems);
            }
        }
        assertThat(failures).as("contract violations per case").isEmpty();
    }

    @Test
    @DisplayName("LLM-02 category accuracy meets the threshold")
    void categoryAccuracyAboveThreshold() {
        List<CaseResult> results = GoldenSetRun.results();
        EvalMetrics metrics = EvalMetrics.of(results);

        assertThat(metrics.categoryAccuracy())
                .as("category accuracy; misses:%n%s", misses(results, true))
                .isGreaterThanOrEqualTo(Config.minCategoryAccuracy());
    }

    @Test
    @DisplayName("LLM-03 priority: exact above threshold, never more than one level off")
    void priorityAccuracyAboveThreshold() {
        List<CaseResult> results = GoldenSetRun.results();
        EvalMetrics metrics = EvalMetrics.of(results);

        assertThat(metrics.priorityAccuracy())
                .as("priority accuracy; misses:%n%s", misses(results, false))
                .isGreaterThanOrEqualTo(Config.minPriorityAccuracy());
        // URGENT classified as LOW is worse than URGENT classified as HIGH
        assertThat(metrics.priorityWithinOne()).as("priority within one level").isGreaterThanOrEqualTo(0.95);
    }

    @Test
    @DisplayName("LLM-04 the same message gets the same category across repeated runs")
    void sameMessageSameCategory() {
        // cases marked "ambiguous" are where non-determinism shows up first
        List<GoldenCase> probes = LlmFixtures.goldenSet().stream()
                .filter(c -> c.notes() != null && c.notes().contains("ambiguous"))
                .limit(3)
                .toList();
        Assumptions.assumeFalse(probes.isEmpty(), "no golden cases marked ambiguous");

        for (GoldenCase probe : probes) {
            List<String> categories = new ArrayList<>();
            for (int i = 0; i < Config.consistencyRuns(); i++) {
                categories.add(LlmFixtures.triage().triage(probe.message()).category());
            }
            assertThat(categories).as("categories for %s over %d runs", probe.id(), Config.consistencyRuns())
                    .containsOnly(categories.get(0));
        }
    }

    @Test
    @DisplayName("LLM-07 metrics do not regress against the reviewed baseline")
    void noRegressionAgainstBaseline() throws IOException {
        EvalMetrics current = EvalMetrics.of(GoldenSetRun.results());
        writeCurrentReport(current);

        JsonNode baseline = LlmFixtures.MAPPER.readTree(getClass().getResourceAsStream("/llm/baseline.json"));
        JsonNode baseMetrics = baseline.path("metrics");
        Assumptions.assumeFalse(baseMetrics.isMissingNode() || baseMetrics.isNull(),
                "baseline.json is empty: review target/llm-eval/current.json and copy it into src/test/resources/llm/baseline.json");

        double tolerance = Config.regressionTolerance();
        assertThat(current.categoryAccuracy()).as("category accuracy vs baseline")
                .isGreaterThanOrEqualTo(baseMetrics.get("category_accuracy").asDouble() - tolerance);
        assertThat(current.priorityAccuracy()).as("priority accuracy vs baseline")
                .isGreaterThanOrEqualTo(baseMetrics.get("priority_accuracy").asDouble() - tolerance);
        assertThat(current.validOutputRate()).as("valid output rate vs baseline")
                .isGreaterThanOrEqualTo(baseMetrics.get("valid_output_rate").asDouble());
    }

    /** The report has exactly the shape of baseline.json, so it can be copied after review. */
    private static void writeCurrentReport(EvalMetrics metrics) throws IOException {
        Map<String, Object> report = new LinkedHashMap<>();
        report.put("prompt_version", LlmFixtures.triage().promptVersion());
        report.put("model", LlmFixtures.triage().model());
        report.put("recorded_at", Instant.now().toString());
        report.put("metrics", metrics);
        report.put("cases", GoldenSetRun.results().stream().map(TriageEvaluationTest::caseRow).toList());
        Files.createDirectories(REPORT_DIR);
        LlmFixtures.MAPPER.writerWithDefaultPrettyPrinter().writeValue(REPORT_DIR.resolve("current.json").toFile(), report);
    }

    private static Map<String, Object> caseRow(CaseResult r) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("id", r.golden().id());
        row.put("expected", r.golden().expectedCategory() + "/" + r.golden().expectedPriority());
        TriageResult t = r.result();
        row.put("actual", r.ok() ? t.category() + "/" + t.priority() : "ERROR");
        row.put("summary", r.ok() ? t.summary() : r.error());
        return row;
    }

    private static String misses(List<CaseResult> results, boolean category) {
        return results.stream()
                .filter(r -> !r.ok() || !(category
                        ? r.golden().expectedCategory().equals(r.result().category())
                        : r.golden().expectedPriority().equals(r.result().priority())))
                .map(r -> "  " + r.golden().id() + ": expected "
                        + (category ? r.golden().expectedCategory() : r.golden().expectedPriority())
                        + ", got " + (r.ok() ? (category ? r.result().category() : r.result().priority()) : r.error()))
                .collect(Collectors.joining("\n"));
    }
}
