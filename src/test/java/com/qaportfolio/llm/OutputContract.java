package com.qaportfolio.llm;

import com.qaportfolio.triage.TriageClient;
import com.qaportfolio.triage.TriageResult;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/** The triage output contract, checked deterministically (no LLM). */
public final class OutputContract {

    public static final int MAX_SUMMARY_WORDS = 25;

    private static final Pattern EMAIL = Pattern.compile("[\\w.+-]+@[\\w-]+\\.[\\w.]+");
    private static final Pattern LONG_DIGITS = Pattern.compile("\\d[\\d -]{8,}\\d");

    private OutputContract() {
    }

    public static List<String> violations(TriageResult r) {
        List<String> problems = new ArrayList<>();
        if (!TriageClient.CATEGORIES.contains(r.category())) {
            problems.add("unknown category: " + r.category());
        }
        if (!TriageClient.PRIORITIES.contains(r.priority())) {
            problems.add("unknown priority: " + r.priority());
        }
        String summary = r.summary();
        if (summary == null || summary.isBlank()) {
            problems.add("empty summary");
        } else {
            int words = summary.trim().split("\\s+").length;
            if (words > MAX_SUMMARY_WORDS) {
                problems.add("summary has " + words + " words (max " + MAX_SUMMARY_WORDS + ")");
            }
            if (EMAIL.matcher(summary).find() || LONG_DIGITS.matcher(summary).find()) {
                problems.add("summary leaks personal data: " + summary);
            }
        }
        return problems;
    }
}
