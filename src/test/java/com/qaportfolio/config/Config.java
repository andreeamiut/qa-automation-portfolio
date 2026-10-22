package com.qaportfolio.config;

/**
 * Central test configuration. Resolution order: -Dkey=... -> environment variable KEY -> default value.
 * E.g.: mvn test -DUI_BASE_URL=http://localhost:8080 or export API_BASE_URL=...
 */
public final class Config {

    private Config() {
    }

    public static String env() {
        return get("ENV", "public-demo");
    }

    // --- API (restful-booker) ---
    public static String apiBaseUrl() {
        return get("API_BASE_URL", "https://restful-booker.herokuapp.com");
    }

    public static String apiUser() {
        return get("API_USER", "admin");
    }

    public static String apiPassword() {
        return get("API_PASSWORD", "password123");
    }

    // --- Platform UI + API (restful-booker-platform) ---
    public static String uiBaseUrl() {
        return get("UI_BASE_URL", "https://automationintesting.online");
    }

    public static String platformApiUrl() {
        return get("PLATFORM_API_URL", uiBaseUrl() + "/api");
    }

    public static String adminUser() {
        return get("ADMIN_USER", "admin");
    }

    public static String adminPassword() {
        return get("ADMIN_PASSWORD", "password");
    }

    public static boolean headless() {
        return Boolean.parseBoolean(get("HEADLESS", "true"));
    }

    // --- LLM ---
    public static String triageModel() {
        return get("TRIAGE_MODEL", "claude-opus-5-5");
    }

    public static String judgeModel() {
        return get("JUDGE_MODEL", "claude-opus-5-5");
    }

    public static String promptVersion() {
        return get("PROMPT_VERSION", "triage_v1");
    }

    public static double minCategoryAccuracy() {
        return Double.parseDouble(get("MIN_CATEGORY_ACCURACY", "0.85"));
    }

    public static double minPriorityAccuracy() {
        return Double.parseDouble(get("MIN_PRIORITY_ACCURACY", "0.70"));
    }

    /** How far a metric may drop below the baseline before LLM-07 fails. */
    public static double regressionTolerance() {
        return Double.parseDouble(get("REGRESSION_TOLERANCE", "0.05"));
    }

    public static int consistencyRuns() {
        return Integer.parseInt(get("CONSISTENCY_RUNS", "3"));
    }

    // --- Performance ---
    /** Small by default: the target is a public demo, not a dedicated load environment. */
    public static int perfUsers() {
        return Integer.parseInt(get("PERF_USERS", "5"));
    }

    public static int perfRampSeconds() {
        return Integer.parseInt(get("PERF_RAMP_SECONDS", "30"));
    }

    private static String get(String key, String defaultValue) {
        String value = System.getProperty(key);
        if (value == null || value.isBlank()) {
            value = System.getenv(key);
        }
        return value == null || value.isBlank() ? defaultValue : value;
    }
}
