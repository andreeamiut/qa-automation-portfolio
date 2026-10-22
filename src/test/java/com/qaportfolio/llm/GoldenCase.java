package com.qaportfolio.llm;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GoldenCase(
        String id,
        String message,
        @JsonProperty("expected_category") String expectedCategory,
        @JsonProperty("expected_priority") String expectedPriority,
        String notes) {

    @Override
    public String toString() {
        return id;
    }
}
