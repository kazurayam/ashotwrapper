package com.kazurayam.ashotwrapper;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ByTest {

    @Disabled
    @Test
    public void test_toString() {
        By by = new By.ByXPath("//*[contains(@class,'container')]");
        assertEquals("foo", by.toString());
    }
}
