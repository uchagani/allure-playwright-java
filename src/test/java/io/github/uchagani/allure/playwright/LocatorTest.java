package io.github.uchagani.allure.playwright;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import io.qameta.allure.model.Status;
import io.qameta.allure.model.StepResult;
import io.qameta.allure.model.TestResult;
import io.qameta.allure.test.AllureResults;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static io.github.uchagani.allure.playwright.Constants.*;
import static io.qameta.allure.test.RunUtils.runWithinTestContext;
import static org.assertj.core.api.Assertions.assertThat;

public class LocatorTest {

    final static String checkboxSelector = "#checkbox";
    final static String buttonSelector = "#button";
    final static String dragSourceSelector = "#source";
    final static String dragTargetSelector = "#target";
    final static String textBoxSelector = "#textbox";
    final static double timeout = 50;
    Page page;
    Playwright playwright;

    @BeforeEach
    void getPage() {
        playwright = Playwright.create();
        page = playwright.chromium().launch().newPage();
    }

    @AfterEach
    void cleanup() {
        playwright.close();
    }

    @Test
    void checkTest_Pass() {
        page.setContent("<input type='checkbox' id='checkbox'>");
        Locator locator = page.locator(checkboxSelector);
        AllureResults results = runTest(locator::check);
        assertStepsWhenPassed(results, checkStepPrefix + checkboxSelector);
    }

    @Test
    void checkTest_Fail() {
        Locator locator = page.locator(checkboxSelector);
        AllureResults results = runTest(() -> locator.check(new Locator.CheckOptions().setTimeout(timeout)));
        assertStepsWhenFailed(results, checkStepPrefix + checkboxSelector);
    }

    @Test
    void clickTest_Pass() {
        page.setContent("<button type='button' id='button'>I'm a Button!</button>");
        Locator locator = page.locator(buttonSelector);
        AllureResults results = runTest(locator::click);
        assertStepsWhenPassed(results, clickStepPrefix + buttonSelector);
    }

    @Test
    void clickTest_Fail() {
        Locator locator = page.locator(buttonSelector);
        AllureResults results = runTest(() -> locator.click(new Locator.ClickOptions().setTimeout(timeout)));
        assertStepsWhenFailed(results, clickStepPrefix + buttonSelector);
    }

    @Test
    void dblclickTest_Pass() {
        page.setContent("<button type='button' id='button'>I'm a Button!</button>");
        Locator locator = page.locator(buttonSelector);
        AllureResults results = runTest(locator::dblclick);
        assertStepsWhenPassed(results, dblclickStepPrefix + buttonSelector);
    }

    @Test
    void dblclickTest_Fail() {
        Locator locator = page.locator(buttonSelector);
        AllureResults results = runTest(() -> locator.dblclick(new Locator.DblclickOptions().setTimeout(timeout)));
        assertStepsWhenFailed(results, dblclickStepPrefix + buttonSelector);
    }

    @Test
    void dragToTest_Pass() throws IOException {
        String content = IOUtils.resourceToString("/dragAndDrop.html", StandardCharsets.UTF_8);
        page.setContent(content);
        Locator sourceLocator = page.locator(dragSourceSelector);
        Locator targetLocator = page.locator(dragTargetSelector);
        AllureResults results = runTest(() -> sourceLocator.dragTo(targetLocator));
        assertStepsWhenPassed(results, "Drag " + dragSourceSelector + " to " + dragTargetSelector);
    }

    @Test
    void dragToTest_Fail() {
        Locator sourceLocator = page.locator(dragSourceSelector);
        Locator targetLocator = page.locator(dragTargetSelector);
        AllureResults results = runTest(() -> sourceLocator.dragTo(targetLocator, new Locator.DragToOptions().setTimeout(timeout)));
        assertStepsWhenFailed(results, "Drag " + dragSourceSelector + " to " + dragTargetSelector);
    }

    @Test
    void fillTest_Pass() {
        String value = "hello";
        page.setContent("<input id='textbox'>");
        Locator locator = page.locator(textBoxSelector);
        AllureResults results = runTest(() -> locator.fill("hello"));
        assertStepsWhenPassed(results, "Fill " + textBoxSelector + " with " + value);
    }

    @Test
    void fillTest_Fail() {
        String value = "hello";
        Locator locator = page.locator(textBoxSelector);
        AllureResults results = runTest(() -> locator.fill("hello", new Locator.FillOptions().setTimeout(timeout)));
        assertStepsWhenFailed(results, "Fill " + textBoxSelector + " with " + value);
    }

    AllureResults runTest(Runnable test) {
        return runWithinTestContext(test, ChannelOwnerAspect::setLifecycle);
    }

    void assertStepsWhenPassed(AllureResults results, String stepName) {
        TestResult testResult = results.getTestResults().get(0);
        assertStepStatusPass(testResult);
        assertStepName(testResult, stepName);
    }

    void assertStepsWhenFailed(AllureResults results, String stepName) {
        TestResult testResult = results.getTestResults().get(0);
        assertStepStatusBroken(testResult);
        assertStepName(testResult, stepName);
        assertThat(testResult.getSteps()).extracting("statusDetails.trace").anyMatch(msg -> ((String) msg).contains("TimeoutError"));
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
