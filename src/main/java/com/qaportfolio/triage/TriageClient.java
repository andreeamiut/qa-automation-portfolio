package com.qaportfolio.triage;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qaportfolio.llm.AnthropicClient;
import com.qaportfolio.llm.LlmResponse;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;

/** Triage service: customer message -> {category, priority, summary}. */
public class TriageClient {

    public static final String DEFAULT_PROMPT_VERSION = "triage_v1";
    public static final Set<String> CATEGORIES =
            Set.of("BOOKING", "CANCELLATION", "COMPLAINT", "BILLING", "INQUIRY", "OTHER");
    /** Ordered ascending, so tests can measure the distance between levels. */
    public static final List<String> PRIORITIES = List.of("LOW", "MEDIUM", "HIGH", "URGENT");

    private static final long MAX_TOKENS = 4096;
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final AnthropicClient llm;
    private final String model;
    private final String promptVersion;
    private final String systemPrompt;

    public TriageClient(AnthropicClient llm, String model) {
        this(llm, model, DEFAULT_PROMPT_VERSION);
    }

    public TriageClient(AnthropicClient llm, String model, String promptVersion) {
        this.llm = llm;
        this.model = model;
        this.promptVersion = promptVersion;
        this.systemPrompt = loadPrompt(promptVersion);
    }

    public TriageResult triage(String customerMessage) {
        // the customer message is data, not instructions: delimit it explicitly
        String user = "<customer_message>\n" + customerMessage + "\n</customer_message>";
        LlmResponse response = llm.complete(model, systemPrompt, user, MAX_TOKENS);
        if (response.isRefusal()) {
            throw new TriageException("Model refused to triage the message");
        }
        return parse(response.text());
    }

    public String model() {
        return model;
    }

    public String promptVersion() {
        return promptVersion;
    }

    public String systemPrompt() {
        return systemPrompt;
    }

    /** Tolerates text around the JSON object (e.g. ```json fences). */
    public static TriageResult parse(String raw) {
        int start = raw.indexOf('{');
        int end = raw.lastIndexOf('}');
        if (start < 0 || end <= start) {
            throw new TriageException("No JSON object in model output: " + raw);
        }
        try {
            JsonNode node = MAPPER.readTree(raw.substring(start, end + 1));
            return new TriageResult(text(node, "category"), text(node, "priority"), text(node, "summary"), raw);
        } catch (IOException e) {
            throw new TriageException("Invalid JSON in model output: " + raw, e);
        }
    }

    private static String text(JsonNode node, String field) {
        JsonNode value = node.get(field);
        return value == null || value.isNull() ? null : value.asText();
    }

    public static String loadPrompt(String version) {
        String path = "/prompts/" + version + ".txt";
        try (InputStream in = TriageClient.class.getResourceAsStream(path)) {
            if (in == null) {
                throw new IllegalArgumentException("Prompt not found: " + path);
            }
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
