package com.kazurayam.ashotwrapper;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class AShotWrapperAsJpegTest {

    private static final Path outputDir =
            Paths.get(".").resolve("build/tmp/testOutput")
                    .resolve(AShotWrapperAsJpegTest.class.getName());

    private static WebDriver driver;

    private static final int timeout = 500;

    @BeforeAll
    static void beforeAll() throws IOException {
        Path dir = outputDir;
        if (Files.exists(dir)) {
            // delete the directory to clear out using Java8 API
            Files.walk(dir)
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        }
        Files.createDirectories(dir);
    }

    @BeforeEach
    void beforeEach(){
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--headless");
        driver = new ChromeDriver(options);
        driver.manage().timeouts().implicitlyWait(timeout, TimeUnit.MILLISECONDS);
        driver.manage().window().setSize(new Dimension(1024, 500));
        driver.navigate().to("http://myadmin.kazurayam.com");
    }

    @Test
    void test_saveElementImageAsJpeg() throws IOException {
        File file = outputDir.resolve("test_saveElementImageAsJpeg.jpg").toFile();
        By by = By.cssSelector("#menu");
        AShotWrapper.saveElementImageAsJpeg(driver, by, file, 0.9f);
        assertTrue(file.exists());
        assertTrue(file.length() > 0);
    }

    @Test
    void test_saveEntirePageImageAsJpeg() throws IOException {
        File file = outputDir.resolve("test_saveEntirePageImageAsJpeg.jpg").toFile();
        AShotWrapper.saveEntirePageImageAsJpeg(driver, file, 0.7f);
        assertTrue(file.exists());
        assertTrue(file.length() > 0);
    }

    @Test
    void test_savePageImageAsJpeg() throws IOException {
        File file = outputDir.resolve("test_savePageImageAsJpeg.jpg").toFile();
        AShotWrapper.savePageImageAsJpeg(driver, file, 0.7f);
        assertTrue(file.exists());
        assertTrue(file.length() > 0);
    }
}
