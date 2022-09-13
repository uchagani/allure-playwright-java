package io.github.uchagani.allure.playwright;

import com.google.gson.JsonObject;
import io.qameta.allure.Allure;
import io.qameta.allure.AllureLifecycle;
import io.qameta.allure.model.Parameter;
import io.qameta.allure.model.Status;
import io.qameta.allure.model.StepResult;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static io.github.uchagani.allure.playwright.Constants.*;
import static io.qameta.allure.util.AspectUtils.getParameters;
import static io.qameta.allure.util.ResultsUtils.getStatus;
import static io.qameta.allure.util.ResultsUtils.getStatusDetails;

@SuppressWarnings("unused")
@Aspect
public class ChannelOwnerAspect {
    static final Map<String, String> actionMethodNamesMap = new HashMap<>();
    private static final InheritableThreadLocal<AllureLifecycle> lifecycle =
            new InheritableThreadLocal<AllureLifecycle>() {
                @Override
                protected AllureLifecycle initialValue() {
                    return Allure.getLifecycle();
                }
            };

    static {
        actionMethodNamesMap.put(checkMethodName, checkStepPrefix);
        actionMethodNamesMap.put(clickMethodName, clickStepPrefix);
        actionMethodNamesMap.put(dblclickMethodName, dblclickStepPrefix);
        actionMethodNamesMap.put(dragAndDropMethodName, "");
        actionMethodNamesMap.put(fillMethodName, "");
        actionMethodNamesMap.put(focusMethodName, focusStepPrefix);
        actionMethodNamesMap.put(hoverMethodName, hoverStepPrefix);
        actionMethodNamesMap.put(pressMethodName, pressStepPrefix);
        actionMethodNamesMap.put(selectOptionMethodName, selectOptionStepPrefix);
        actionMethodNamesMap.put(setInputFilesMethodName, setInputFilesStepPrefix);
        actionMethodNamesMap.put(tapMethodName, tapStepPrefix);
        actionMethodNamesMap.put(typeMethodName, typeStepPrefix);
        actionMethodNamesMap.put(uncheckMethodName, uncheckStepPrefix);
        actionMethodNamesMap.put(goBackMethodName, goBackStepPrefix);
        actionMethodNamesMap.put(goForwardMethodName, goForwardStepPrefix);
        actionMethodNamesMap.put(closeMethodName, closeStepPrefix);
        actionMethodNamesMap.put(gotoMethodName, gotoStepPrefix);
        actionMethodNamesMap.put(expectMethodName, expectStepPrefix);
        actionMethodNamesMap.put(fetchMethodName, fetchStepPrefix);
    }

    public static AllureLifecycle getLifecycle() {
        return lifecycle.get();
    }

    public static void setLifecycle(final AllureLifecycle allure) {
        lifecycle.set(allure);
    }

    @Pointcut("execution(* com.microsoft.playwright.impl.ChannelOwner.sendMessage(String, com.google.gson.JsonObject))")
    public void actionMethods() {
    }

    @Before("actionMethods()")
    public void beforeActionMethods(final JoinPoint joinPoint) {
        String method = (String) joinPoint.getArgs()[0];
        String actionName = actionMethodNamesMap.get(method);
        if (actionName == null) {
            return;
        }

        String uuid = UUID.randomUUID().toString();
        JsonObject params = (JsonObject) joinPoint.getArgs()[1];
        String stepName = getStepName(method, params);
        List<Parameter> parameters = getParameters((MethodSignature) joinPoint.getSignature(), joinPoint.getArgs());
        StepResult result = new StepResult().setName(stepName).setParameters(parameters);
        getLifecycle().startStep(uuid, result);
    }

    @AfterThrowing(pointcut = "actionMethods()", throwing = "e")
    public void stepFailed(final Throwable e) {
        getLifecycle().updateStep(s -> s.setStatus(getStatus(e).orElse(Status.BROKEN))
                .setStatusDetails(getStatusDetails(e).orElse(null)));
        getLifecycle().stopStep();
    }

    @AfterReturning(pointcut = "actionMethods()")
    public void stepStop() {
        getLifecycle().updateStep(s -> s.setStatus(Status.PASSED));
        getLifecycle().stopStep();
    }

    private String getStepNameForAssertion(JsonObject params) {
        String expression = params.get("expression").getAsString().replace(".", " ");
        String expectedText = "";
        try {
            expectedText = ": " + params.get("expectedText").getAsJsonArray().get(0).getAsJsonObject().get("string").getAsString();
        } catch (NullPointerException npe) {
            // ignore because expected text doesn't exist in the params.
        }
        return "Expect " + params.get("selector").getAsString() + " " + expression + expectedText;
    }

    private String getStepNameForAPIRequest(JsonObject params) {
        return params.get("method").getAsString() + " " + params.get("url").getAsString();
    }

    private String getStepNameForNavigation(JsonObject params) {
        return gotoStepPrefix + params.get("url").getAsString();
    }

    private String getStepNameForLocator(String method, JsonObject params) {
        String selector = params.get("selector").getAsString();
        if (selector.equals(":root")) {
            selector = "page";
        }
        return actionMethodNamesMap.get(method) + selector;
    }

    private String getStepNameForDragAndDrop(JsonObject params) {
        return "Drag " + params.get("source").getAsString() + " to " + params.get("target").getAsString();
    }

    private String getStepNameForFill(JsonObject params) {
        return "Fill " + params.get("selector").getAsString() + " with " + params.get("value").getAsString();
    }

    private String getStepNameForPress(JsonObject params) {
        return "Press key(s) " + params.get("key").getAsString() + " on " + params.get("selector").getAsString();
    }

    private String getStepNameForSetInputFiles(JsonObject params) {
        String filename = params.get("files").getAsJsonArray().get(0).getAsJsonObject().get("name").getAsString();
        return setInputFilesStepPrefix + filename;
    }

    private String getStepNameForType(JsonObject params) {
        return "Type " + params.get("text").getAsString() + " on " + params.get("selector").getAsString();
    }

    private String getStepName(String method, JsonObject params) {
        if (method.equals("fetch")) {
            return getStepNameForAPIRequest(params);
        }

        if (method.equals("goto")) {
            return getStepNameForNavigation(params);
        }

        if (method.equals("expect")) {
            return getStepNameForAssertion(params);
        }

        if (method.equals("dragAndDrop")) {
            return getStepNameForDragAndDrop(params);
        }

        if (method.equals("fill")) {
            return getStepNameForFill(params);
        }

        if (method.equals("press")) {
            return getStepNameForPress(params);
        }

        if (method.equals("setInputFiles")) {
            return getStepNameForSetInputFiles(params);
        }

        if (method.equals("type")) {
            return getStepNameForType(params);
        }

        if (method.equals("goBack")) {
            return goBackStepPrefix;
        }

        if (method.equals("goForward")) {
            return goForwardStepPrefix;
        }

        if (method.equals("close")) {
            return closeStepPrefix;
        }

        return getStepNameForLocator(method, params);
    }
}
