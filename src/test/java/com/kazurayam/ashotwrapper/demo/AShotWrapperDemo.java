package com.kazurayam.ashotwrapper.demo;

import com.kazurayam.ashotwrapper.AShotWrapper;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

public class AShotWrapperDemo {

    private static final Path outputDir =
            Paths.get(".").resolve("build/tmp/testOutput")
                    .resolve(AShotWrapperDemo.class.getName());

    private static WebDriver driver;

    private static final int timeout = 500;

    private AShotWrapper.Options aswOptions = null;

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
        driver.manage().window().setSize(new Dimension(800, 800));
        driver.navigate().to("http://example.com");
        //
        float dpr = AShotWrapper.DevicePixelRatioResolver.resolveDPR(driver);
        aswOptions = new AShotWrapper.Options.Builder().devicePixelRatio(dpr).build();
    }

    @Test
    void test_takeWebElementImage() throws IOException {
        BufferedImage image = AShotWrapper.takeElementImage(driver,
                By.xpath("//body/div"),
                aswOptions);
        assertNotNull(image);
        File screenshotFile = outputDir.resolve("test_takeWebElementImage.png").toFile();
        ImageIO.write(image, "PNG", screenshotFile);
        assertTrue(screenshotFile.exists());
    }

    @Test
    void test_takeEntirePageImage() throws IOException {
        BufferedImage image = AShotWrapper.takeEntirePageImage(driver, aswOptions);
        assertNotNull(image);
        File screenshotFile = outputDir.resolve("test_takeEntirePageImage.png").toFile();
        ImageIO.write(image, "PNG", screenshotFile);
        assertTrue(screenshotFile.exists());
    }

    @Test
    void test_saveElementImage() throws IOException {
        File screenshotFile = outputDir.resolve("test_saveElementImage.png").toFile();
        AShotWrapper.saveElementImage(driver,
                By.xpath("//body/div"), screenshotFile);
        assertTrue(screenshotFile.exists());
    }

    @Test
    void test_saveEntirePageImage() throws IOException {
        File screenshotFile = outputDir.resolve("test_saveEntirePageImage.png").toFile();
        AShotWrapper.saveEntirePageImage(driver, screenshotFile);
        assertTrue(screenshotFile.exists());
    }



    @AfterEach
    void tearDown(){
        if (driver != null) {
            driver.quit();
        }
    }

}

