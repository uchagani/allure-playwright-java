package io.github.uchagani.allure.playwright;

import com.microsoft.playwright.Locator;
import io.qameta.allure.Allure;
import io.qameta.allure.AllureLifecycle;
import io.qameta.allure.model.Parameter;
import io.qameta.allure.model.Status;
import io.qameta.allure.model.StepResult;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static io.qameta.allure.util.AspectUtils.getParameters;
import static io.qameta.allure.util.ResultsUtils.getStatus;
import static io.qameta.allure.util.ResultsUtils.getStatusDetails;

@SuppressWarnings("unused")
@Aspect
public class LocatorAspect {
    private static final List<String> actionNames = new ArrayList<>(Arrays.asList("check", "click", "dblclick", "dragTo", "fill", "focus", "hover", "press", "selectOption", "setInputFiles", "tap", "uncheck"));

    private static final InheritableThreadLocal<AllureLifecycle> lifecycle =
            new InheritableThreadLocal<AllureLifecycle>() {
                @Override
                protected AllureLifecycle initialValue() {
                    return Allure.getLifecycle();
                }
            };

    @Pointcut("execution(* com.microsoft.playwright.Locator.*( ..))")
    public void locatorMethods() {
        //pointcut body, should be empty
    }

    @Before("locatorMethods()")
    public void stepStart(final JoinPoint joinPoint) throws NoSuchFieldException, ClassNotFoundException, IllegalAccessException {
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        String actionName = methodSignature.getName();

        if (actionNames.stream().noneMatch(actionName::equalsIgnoreCase)) {
            return;
        }

        String uuid = UUID.randomUUID().toString();
        Locator locator = (Locator) joinPoint.getTarget();
        String selector = PlaywrightUtils.getSelector(locator);
        String stepName = actionName + " " + selector;
        List<Parameter> parameters = getParameters(methodSignature, joinPoint.getArgs());
        StepResult result = new StepResult().setName(stepName).setParameters(parameters);
        getLifecycle().startStep(uuid, result);
    }

    @AfterThrowing(pointcut = "locatorMethods()", throwing = "e")
    public void stepFailed(final Throwable e) {
        getLifecycle().updateStep(s -> s.setStatus(getStatus(e).orElse(Status.BROKEN))
                .setStatusDetails(getStatusDetails(e).orElse(null)));
        getLifecycle().stopStep();
    }

    @AfterReturning(pointcut = "locatorMethods()")
    public void stepStop() {
        getLifecycle().updateStep(s -> s.setStatus(Status.PASSED));
        getLifecycle().stopStep();
    }

    public static void setLifecycle(final AllureLifecycle allure) {
        lifecycle.set(allure);
    }

    public static AllureLifecycle getLifecycle() {
        return lifecycle.get();
    }

}
