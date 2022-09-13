package io.github.uchagani.allure.playwright;

import com.microsoft.playwright.Page;
import io.qameta.allure.test.AllureResults;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static io.github.uchagani.allure.playwright.Constants.*;

public class PageMethodTests extends TestBase {

    @Test
    void goBackTest_Pass() {
        AllureResults results = runTest(page::goBack);
        assertStepsWhenPassed(results, goBackStepPrefix);
    }

    @Disabled("Not sure how to make goBack fail")
    @Test
    void goBackTest_Fail() {
        AllureResults results = runTest(page::goBack);
        assertStepsWhenBroken(results, goBackStepPrefix);
    }

    @Test
    void goForwardTest_Pass() {
        AllureResults results = runTest(page::goForward);
        assertStepsWhenPassed(results, goForwardStepPrefix);
    }

    @Disabled("Not sure how to make goForward fail")
    @Test
    void goForward_Fail() {
        AllureResults results = runTest(page::goBack);
        assertStepsWhenBroken(results, goForwardStepPrefix);
    }

    @Test
    void closeTest_Pass() {
        AllureResults results = runTest(page::close);
        assertStepsWhenPassed(results, closeStepPrefix);
    }

    @Disabled("Not sure how to make close fail")
    @Test
    void closeTest_Fail() {
        AllureResults results = runTest(page::close);
        assertStepsWhenBroken(results, closeStepPrefix);
    }

    @Test
    void gotoTest_Pass() {
        String url = "https://bing.com";
        AllureResults results = runTest(() -> page.navigate(url));
        assertStepsWhenPassed(results, gotoStepPrefix + url);
    }

    @Disabled("Not sure how to make navigation fail.  Using a bad URL doesn't seem to work all the time.")
    @Test
    void gotoTest_Fail() {
        String url = "https://" + UUID.randomUUID() + ".com";
        AllureResults results = runTest(() -> page.navigate(url, new Page.NavigateOptions().setTimeout(50)));
        assertStepsWhenBroken(results, gotoStepPrefix + url);
    }
}
