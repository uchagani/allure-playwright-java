package io.github.uchagani.allure.playwright;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.impl.APIResponseAssertionsImpl;
import com.microsoft.playwright.impl.LocatorAssertionsImpl;
import com.microsoft.playwright.impl.RequestOptionsImpl;

import java.lang.reflect.Field;

public class PlaywrightUtils {
    private static final Class<?> assertionsBaseClass;

    static {
        try {
            assertionsBaseClass = Class.forName("com.microsoft.playwright.impl.AssertionsBase");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static String getSelector(Locator locator) throws ClassNotFoundException, NoSuchFieldException, IllegalAccessException {
        Class<?> locatorImplClass = Class.forName("com.microsoft.playwright.impl.LocatorImpl");
        Object locatorImpl = locatorImplClass.cast(locator);
        return getSelectorFromLocatorImpl(locatorImpl);
    }

    public static String getSelector(LocatorAssertionsImpl locatorAssertionsImpl) throws NoSuchFieldException, IllegalAccessException, ClassNotFoundException {
        Object assertionsBase = assertionsBaseClass.cast(locatorAssertionsImpl);
        Object locatorImpl = getLocatorImplFromAssertionsBase(assertionsBase);
        return getSelectorFromLocatorImpl(locatorImpl);
    }

    public static boolean getIsNot(APIResponseAssertionsImpl apiResponseAssertionsImpl) throws NoSuchFieldException, IllegalAccessException {
        Field isNotField = apiResponseAssertionsImpl.getClass().getDeclaredField("isNot");
        isNotField.setAccessible(true);
        return (boolean) isNotField.get(apiResponseAssertionsImpl);
    }

    public static String getAPIRequestMethod(RequestOptionsImpl requestOptionsIml) throws NoSuchFieldException, IllegalAccessException {
        Field method = requestOptionsIml.getClass().getDeclaredField("method");
        method.setAccessible(true);
        return (String) method.get(requestOptionsIml);
    }

    private static Object getLocatorImplFromAssertionsBase(Object assertionsBase) throws NoSuchFieldException, IllegalAccessException {
        Field locatorField = assertionsBaseClass.getDeclaredField("actualLocator");
        locatorField.setAccessible(true);
        return locatorField.get(assertionsBase);
    }

    private static String getSelectorFromLocatorImpl(Object object) throws NoSuchFieldException, IllegalAccessException {
        Field selectorField = object.getClass().getDeclaredField("selector");
        selectorField.setAccessible(true);
        return (String) selectorField.get(object);
    }
}
