package com.kazurayam.ashotwrapper.samples;

import com.kazurayam.ashotwrapper.AShotWrapper;
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

public class AShotWrapperCensorDemo {

    private static final Path outputDir =
            Paths.get(".").resolve("build/tmp/testOutput")
                    .resolve(AShotWrapperCensorDemo.class.getName());

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
        driver.manage().window().setSize(new Dimension(1200, 400));
        driver.navigate().to("https://site1.sbisec.co.jp/ETGate/");
        //
        float dpr = AShotWrapper.DevicePixelRatioResolver.resolveDPR(driver);
        aswOptions = new AShotWrapper.Options.Builder().devicePixelRatio(dpr).build();
    }

    @Test
    void test_saveEntirePageImageWithCensor() throws IOException {
        File file = outputDir.resolve("test_saveEntirePageImageWithCensor.png").toFile();
        AShotWrapper.Options options =
                new AShotWrapper.Options.Builder()
                        .addIgnoredElement(
                                By.xpath("//h1[@id='logo']"))
                        .addIgnoredElement(
                                By.cssSelector("td#SUBAREA01 div"))
                        .addIgnoredElement(
                                By.cssSelector("td#SUBAREA01 img"))
                        .addIgnoredElement(
                                By.cssSelector("div.md-l-utl-mt10"))
                        .build();
        AShotWrapper.savePageImage(driver, options, file);
        //AShotWrapper.saveEntirePageImage(driver, options, file);

        assertTrue(file.exists());
    }
}
