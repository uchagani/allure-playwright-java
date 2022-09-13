package io.github.uchagani.allure.playwright;

import com.microsoft.playwright.impl.APIResponseAssertionsImpl;
import io.qameta.allure.Allure;
import io.qameta.allure.AllureLifecycle;
import io.qameta.allure.model.Status;
import io.qameta.allure.model.StepResult;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;

import java.lang.reflect.Field;
import java.util.UUID;

import static io.qameta.allure.util.ResultsUtils.getStatus;
import static io.qameta.allure.util.ResultsUtils.getStatusDetails;

@SuppressWarnings("unused")
@Aspect
public class APIResponseAssertionsImplAspect {
    private static final InheritableThreadLocal<AllureLifecycle> lifecycle =
            new InheritableThreadLocal<AllureLifecycle>() {
                @Override
                protected AllureLifecycle initialValue() {
                    return Allure.getLifecycle();
                }
            };

    @Pointcut("execution(* com.microsoft.playwright.impl.APIResponseAssertionsImpl.isOK())")
    public void apiResponseIsOk() {
    }

    @Before("apiResponseIsOk()")
    public void stepStartApiResponse(final JoinPoint joinPoint) throws NoSuchFieldException, IllegalAccessException {
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        APIResponseAssertionsImpl apiResponseAssertionsImpl = (APIResponseAssertionsImpl) joinPoint.getTarget();
        String stepName = "Expect APIResponse to ";
        if (getIsNot(apiResponseAssertionsImpl)) {
            stepName = stepName + "not ";
        }
        stepName = stepName + "be OK";
        String uuid = UUID.randomUUID().toString();
        StepResult result = new StepResult().setName(stepName);
        getLifecycle().startStep(uuid, result);
    }

    @AfterThrowing(pointcut = "apiResponseIsOk()", throwing = "e")
    public void stepFailed(final Throwable e) {
        getLifecycle().updateStep(s -> s.setStatus(getStatus(e).orElse(Status.BROKEN))
                .setStatusDetails(getStatusDetails(e).orElse(null)));
        getLifecycle().stopStep();
    }

    @AfterReturning(pointcut = "apiResponseIsOk()")
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

    private static boolean getIsNot(APIResponseAssertionsImpl apiResponseAssertionsImpl) throws NoSuchFieldException, IllegalAccessException {
        Field isNotField = apiResponseAssertionsImpl.getClass().getDeclaredField("isNot");
        isNotField.setAccessible(true);
        return (boolean) isNotField.get(apiResponseAssertionsImpl);
    }
}
