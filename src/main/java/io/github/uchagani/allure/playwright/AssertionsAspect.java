package io.github.uchagani.allure.playwright;

import com.microsoft.playwright.impl.APIResponseAssertionsImpl;
import com.microsoft.playwright.impl.LocatorAssertionsImpl;
import io.qameta.allure.Allure;
import io.qameta.allure.AllureLifecycle;
import io.qameta.allure.model.Parameter;
import io.qameta.allure.model.Status;
import io.qameta.allure.model.StepResult;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;

import java.util.List;
import java.util.UUID;

import static io.qameta.allure.util.AspectUtils.getParameters;
import static io.qameta.allure.util.ResultsUtils.getStatus;
import static io.qameta.allure.util.ResultsUtils.getStatusDetails;

@SuppressWarnings("unused")
@Aspect
public class AssertionsAspect {
    private static final InheritableThreadLocal<AllureLifecycle> lifecycle =
            new InheritableThreadLocal<AllureLifecycle>() {
                @Override
                protected AllureLifecycle initialValue() {
                    return Allure.getLifecycle();
                }
            };

    @Pointcut(
            "execution(* com.microsoft.playwright.impl.AssertionsBase.expectImpl(String, com.microsoft.playwright.impl.FrameExpectOptions, Object, String))")
    public void pageOrLocatorAssertionMethod() {
        //pointcut body, should be empty
    }

    @Pointcut("execution(* com.microsoft.playwright.impl.APIResponseAssertionsImpl.isOK())")
    public void apiResponseIsOk() {
        //pointcut body, should be empty
    }

    @Before("apiResponseIsOk()")
    public void stepStartApiResponse(final JoinPoint joinPoint) throws NoSuchFieldException, IllegalAccessException {
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        APIResponseAssertionsImpl apiResponseAssertionsImpl = (APIResponseAssertionsImpl) joinPoint.getTarget();
        String stepName = "APIResponse expected to ";
        if (PlaywrightUtils.getIsNot(apiResponseAssertionsImpl)) {
            stepName = stepName + "not ";
        }
        stepName = stepName + "be OK";
        String uuid = UUID.randomUUID().toString();
        StepResult result = new StepResult().setName(stepName);
        getLifecycle().startStep(uuid, result);
    }

    @Before("pageOrLocatorAssertionMethod()")
    public void stepStart(final JoinPoint joinPoint) throws NoSuchFieldException, ClassNotFoundException, IllegalAccessException {
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        List<Parameter> parameters = getParameters(methodSignature, joinPoint.getArgs());
        String uuid = UUID.randomUUID().toString();
        String message;
        String actual;

        if (parameters.get(3).getValue().startsWith("Locator")) {
            LocatorAssertionsImpl locatorAssertionsImpl = (LocatorAssertionsImpl) joinPoint.getTarget();
            String selector = PlaywrightUtils.getSelector(locatorAssertionsImpl);
            message = parameters.get(3).getValue().replace("Locator", "Locator " + selector);
            actual = parameters.get(2).getValue();
        } else {
            message = parameters.get(3).getValue();
            actual = parameters.get(2).getValue();
        }


        String stepName = String.format("%s %s", message, actual);
        StepResult result = new StepResult().setName(stepName);
        getLifecycle().startStep(uuid, result);
    }

    @AfterThrowing(pointcut = "pageOrLocatorAssertionMethod() || apiResponseIsOk()", throwing = "e")
    public void stepFailed(final Throwable e) {
        getLifecycle().updateStep(s -> s.setStatus(getStatus(e).orElse(Status.BROKEN))
                .setStatusDetails(getStatusDetails(e).orElse(null)));
        getLifecycle().stopStep();
    }

    @AfterReturning(pointcut = "pageOrLocatorAssertionMethod() || apiResponseIsOk()")
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
