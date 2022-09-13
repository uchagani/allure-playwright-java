package io.github.uchagani.allure.playwright;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.assertions.LocatorAssertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

public class DemoReportTest extends TestBase {
    @Disabled("This test is not part of the actual test suite")
    @Test
    void demoTest() {
        Path file = Paths.get("src/test/resources/content.html");

        Locator checkboxLocator = page.locator(checkboxSelector);
        Locator checkedCheckboxLocator = page.locator(checkedCheckboxSelector);
        Locator buttonLocator = page.locator(buttonSelector);
        Locator textboxLocator = page.locator(textBoxSelector);
        Locator selectOptionLocator = page.locator(selectOptionSelector);

        page.setContent(html);
        page.locator(dragSourceSelector).dragTo(page.locator(dragTargetSelector));
        checkboxLocator.check();
        checkedCheckboxLocator.uncheck();
        buttonLocator.click();
        textboxLocator.press("Shift+a");
        textboxLocator.fill("Hi");
        textboxLocator.type("Hi");
        selectOptionLocator.selectOption("audi");
        page.locator(inputFileSelector).setInputFiles(file);
        page.locator(divSelector).tap();

        assertThat(buttonLocator).isEnabled();
        assertThat(page.locator(buttonSelector)).containsText("I'm a Button!");
        assertThat(checkboxLocator).isChecked();
        assertThat(checkedCheckboxLocator).not().isChecked();
        //failed on purpose
        assertThat(buttonLocator).containsText("Foo", new LocatorAssertions.ContainsTextOptions().setTimeout(timeout));
    }
}
