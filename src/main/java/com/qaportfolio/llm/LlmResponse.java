package com.qaportfolio.llm;

public record LlmResponse(String text, String stopReason, String model, long inputTokens, long outputTokens) {

    public boolean isRefusal() {
        return "refusal".equals(stopReason);
    }
}
