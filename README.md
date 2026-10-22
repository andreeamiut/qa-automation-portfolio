# QA Automation Portfolio

[![Tests](https://github.com/andreeamiut/qa-automation-portfolio/actions/workflows/ci.yml/badge.svg)](https://github.com/andreeamiut/qa-automation-portfolio/actions/workflows/ci.yml)

Java 25 (LTS) + Maven. Tests three systems:

| System | What it is | Tests |
|---|---|---|
| [restful-booker](https://restful-booker.herokuapp.com) | Public REST booking API | API-01..09, PERF-01 |
| [restful-booker-platform](https://automationintesting.online) | UI + API of a demo B&B | UI-01..05, INT-03 |
| Triage service (`src/main`) | LLM: customer message -> `{category, priority, summary}` | LLM-01..07, INT-03 |

## What this demonstrates

- **API testing** – contract, CRUD and negative tests against a public booking API
- **UI testing** – Playwright & Selenium with traces for every run
- **Performance** – Gatling load test with configurable users/ramp-up
- **LLM evaluation** – golden-set accuracy, consistency, prompt-injection (canary leak) checks, LLM-as-judge and baseline regression
- **CI** – smoke suite on every PR, Allure reporting

## Running

```bash
mvn test-compile exec:java -Dexec.args="install chromium"   # once: Playwright browsers

mvn test            # regression: API + UI + deterministic tests (no LLM)
mvn test -Psmoke    # only @Tag("smoke"), runs on every PR
mvn test -Pllm      # LLM evaluation; requires ANTHROPIC_API_KEY
mvn gatling:test    # PERF-01; -DPERF_USERS=20 -DPERF_RAMP_SECONDS=60

mvn allure:serve    # Allure report from the last run, opened in the browser
mvn allure:report   # static report in target/site/allure-maven-plugin
```

Any value in `Config` can be overridden with `-DKEY=...` or the `KEY` environment variable
(e.g. `UI_BASE_URL`, `HEADLESS=false`, `TRIAGE_MODEL`, `MIN_CATEGORY_ACCURACY`).

## LLM evaluation

- **LLM-01** contract: category/priority from the enum, summary ≤ 25 words, no personal data
- **LLM-02 / 03** category / priority accuracy against `golden_set.json`
- **LLM-04** consistency: ambiguous cases run N times get the same category
- **LLM-05** prompt injection: 8 attack types; the prompt's canary must never leak
- **LLM-06** LLM-as-judge: summary faithfulness
- **LLM-07** regression against `baseline.json`

The golden set runs once per JVM and its results are shared between tests.
Every run writes `target/llm-eval/current.json`; after a manual review, copy its metrics into
`src/test/resources/llm/baseline.json` to enable LLM-07.

To change the prompt: create `triage_v2.txt`, run with `-DPROMPT_VERSION=triage_v2`
and compare against the baseline before switching the default version.

## UI traces

Every Playwright test saves a trace in `target/traces/`:
`npx playwright show-trace target/traces/<Class>-<method>.zip`
