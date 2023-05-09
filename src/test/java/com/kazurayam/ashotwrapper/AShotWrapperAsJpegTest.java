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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
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
        driver.manage().window().setSize(new Dimension(1024, 1000));
        //driver.navigate().to("https://community.developer.atlassian.com/");
        //driver.navigate().to("https://offermanwoodshop.com/");
        driver.navigate().to("https://www.fsa.go.jp/kouhou/index.html");
    }

    @Test
    void test_saveElementImageAsJpeg() throws IOException {
        File screenshotFile = outputDir.resolve("test_saveElementImageAsJpeg.jpg").toFile();
        By by = By.cssSelector("#menu");
        AShotWrapper.saveElementImageAsJpeg(driver, by, screenshotFile, 0.9f);
        assertTrue(screenshotFile.exists());
        assertTrue(screenshotFile.length() > 0);
    }

    @Test
    void test_saveEntirePageImageAsJpeg() throws IOException {
        File screenshotFile = outputDir.resolve("test_saveEntirePageImageAsJpeg.jpg").toFile();
        AShotWrapper.saveEntirePageImageAsJpeg(driver, screenshotFile, 0.7f);
        assertTrue(screenshotFile.exists());
        assertTrue(screenshotFile.length() > 0);
    }

    @Test
    void test_verifyImageSize() throws IOException {

        Reporter rp = new Reporter();
        File png = outputDir.resolve("screenshot.png").toFile();
        AShotWrapper.saveEntirePageImage(driver, png);
        rp.setPNG(new FileQualityPair(png, 1.0f));
        for (int i = 10; i > 0; i--) {
            float quality = i * 0.1f;
            File jpg = outputDir.resolve("screenshot-" + i * 10 + ".jpg").toFile();
            AShotWrapper.saveEntirePageImageAsJpeg(driver, jpg, quality);
            rp.addJPEG(new FileQualityPair(jpg, quality));
        }
        File report = outputDir.resolve("report.md").toFile();
        rp.report(report);
    }

    class Reporter {
        private FileQualityPair png;
        private List<FileQualityPair> jpegs = new ArrayList<FileQualityPair>();
        void setPNG(FileQualityPair png) {
            this.png = png;
        }
        void addJPEG(FileQualityPair jpeg) {
            this.jpegs.add(jpeg);
        }
        void report(File markdown) throws IOException {
            StringBuilder sb = new StringBuilder();
            sb.append("# Screenshots in PNG and JPEG - a study of file size\n");
            sb.append("|File|Quality|Size(bytes)|\n");
            sb.append("|:---|------:|----------:|\n");
            sb.append(String.format("| [%s](./%s) | %1.1f | %,d |\n",
                    png.getFile().getName(), png.getFile().getName(),
                    png.getQuality(), png.getFile().length()));
            jpegs.forEach(pair -> {
                sb.append(String.format("| [%s](./%s) | %1.1f | %,d |\n",
                        pair.getFile().getName(), pair.getFile().getName(),
                        pair.getQuality(), pair.getFile().length()));
            });
            String text = sb.toString();
            Files.write(markdown.toPath(), text.getBytes());
        }
    }
    class FileQualityPair {
        private File file;
        private float quality;
        FileQualityPair(File file, float quality) {
            this.file = file;
            this.quality = quality;
        }
        File getFile() {
            return file;
        }
        float getQuality() {
            return quality;
        }
    }
}
