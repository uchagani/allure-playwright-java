package io.github.uchagani.allure.playwright;

import io.qameta.allure.model.TestResult;
import io.qameta.allure.test.AllureResults;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class APIRequestContextTests extends TestBase {

    @Test
    void apiRequestContextTest_Pass() {
        String url = "https://playwright.dev";
        AllureResults results = runTest(() -> page.request().get(url));
        assertStepsWhenPassed(results, "GET " + url);
    }

    @Test
    void apiRequestContextTest_Broken() {
        String url = "https://" + UUID.randomUUID() + ".com";
        AllureResults results = runTest(() -> page.request().get(url));
        TestResult testResult = results.getTestResults().get(0);

        assertStepStatusBroken(testResult);
        assertStepName(testResult, "GET " + url);
        assertThat(testResult.getSteps()).extracting("statusDetails.trace").anyMatch(msg -> ((String) msg).contains("PlaywrightException"));
    }
}
