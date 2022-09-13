package io.github.uchagani.allure.playwright;

import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.assertions.LocatorAssertions;
import io.qameta.allure.model.Status;
import io.qameta.allure.model.StepResult;
import io.qameta.allure.model.TestResult;
import io.qameta.allure.test.AllureResults;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static io.qameta.allure.test.RunUtils.runWithinTestContext;

public class AssertTests extends TestBase {

    @Test
    void assertTest_Pass() {
        page.setContent(html);
        String expectedText = "I'm a Button";
        Locator locator = page.locator(buttonSelector);
        AllureResults results = runTest(() -> assertThat(locator).containsText(expectedText));
        assertStepsWhenPassed(results, "Expect " + buttonSelector + " to have text: " + expectedText);
    }

    @Test
    void assertTest_Fail() {
        String expectedText = "I'm a Button";
        Locator locator = page.locator(buttonSelector);
        AllureResults results = runTest(() -> assertThat(locator).containsText(expectedText, new LocatorAssertions.ContainsTextOptions().setTimeout(timeout)));
        assertStepsWhenFailed(results, "Expect " + buttonSelector + " to have text: " + expectedText);
    }

    @Test
    void isOKAssertTest_Pass() {
        AllureResults results = runWithinTestContext(() -> {
            APIResponse response = page.request().get("https://playwright.dev");
            assertThat(response).isOK();
        }, APIResponseAssertionsImplAspect::setLifecycle);

        TestResult testResult = results.getTestResults().get(0);
        Assertions.assertThat(testResult.getSteps()).flatExtracting(StepResult::getName).containsExactly("Expect APIResponse to be OK");
        Assertions.assertThat(testResult.getSteps()).flatExtracting(StepResult::getStatus).containsExactly(Status.PASSED);
    }

    @Test
    void isOKAssertTest_Fail() {
        AllureResults results = runWithinTestContext(() -> {
            APIResponse response = page.request().get("https://dummy.restapiexample.com/api/v1/foo");
            assertThat(response).isOK();
        }, APIResponseAssertionsImplAspect::setLifecycle);

        TestResult testResult = results.getTestResults().get(0);
        Assertions.assertThat(testResult.getStatus()).isEqualTo(Status.FAILED);
        Assertions.assertThat(testResult.getSteps()).flatExtracting(StepResult::getName).containsExactly("Expect APIResponse to be OK");
        Assertions.assertThat(testResult.getSteps()).flatExtracting(StepResult::getStatus).containsExactly(Status.FAILED);
    }

    @Test
    void notIsOKAssertTest_Pass() {
        AllureResults results = runWithinTestContext(() -> {
            APIResponse response = page.request().get("https://dummy.restapiexample.com/api/v1/foo");
            assertThat(response).not().isOK();
        }, APIResponseAssertionsImplAspect::setLifecycle);

        TestResult testResult = results.getTestResults().get(0);
        Assertions.assertThat(testResult.getSteps()).flatExtracting(StepResult::getName).containsExactly("Expect APIResponse to not be OK");
        Assertions.assertThat(testResult.getSteps()).flatExtracting(StepResult::getStatus).containsExactly(Status.PASSED);
    }

    @Test
    void notIsOKAssertTest_Fail() {
        AllureResults results = runWithinTestContext(() -> {
            APIResponse response = page.request().get("https://playwright.dev");
            assertThat(response).not().isOK();
        }, APIResponseAssertionsImplAspect::setLifecycle);

        TestResult testResult = results.getTestResults().get(0);
        Assertions.assertThat(testResult.getStatus()).isEqualTo(Status.FAILED);
        Assertions.assertThat(testResult.getSteps()).flatExtracting(StepResult::getName).containsExactly("Expect APIResponse to not be OK");
        Assertions.assertThat(testResult.getSteps()).flatExtracting(StepResult::getStatus).containsExactly(Status.FAILED);
    }
}
