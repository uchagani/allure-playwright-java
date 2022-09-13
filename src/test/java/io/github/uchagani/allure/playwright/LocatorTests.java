package io.github.uchagani.allure.playwright;

import com.microsoft.playwright.Locator;
import io.qameta.allure.test.AllureResults;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

import static io.github.uchagani.allure.playwright.Constants.*;

public class LocatorTests extends TestBase {
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
        assertStepsWhenBroken(results, checkStepPrefix + checkboxSelector);
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
        assertStepsWhenBroken(results, clickStepPrefix + buttonSelector);
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
        assertStepsWhenBroken(results, dblclickStepPrefix + buttonSelector);
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
        assertStepsWhenBroken(results, "Drag " + dragSourceSelector + " to " + dragTargetSelector);
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
        assertStepsWhenBroken(results, "Fill " + textBoxSelector + " with " + value);
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
        assertStepsWhenBroken(results, focusStepPrefix + textBoxSelector);
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
        assertStepsWhenBroken(results, hoverStepPrefix + textBoxSelector);
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
        assertStepsWhenBroken(results, "Press key(s) " + key + " on " + textBoxSelector);
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
        assertStepsWhenBroken(results, selectOptionStepPrefix + selectOptionSelector);
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
        assertStepsWhenBroken(results, setInputFilesStepPrefix + file.getFileName());
    }

    @Test
    void tapTest_Pass() {
        page.setContent(html);
        Locator locator = page.locator(divSelector);
        AllureResults results = runTest(locator::tap);
        assertStepsWhenPassed(results, tapStepPrefix + divSelector);
    }

    @Test
    void tapTest_Fail() {
        Locator locator = page.locator(divSelector);
        AllureResults results = runTest(() -> locator.tap(new Locator.TapOptions().setTimeout(50)));
        assertStepsWhenBroken(results, tapStepPrefix + divSelector);
    }

    @Test
    void typeTest_Pass() {
        String text = "abc";
        page.setContent(html);
        Locator locator = page.locator(textBoxSelector);
        AllureResults results = runTest(() -> locator.type(text));
        assertStepsWhenPassed(results, "Type " + text + " on " + textBoxSelector);
    }

    @Test
    void typeTest_Fail() {
        String text = "abc";
        Locator locator = page.locator(textBoxSelector);
        AllureResults results = runTest(() -> locator.type(text, new Locator.TypeOptions().setTimeout(timeout)));
        assertStepsWhenBroken(results, "Type " + text + " on " + textBoxSelector);
    }

    @Test
    void uncheckTest_Pass() {
        page.setContent(html);
        Locator locator = page.locator(checkedCheckboxSelector);
        AllureResults results = runTest(locator::uncheck);
        assertStepsWhenPassed(results, uncheckStepPrefix + checkedCheckboxSelector);
    }

    @Test
    void uncheckTest_Fail() {
        Locator locator = page.locator(checkedCheckboxSelector);
        AllureResults results = runTest(() -> locator.uncheck(new Locator.UncheckOptions().setTimeout(timeout)));
        assertStepsWhenBroken(results, uncheckStepPrefix + checkedCheckboxSelector);
    }
}
