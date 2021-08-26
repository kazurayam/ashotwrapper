package com.kazurayam.ashotwrapper;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;

public class DevicePixelRatioResolver {

    private DevicePixelRatioResolver() {}

    public static float resolveDPR(WebDriver driver) {
        JavascriptExecutor js = (JavascriptExecutor)driver;
        Long value = (Long)js.executeScript("return window.devicePixelRatio;");
        return (float)value;
    }
}
