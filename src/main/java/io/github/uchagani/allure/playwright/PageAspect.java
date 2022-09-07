package io.github.uchagani.allure.playwright;

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
public class PageAspect {
    private static final List<String> actionNames = new ArrayList<>(Arrays.asList("check", "click", "close", "dblclick", "dragAndDrop", "fill", "focus", "goBack", "goForward", "navigate", "hover", "selectOption", "setInputFiles", "type", "uncheck"));
    private static final InheritableThreadLocal<AllureLifecycle> lifecycle =
            new InheritableThreadLocal<AllureLifecycle>() {
                @Override
                protected AllureLifecycle initialValue() {
                    return Allure.getLifecycle();
                }
            };

    @Pointcut("execution(* com.microsoft.playwright.Page.*( ..))")
    public void anyPageMethod() {
        //pointcut body, should be empty
    }

    @Before("anyPageMethod()")
    public void stepStart(final JoinPoint joinPoint) {
        final MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        final String actionName = methodSignature.getName();

        if (actionNames.stream().noneMatch(actionName::equalsIgnoreCase)) {
            return;
        }

        final String uuid = UUID.randomUUID().toString();
        final List<Parameter> parameters = getParameters(methodSignature, joinPoint.getArgs());
        final StepResult result = new StepResult().setName(actionName).setParameters(parameters);
        getLifecycle().startStep(uuid, result);
    }

    @AfterThrowing(pointcut = "anyPageMethod()", throwing = "e")
    public void stepFailed(final Throwable e) {
        getLifecycle().updateStep(s -> s.setStatus(getStatus(e).orElse(Status.BROKEN))
                .setStatusDetails(getStatusDetails(e).orElse(null)));
        getLifecycle().stopStep();
    }

    @AfterReturning(pointcut = "anyPageMethod()")
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
