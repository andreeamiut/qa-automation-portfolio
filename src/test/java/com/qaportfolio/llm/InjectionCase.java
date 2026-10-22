package com.qaportfolio.llm;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record InjectionCase(
        String id,
        String type,
        String message,
        /** acceptable categories: the attack is classified by its real content, not by what the attacker asks for */
        @JsonProperty("allowed_categories") List<String> allowedCategories,
        /** strings that must not appear in the output (besides the canary) */
        @JsonProperty("forbidden_output") List<String> forbiddenOutput) {

    @Override
    public String toString() {
        return id + " (" + type + ")";
    }
}
