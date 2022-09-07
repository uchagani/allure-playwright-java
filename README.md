# allure-playwright-java

`allure-playwright-java` adds [Playwright](https://playwright.dev/java) actions as steps inside an [Allure Report](https://docs.qameta.io/allure-report/).

![demo](https://raw.githubusercontent.com/uchagani/allure-playwright-java/main/demo.jpg)

# Requirements
* Java 8+
* Playwright 1.18+

# Getting Started

1. Configure Allure for your test framework by following the instructions in the [Allure documentation](https://docs.qameta.io/allure-report/#_java).  `allure-playwright-java` should work with any test runner but has been tested with JUnit4, Junit5, and TestNG.


2. Add `allure-playwright-java` as a dependency.

```xml
<dependency>
    <groupId>io.github.uchagani</groupId>
    <artifactId>allure-playwright-java</artifactId>
    <version>1.0.0</version>
</dependency>
```
3. Run tests as you normally would.
4. Run `allure serve` to generate and launch report.

# Note:

In order to avoid polluting the report, `allure-playwright-java` currently only adds steps for action methods.  These include:

**Page methods**: `"check", "click", "close", "dblclick", "dragAndDrop", "fill", "focus", "goBack", "goForward", "navigate", "hover", "selectOption", "setInputFiles", "type", "uncheck"`

**Locator methods**: `"check", "click", "dblclick", "dragTo", "fill", "focus", "hover", "press", "selectOption", "setInputFiles", "tap", "uncheck"`

**APIRequestContext methods**: `"delete", "fetch", "get", "head", "patch", "post", "put"`

If you would like steps generated for other classes/methods please create an issue.
