package io.github.uchagani.allure.playwright;

import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.assertions.LocatorAssertions;
import com.microsoft.playwright.assertions.PageAssertions;
import io.qameta.allure.model.Status;
import io.qameta.allure.model.StepResult;
import io.qameta.allure.model.TestResult;
import io.qameta.allure.test.AllureResults;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static io.qameta.allure.test.RunUtils.runWithinTestContext;
import static org.assertj.core.api.Assertions.assertThat;

public class AspectTest {
    private Page page;
    private Playwright playwright;
    private final static String passSelector = ".foo";
    private final static String failSelector = ".blah";

    @BeforeEach
    void getPage() {
        playwright = Playwright.create();
        page = playwright.chromium().launch().newPage();
        page.setContent(" <button type='button' class='foo'>Click Me!</button> ");
    }

    @AfterEach
    void cleanup() {
        playwright.close();
    }

    @Test
    void locatorTest_pass() {
        AllureResults results = runWithinTestContext(() -> {
            Locator locator = page.locator(passSelector);
            locator.click();
        }, LocatorAspect::setLifecycle);

        TestResult testResult = results.getTestResults().get(0);
        assertThat(testResult.getSteps()).flatExtracting(StepResult::getName).containsExactly("click " + passSelector);
        assertThat(testResult.getSteps()).flatExtracting(StepResult::getStatus).containsExactly(Status.PASSED);
    }

    @Test
    void locatorTest_fail() {
        AllureResults results = runWithinTestContext(() -> {
            Locator locator = page.locator(failSelector);
            locator.click(new Locator.ClickOptions().setTimeout(50));
        }, LocatorAspect::setLifecycle);

        TestResult testResult = results.getTestResults().get(0);
        assertThat(testResult.getStatus()).isEqualTo(Status.BROKEN);
        assertThat(testResult.getStatusDetails().getMessage()).contains("Timeout");
        assertThat(testResult.getSteps()).flatExtracting(StepResult::getName).containsExactly("click " + failSelector);
        assertThat(testResult.getSteps()).flatExtracting(StepResult::getStatus).containsExactly(Status.BROKEN);
    }

    @Test
    void apiRequestContextTest_pass() {
        String url = "https://playwright.dev";
        AllureResults results = runWithinTestContext(() -> {
            APIResponse response = page.request().get(url);
            assertThat(response).isOK();
        }, APIRequestContextAspect::setLifecycle);

        TestResult testResult = results.getTestResults().get(0);
        assertThat(testResult.getSteps()).flatExtracting(StepResult::getName).containsExactly("GET " + url);
        assertThat(testResult.getSteps()).flatExtracting(StepResult::getStatus).containsExactly(Status.PASSED);
    }

    @Test
    void pageTest_pass() {
        String url = "https://playwright.dev";
        AllureResults results = runWithinTestContext(() -> page.navigate(url), PageAspect::setLifecycle);

        TestResult testResult = results.getTestResults().get(0);
        assertThat(testResult.getSteps()).flatExtracting(StepResult::getName).containsExactly("navigate");
        assertThat(testResult.getSteps()).flatExtracting(StepResult::getStatus).containsExactly(Status.PASSED);
    }

    @Test
    void locatorAssertTest_pass() {
        String expectedText = "Click";
        AllureResults results = runWithinTestContext(() -> {
            Locator locator = page.locator(passSelector);
            assertThat(locator).containsText(expectedText);
        }, AssertionsAspect::setLifecycle);

        TestResult testResult = results.getTestResults().get(0);
        assertThat(testResult.getSteps()).flatExtracting(StepResult::getName).contains("Locator " + passSelector + " expected to contain text " + expectedText);
    }

    @Test
    void locatorAssertTest_fail() {
        String expectedText = "abc";
        AllureResults results = runWithinTestContext(() -> {
            Locator locator = page.locator(passSelector);
            assertThat(locator).containsText(expectedText, new LocatorAssertions.ContainsTextOptions().setTimeout(50));
        }, AssertionsAspect::setLifecycle);

        TestResult testResult = results.getTestResults().get(0);
        assertThat(testResult.getStatus()).isEqualTo(Status.FAILED);
        assertThat(testResult.getSteps()).flatExtracting(StepResult::getName).contains("Locator " + passSelector + " expected to contain text " + expectedText);
    }

    @Test
    void pageAssertTest_pass() {
        String expectedText = "Page Assert Test";
        AllureResults results = runWithinTestContext(() -> {
            page.evaluate("document.title = 'Page Assert Test'");
            assertThat(page).hasTitle(expectedText);
        }, AssertionsAspect::setLifecycle);

        TestResult testResult = results.getTestResults().get(0);
        assertThat(testResult.getSteps()).flatExtracting(StepResult::getName).contains("Page title expected to be " + expectedText);
    }

    @Test
    void pageAssertTest_fail() {
        String expectedText = "abc";
        AllureResults results = runWithinTestContext(() -> {
            assertThat(page).hasTitle(expectedText, new PageAssertions.HasTitleOptions().setTimeout(50));
        }, AssertionsAspect::setLifecycle);

        TestResult testResult = results.getTestResults().get(0);
        assertThat(testResult.getStatus()).isEqualTo(Status.FAILED);
        assertThat(testResult.getStatusDetails().getMessage()).startsWith("Page title expected to be: " + expectedText);
    }

    @Test
    void apiResponseAssertTest_pass() {
        AllureResults results = runWithinTestContext(() -> {
            APIResponse response = page.request().get("https://playwright.dev");
            assertThat(response).isOK();
        }, AssertionsAspect::setLifecycle);

        TestResult testResult = results.getTestResults().get(0);
        assertThat(testResult.getSteps()).flatExtracting(StepResult::getName).containsExactly("APIResponse expected to be OK");
        assertThat(testResult.getSteps()).flatExtracting(StepResult::getStatus).containsExactly(Status.PASSED);
    }

    @Test
    void apiResponseAssertTest_fail() {
        AllureResults results = runWithinTestContext(() -> {
            APIResponse response = page.request().get("https://dummy.restapiexample.com/api/v1/foo");
            assertThat(response).isOK();
        }, AssertionsAspect::setLifecycle);

        TestResult testResult = results.getTestResults().get(0);
        assertThat(testResult.getStatus()).isEqualTo(Status.FAILED);
        assertThat(testResult.getSteps()).flatExtracting(StepResult::getName).containsExactly("APIResponse expected to be OK");
        assertThat(testResult.getSteps()).flatExtracting(StepResult::getStatus).containsExactly(Status.FAILED);
    }

    @Test
    void apiResponseAssertNotTest_pass() {
        AllureResults results = runWithinTestContext(() -> {
            APIResponse response = page.request().get("https://dummy.restapiexample.com/api/v1/foo");
            assertThat(response).not().isOK();
        }, AssertionsAspect::setLifecycle);

        TestResult testResult = results.getTestResults().get(0);
        assertThat(testResult.getSteps()).flatExtracting(StepResult::getName).containsExactly("APIResponse expected to not be OK");
        assertThat(testResult.getSteps()).flatExtracting(StepResult::getStatus).containsExactly(Status.PASSED);
    }

    @Test
    void apiResponseAssertNotTest_fail() {
        AllureResults results = runWithinTestContext(() -> {
            APIResponse response = page.request().get("https://playwright.dev");
            assertThat(response).not().isOK();
        }, AssertionsAspect::setLifecycle);

        TestResult testResult = results.getTestResults().get(0);
        assertThat(testResult.getStatus()).isEqualTo(Status.FAILED);
        assertThat(testResult.getSteps()).flatExtracting(StepResult::getName).containsExactly("APIResponse expected to not be OK");
        assertThat(testResult.getSteps()).flatExtracting(StepResult::getStatus).containsExactly(Status.FAILED);
    }
}
