package com.qaportfolio.llm;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qaportfolio.config.Config;
import com.qaportfolio.triage.TriageClient;
import org.junit.jupiter.api.Assumptions;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Clients and data shared by the LLM tests. Clients are created once per JVM. */
public final class LlmFixtures {

    public static final ObjectMapper MAPPER = new ObjectMapper();

    private static AnthropicClient llm;
    private static TriageClient triage;

    private LlmFixtures() {
    }

    public static synchronized TriageClient triage() {
        requireCredentials();
        if (triage == null) {
            triage = new TriageClient(llm(), Config.triageModel(), Config.promptVersion());
        }
        return triage;
    }

    public static synchronized AnthropicClient llm() {
        requireCredentials();
        if (llm == null) {
            llm = new AnthropicClient();
        }
        return llm;
    }

    /** Without credentials the LLM tests are reported as skipped, not failed. */
    public static void requireCredentials() {
        boolean hasKey = notBlank(System.getenv("ANTHROPIC_API_KEY")) || notBlank(System.getenv("ANTHROPIC_AUTH_TOKEN"));
        Assumptions.assumeTrue(hasKey, "ANTHROPIC_API_KEY not set: skipping LLM tests");
    }

    /** The canary is read from the prompt, so the test stays correct when the prompt is versioned. */
    public static String canary() {
        Matcher m = Pattern.compile("CANARY-[A-Za-z0-9]+").matcher(TriageClient.loadPrompt(Config.promptVersion()));
        if (!m.find()) {
            throw new IllegalStateException("Prompt " + Config.promptVersion() + " has no canary");
        }
        return m.group();
    }

    public static List<GoldenCase> goldenSet() {
        return read("/llm/golden_set.json", new TypeReference<>() {
        });
    }

    public static List<InjectionCase> injectionCases() {
        return read("/llm/injection_cases.json", new TypeReference<>() {
        });
    }

    public static <T> T read(String resource, TypeReference<T> type) {
        try (InputStream in = LlmFixtures.class.getResourceAsStream(resource)) {
            if (in == null) {
                throw new IllegalArgumentException("Missing test resource " + resource);
            }
            return MAPPER.readValue(in, type);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static boolean notBlank(String s) {
        return s != null && !s.isBlank();
    }
}
