package com.qaportfolio.llm;

import com.fasterxml.jackson.databind.JsonNode;
import com.qaportfolio.config.Config;

/** LLM-as-judge: scores how faithful the summary is to the original message (1-5). */
public class JudgeClient {

    private static final String SYSTEM = """
            You grade summaries written by a hotel help-desk triage service.
            You get the original customer message and the summary. Score the summary from 1 to 5:
            5 = accurate, complete on what the customer wants, no invented facts
            4 = accurate, minor omission
            3 = noticeable omission or vague
            2 = contains a claim not supported by the message
            1 = wrong or unrelated
            Invented facts cap the score at 2, however fluent the summary is.
            Return only JSON: {"score": <1-5>, "reason": "<one sentence>"}
            """;

    public record Verdict(int score, String reason) {
    }

    private final AnthropicClient llm;

    public JudgeClient(AnthropicClient llm) {
        this.llm = llm;
    }

    public Verdict grade(String message, String summary) {
        String user = "<customer_message>\n" + message + "\n</customer_message>\n<summary>\n" + summary + "\n</summary>";
        String raw = llm.complete(Config.judgeModel(), SYSTEM, user, 4096).text();
        try {
            JsonNode node = LlmFixtures.MAPPER.readTree(raw.substring(raw.indexOf('{'), raw.lastIndexOf('}') + 1));
            return new Verdict(node.get("score").asInt(), node.path("reason").asText());
        } catch (Exception e) {
            throw new IllegalStateException("Judge returned unparsable output: " + raw, e);
        }
    }
}
