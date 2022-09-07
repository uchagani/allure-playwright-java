package io.github.uchagani.allure.playwright;

import com.microsoft.playwright.impl.RequestOptionsImpl;
import io.qameta.allure.Allure;
import io.qameta.allure.AllureLifecycle;
import io.qameta.allure.model.Parameter;
import io.qameta.allure.model.Status;
import io.qameta.allure.model.StepResult;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;

import java.util.*;

import static io.qameta.allure.util.AspectUtils.getParameters;
import static io.qameta.allure.util.ResultsUtils.getStatus;
import static io.qameta.allure.util.ResultsUtils.getStatusDetails;

@SuppressWarnings("unused")
@Aspect
public class APIRequestContextAspect {
    private static final List<String> actionNames = new ArrayList<>(Arrays.asList("delete", "fetch", "get", "head", "patch", "post", "put"));

    private static final InheritableThreadLocal<AllureLifecycle> lifecycle =
            new InheritableThreadLocal<AllureLifecycle>() {
                @Override
                protected AllureLifecycle initialValue() {
                    return Allure.getLifecycle();
                }
            };

    @Pointcut(
            "execution(* com.microsoft.playwright.impl.APIRequestContextImpl.fetchImpl(String, com.microsoft.playwright.impl.RequestOptionsImpl))")
    public void apiRequests() {
        //pointcut body, should be empty
    }

    @Before("apiRequests()")
    public void stepStart(final JoinPoint joinPoint) throws NoSuchFieldException, IllegalAccessException {
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        RequestOptionsImpl requestOptions = (RequestOptionsImpl) joinPoint.getArgs()[1];
        String actionName = PlaywrightUtils.getAPIRequestMethod(requestOptions);

        if (actionNames.stream().noneMatch(actionName::equalsIgnoreCase)) {
            return;
        }

        List<Parameter> parameters = getParameters(methodSignature, joinPoint.getArgs());
        String uuid = UUID.randomUUID().toString();
        String url = parameters.get(0).getValue();
        String stepName = actionName.toUpperCase(Locale.ROOT) + " " + url;
        StepResult result = new StepResult().setName(stepName);
        getLifecycle().startStep(uuid, result);
    }

    @AfterThrowing(pointcut = "apiRequests()", throwing = "e")
    public void stepFailed(final Throwable e) {
        getLifecycle().updateStep(s -> s.setStatus(getStatus(e).orElse(Status.BROKEN))
                .setStatusDetails(getStatusDetails(e).orElse(null)));
        getLifecycle().stopStep();
    }

    @AfterReturning(pointcut = "apiRequests()")
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
