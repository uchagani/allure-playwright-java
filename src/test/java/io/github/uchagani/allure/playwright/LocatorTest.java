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
import java.nio.file.Path;
import java.nio.file.Paths;

import static io.github.uchagani.allure.playwright.Constants.*;
import static io.qameta.allure.test.RunUtils.runWithinTestContext;
import static org.assertj.core.api.Assertions.assertThat;

public class LocatorTest {
    final static String html;
    final static String checkboxSelector = "#checkbox";
    final static String buttonSelector = "#button";
    final static String dragSourceSelector = "#source";
    final static String dragTargetSelector = "#target";
    final static String textBoxSelector = "#textbox";
    final static String selectOptionSelector = "#select";
    final static String inputFileSelector = "#inputFile";
    final static double timeout = 50;
    Page page;
    Playwright playwright;

    static {
        try {
            html = IOUtils.resourceToString("/content.html", StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

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
        page.setContent(html);
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
        page.setContent(html);
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
        page.setContent(html);
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
    void dragToTest_Pass() {
        page.setContent(html);
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
        page.setContent(html);
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

    @Test
    void focusTest_Pass() {
        page.setContent(html);
        Locator locator = page.locator(textBoxSelector);
        AllureResults results = runTest(locator::focus);
        assertStepsWhenPassed(results, focusStepPrefix + textBoxSelector);
    }

    @Test
    void focusTest_Fail() {
        Locator locator = page.locator(textBoxSelector);
        AllureResults results = runTest(() -> locator.focus(new Locator.FocusOptions().setTimeout(timeout)));
        assertStepsWhenFailed(results, focusStepPrefix + textBoxSelector);
    }

    @Test
    void hoverTest_Pass() {
        page.setContent(html);
        Locator locator = page.locator(textBoxSelector);
        AllureResults results = runTest(locator::hover);
        assertStepsWhenPassed(results, hoverStepPrefix + textBoxSelector);
    }

    @Test
    void hoverTest_Fail() {
        Locator locator = page.locator(textBoxSelector);
        AllureResults results = runTest(() -> locator.hover(new Locator.HoverOptions().setTimeout(timeout)));
        assertStepsWhenFailed(results, hoverStepPrefix + textBoxSelector);
    }

    @Test
    void pressTest_Pass() {
        String key = "Shift+a";
        page.setContent(html);
        Locator locator = page.locator(textBoxSelector);
        AllureResults results = runTest(() -> locator.press(key));
        assertStepsWhenPassed(results, "Press key(s) " + key + " on " + textBoxSelector);
    }

    @Test
    void pressTest_Fail() {
        String key = "Shift+a";
        Locator locator = page.locator(textBoxSelector);
        AllureResults results = runTest(() -> locator.press("Shift+a", new Locator.PressOptions().setTimeout(timeout)));
        assertStepsWhenFailed(results, "Press key(s) " + key + " on " + textBoxSelector);
    }

    @Test
    void selectOptionTest_Pass() {
        String value = "audi";
        page.setContent(html);
        Locator locator = page.locator(selectOptionSelector);
        AllureResults results = runTest(() -> locator.selectOption(value));
        assertStepsWhenPassed(results, selectOptionStepPrefix + selectOptionSelector);
    }

    @Test
    void selectOptionTest_Fail() {
        String value = "audi";
        Locator locator = page.locator(selectOptionSelector);
        AllureResults results = runTest(() -> locator.selectOption(value, new Locator.SelectOptionOptions().setTimeout(timeout)));
        assertStepsWhenFailed(results, selectOptionStepPrefix + selectOptionSelector);
    }

    @Test
    void setInputFileTest_Pass() {
        Path file = Paths.get("src/test/resources/content.html");
        page.setContent(html);
        Locator locator = page.locator(inputFileSelector);
        AllureResults results = runTest(() -> locator.setInputFiles(file));
        assertStepsWhenPassed(results, setInputFilesStepPrefix + file.getFileName());
    }

    @Test
    void setInputFileTest_Fail() {
        Path file = Paths.get("src/test/resources/content.html");
        Locator locator = page.locator(inputFileSelector);
        AllureResults results = runTest(() -> locator.setInputFiles(file, new Locator.SetInputFilesOptions().setTimeout(50)));
        assertStepsWhenFailed(results, setInputFilesStepPrefix + file.getFileName());
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
