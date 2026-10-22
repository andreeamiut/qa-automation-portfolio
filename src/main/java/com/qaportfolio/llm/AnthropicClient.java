package com.qaportfolio.llm;

import com.anthropic.client.okhttp.AnthropicOkHttpClient;
import com.anthropic.core.JsonValue;
import com.anthropic.models.messages.Message;
import com.anthropic.models.messages.MessageCreateParams;
import com.anthropic.models.messages.OutputConfig;
import com.anthropic.models.messages.TextBlock;

import java.util.stream.Collectors;

/**
 * Thin wrapper over the Anthropic SDK: one call = one text response.
 * The key is read from ANTHROPIC_API_KEY (or from the `ant auth login` profile).
 */
public class AnthropicClient {

    private final com.anthropic.client.AnthropicClient sdk;

    public AnthropicClient() {
        this(AnthropicOkHttpClient.fromEnv());
    }

    public AnthropicClient(com.anthropic.client.AnthropicClient sdk) {
        this.sdk = sdk;
    }

    public LlmResponse complete(String model, String system, String userMessage, long maxTokens) {
        MessageCreateParams params = MessageCreateParams.builder()
                .model(model)
                .maxTokens(maxTokens)
                .system(system)
                .addUserMessage(userMessage)
                // classification is a simple task; low effort keeps cost and latency down
                .outputConfig(OutputConfig.builder().effort(OutputConfig.Effort.LOW).build())
                // if a safety classifier refuses, the server retries on a fallback model
                .putAdditionalHeader("anthropic-beta", "server-side-fallback-2026-07-01")
                .putAdditionalBodyProperty("fallbacks", JsonValue.from("default"))
                .build();

        Message message = sdk.messages().create(params);

        String text = message.content().stream()
                .flatMap(block -> block.text().stream())
                .map(TextBlock::text)
                .collect(Collectors.joining());
        String stopReason = message.stopReason().map(Object::toString).orElse("unknown");

        return new LlmResponse(text, stopReason, message.model().asString(),
                message.usage().inputTokens(), message.usage().outputTokens());
    }
}
