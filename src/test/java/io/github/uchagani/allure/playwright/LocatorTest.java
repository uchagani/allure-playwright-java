package io.github.uchagani.allure.playwright;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import io.qameta.allure.model.Status;
import io.qameta.allure.model.StepResult;
import io.qameta.allure.model.TestResult;
import io.qameta.allure.test.AllureResults;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.stream.Stream;

import static io.github.uchagani.allure.playwright.Constants.*;
import static io.qameta.allure.test.RunUtils.runWithinTestContext;
import static org.assertj.core.api.Assertions.assertThat;

public class LocatorTest {

    final static String checkboxSelector = "#checkbox";
    final static String buttonSelector = "#button";
    Page page;
    Playwright playwright;

    private static Stream<Arguments> locatorPassTestDataProvider() {
        String checkboxHTML = "<input type='checkbox' id='checkbox'>";
        String buttonHTML = "<button type='button' id='button'>I'm a Button!</button>";

        return Stream.of(
                Arguments.of(checkboxHTML, checkboxSelector, checkMethodName, checkStepPrefix),
                Arguments.of(buttonHTML, buttonSelector, clickMethodName, clickStepPrefix),
                Arguments.of(buttonHTML, buttonSelector, dblclickMethodName, dblclickStepPrefix)
        );
    }

    private static Stream<Arguments> locatorFailTestDataProvider() {
        int timeout = 50;
        return Stream.of(
                Arguments.of(checkboxSelector, checkMethodName, checkStepPrefix, new Locator.CheckOptions().setTimeout(timeout)),
                Arguments.of(buttonSelector, clickMethodName, clickStepPrefix, new Locator.ClickOptions().setTimeout(timeout)),
                Arguments.of(buttonSelector, dblclickMethodName, dblclickStepPrefix, new Locator.DblclickOptions().setTimeout(timeout))
        );
    }

    private static void callLocatorMethod(Locator locator, String methodName) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method method = Locator.class.getMethod(methodName);
        method.invoke(locator);
    }

    private static void callLocatorWithParamsMethod(Locator locator, String methodName, Object params) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method method = Locator.class.getMethod(methodName, params.getClass());
        method.invoke(locator, params);
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

    @ParameterizedTest(name = "{2} Pass Test")
    @MethodSource("locatorPassTestDataProvider")
    void locatorPassTest(String html, String selector, String methodName, String stepPrefix) {
        page.setContent(html);
        AllureResults results = runWithinTestContext(() -> {
            Locator locator = page.locator(selector);
            try {
                callLocatorMethod(locator, methodName);
            } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }, ChannelOwnerAspect::setLifecycle);

        TestResult testResult = results.getTestResults().get(0);
        assertThat(testResult.getSteps()).flatExtracting(StepResult::getName).containsExactly(stepPrefix + selector);
        assertThat(testResult.getSteps()).flatExtracting(StepResult::getStatus).containsExactly(Status.PASSED);
    }

    @ParameterizedTest(name = "{1} Fail Test")
    @MethodSource("locatorFailTestDataProvider")
    void locatorFailTest(String selector, String methodName, String stepPrefix, Object params) {
        AllureResults results = runWithinTestContext(() -> {
            Locator locator = page.locator(selector);
            try {
                callLocatorWithParamsMethod(locator, methodName, params);
            } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }, ChannelOwnerAspect::setLifecycle);

        TestResult testResult = results.getTestResults().get(0);
        assertThat(testResult.getSteps()).flatExtracting(StepResult::getName).containsExactly(stepPrefix + selector);
        assertThat(testResult.getSteps()).flatExtracting(StepResult::getStatus).containsExactly(Status.BROKEN);
        assertThat(testResult.getSteps()).extracting("statusDetails.trace").anyMatch(msg -> ((String) msg).contains("TimeoutError"));
    }
}
