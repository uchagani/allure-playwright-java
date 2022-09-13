package io.github.uchagani.allure.playwright;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import io.qameta.allure.model.Status;
import io.qameta.allure.model.StepResult;
import io.qameta.allure.model.TestResult;
import io.qameta.allure.test.AllureResults;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static io.qameta.allure.test.RunUtils.runWithinTestContext;
import static org.assertj.core.api.Assertions.assertThat;

public class TestBase {
    final static String html;
    final static double timeout = 1;
    final static String checkboxSelector = "#checkbox";
    final static String buttonSelector = "#button";
    final static String dragSourceSelector = "#source";
    final static String dragTargetSelector = "#target";
    final static String textBoxSelector = "#textbox";
    final static String selectOptionSelector = "#select";
    final static String inputFileSelector = "#inputFile";
    final static String divSelector = "#tap";
    final static String checkedCheckboxSelector = "#checkboxChecked";

    static {
        try {
            html = IOUtils.resourceToString("/content.html", StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    Playwright playwright;
    Page page;

    @BeforeEach
    void getPage() {
        playwright = Playwright.create();
        BrowserContext context = playwright.chromium().launch().newContext(new Browser.NewContextOptions().setHasTouch(true));
        page = context.newPage();
    }

    @AfterEach
    void cleanup() {
        playwright.close();
    }

    AllureResults runTest(Runnable test) {
        return runWithinTestContext(test, ChannelOwnerAspect::setLifecycle);
    }

    void assertStepsWhenPassed(AllureResults results, String stepName) {
        TestResult testResult = results.getTestResults().get(0);
        assertStepStatusPass(testResult);
        assertStepName(testResult, stepName);
    }

    void assertStepsWhenBroken(AllureResults results, String stepName) {
        TestResult testResult = results.getTestResults().get(0);
        assertStepStatusBroken(testResult);
        assertStepName(testResult, stepName);
        assertThat(testResult.getSteps()).extracting("statusDetails.trace").anyMatch(msg -> ((String) msg).contains("TimeoutError"));
    }

    void assertStepsWhenFailed(AllureResults results, String stepName) {
        TestResult testResult = results.getTestResults().get(0);
        assertThat(testResult.getStatus()).isEqualTo(Status.FAILED);
        assertStepName(testResult, stepName);
        assertThat(testResult.getStatusDetails().getTrace()).contains("AssertionFailedError");
    }

    void assertStepStatusPass(TestResult testResult) {
        assertThat(testResult.getSteps()).flatExtracting(StepResult::getStatus).containsExactly(Status.PASSED);
    }

    void assertStepStatusBroken(TestResult testResult) {
        assertThat(testResult.getSteps()).flatExtracting(StepResult::getStatus).containsExactly(Status.BROKEN);
    }

    void assertStepName(TestResult testResult, String stepName) {
        assertThat(testResult.getSteps()).flatExtracting(StepResult::getName).containsExactly(stepName);
    }
}
