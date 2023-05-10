package com.kazurayam.ashotwrapper.samples;

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
            Paths.get(".").resolve("docs/samples")
                    .resolve(AShotWrapperDemo.class.getName());
    private static WebDriver driver;
    private static final int timeout = 500;
    private AShotWrapper.Options options = null;

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
        driver.manage().window().setSize(new Dimension(800, 400));
        //
        float dpr = AShotWrapper.DevicePixelRatioResolver.resolveDPR(driver);
        this.options = new AShotWrapper.Options.Builder().devicePixelRatio(dpr).build();
    }

    @AfterEach
    void tearDown(){
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    void test_saveElementImage() throws IOException {
        driver.navigate().to("http://example.com");
        File file = outputDir.resolve("test_saveElementImage.png").toFile();
        AShotWrapper.saveElementImage(driver,
                By.xpath("//body/div"), file);
        assertTrue(file.exists());
    }

    @Test
    void test_saveElementImageAsJpeg() throws IOException {
        driver.navigate().to("http://example.com");
        File file = outputDir.resolve("test_saveElementImageAsJpeg.jpg").toFile();
        AShotWrapper.saveElementImageAsJpeg(driver,
                By.xpath("//body/div"), file, 0.7f);
        assertTrue(file.exists());
    }

    @Test
    void test_saveEntirePageImage() throws IOException {
        driver.navigate().to("https://www.iana.org/domains/reserved");
        File file = outputDir.resolve("test_saveEntirePageImage.png").toFile();
        AShotWrapper.saveEntirePageImage(driver, file);
        assertTrue(file.exists());
    }

    @Test
    void test_saveEntirePageImageAsJpeg() throws IOException {
        driver.navigate().to("https://www.iana.org/domains/reserved");
        File file = outputDir.resolve("test_saveEntirePageImageAsJpeg.jpg").toFile();
        AShotWrapper.saveEntirePageImageAsJpeg(driver, file, 0.7f);
        assertTrue(file.exists());
    }

    @Test
    void test_saveEntirePageImageWithCensor() throws IOException {
        driver.navigate().to("http://example.com");
        File file = outputDir.resolve("test_saveEntirePageImageWithCensor.png").toFile();
        AShotWrapper.Options options =
                new AShotWrapper.Options.Builder()
                        .addIgnoredElement(
                                By.xpath("//body/div/p[1]"))
                        .build();
        AShotWrapper.saveEntirePageImage(driver, options, file);
        assertTrue(file.exists());
    }

    @Test
    void test_savePageImage() throws IOException {
        driver.navigate().to("https://www.iana.org/domains/reserved");
        File file = outputDir.resolve("test_savePageImage.png").toFile();
        AShotWrapper.savePageImage(driver, file);
        assertTrue(file.exists());
    }


    @Test
    void test_takeElementImage() throws IOException {
        driver.navigate().to("http://example.com");
        BufferedImage image = AShotWrapper.takeElementImage(driver,
                By.xpath("//body/div"),
                options);
        assertNotNull(image);
        File file = outputDir.resolve("test_takeWebElementImage.png").toFile();
        ImageIO.write(image, "PNG", file);
        assertTrue(file.exists());
    }

    @Test
    void test_takeEntirePageImage() throws IOException {
        driver.navigate().to("https://www.iana.org/domains/reserved");
        BufferedImage image = AShotWrapper.takeEntirePageImage(driver, options);
        assertNotNull(image);
        File file = outputDir.resolve("test_takeEntirePageImage.png").toFile();
        ImageIO.write(image, "PNG", file);
        assertTrue(file.exists());
    }

    @Test
    void test_takePageImage() throws IOException {
        driver.navigate().to("https://www.iana.org/domains/reserved");
        BufferedImage image = AShotWrapper.takePageImage(driver, options);
        assertNotNull(image);
        File file = outputDir.resolve("test_takePageImage.png").toFile();
        ImageIO.write(image, "PNG", file);
        assertTrue(file.exists());
    }
}

