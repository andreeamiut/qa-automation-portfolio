package com.qaportfolio.triage;

/** Triage service output. `raw` keeps the model's raw text for debugging. */
public record TriageResult(String category, String priority, String summary, String raw) {
}
