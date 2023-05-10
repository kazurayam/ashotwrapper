# Getting started with AShot with AshotWrapper

A thin wrapper for the [AShot](https://github.com/pazone/ashot) library in java,
which makes it easy to use the AShot in the [Visual Inspection in Katalon Studio](https://forum.katalon.com/t/visual-inspection-in-katalon-studio-reborn/57440) project.

The artifact is available at the Maven Central repository:

-   <https://mvnrepository.com/artifact/com.kazurayam/ashotwrapper>

## API

Javadoc is [here](./api/index.html)

## Example

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
        void test_takeElementImage() throws IOException {
            BufferedImage image = AShotWrapper.takeElementImage(driver,
                    By.xpath("//body/div"),
                    aswOptions);
            assertNotNull(image);
            File file = outputDir.resolve("test_takeWebElementImage.png").toFile();
            ImageIO.write(image, "PNG", file);
            assertTrue(file.exists());
        }

        @Test
        void test_takeEntirePageImage() throws IOException {
            BufferedImage image = AShotWrapper.takeEntirePageImage(driver, aswOptions);
            assertNotNull(image);
            File file = outputDir.resolve("test_takeEntirePageImage.png").toFile();
            ImageIO.write(image, "PNG", file);
            assertTrue(file.exists());
        }

        @Test
        void test_saveElementImage() throws IOException {
            File file = outputDir.resolve("test_saveElementImage.png").toFile();
            AShotWrapper.saveElementImage(driver,
                    By.xpath("//body/div"), file);
            assertTrue(file.exists());
        }

        @Test
        void test_saveElementImageAsJpeg() throws IOException {
            File file = outputDir.resolve("test_saveElementImageAsJpeg.jpg").toFile();
            AShotWrapper.saveElementImageAsJpeg(driver,
                    By.xpath("//body/div"), file, 0.7f);
            assertTrue(file.exists());
        }

        @Test
        void test_saveEntirePageImage() throws IOException {
            File file = outputDir.resolve("test_saveEntirePageImage.png").toFile();
            AShotWrapper.saveEntirePageImage(driver, file);
            assertTrue(file.exists());
        }

        @Test
        void test_saveEntirePageImageAsJpeg() throws IOException {
            File file = outputDir.resolve("test_saveEntirePageImageAsJpeg.jpg").toFile();
            AShotWrapper.saveEntirePageImageAsJpeg(driver, file, 0.7f);
            assertTrue(file.exists());
        }

        @Test
        void test_saveEntirePageImageWithCensor() throws IOException {
            File file = outputDir.resolve("test_saveEntirePageImageWithCensor.png").toFile();
            AShotWrapper.Options options =
                    new AShotWrapper.Options.Builder()
                            .addIgnoredElement(
                                    By.xpath("//body/div/p[1]"))
                            .build();
            AShotWrapper.saveEntirePageImage(driver, options, file);
            assertTrue(file.exists());
        }



        @AfterEach
        void tearDown(){
            if (driver != null) {
                driver.quit();
            }
        }

    }

This will emit the following output:

    $ tree build/tmp/testOutput
    build/tmp/testOutput
    └── com.kazurayam.ashotwrapper.AShotWrapperTest
        ├── test_saveElementImage.png
        ├── test_saveEntirePageImage.png
        ├── test_takeEntirePageImage.png
        └── test_takeWebElementImage.png

    1 directory, 4 files

# Motivation, etc.

The AShot library provides a rich set of screenshot functionalities. I appreciate that.
I only need just a part of them in the "Visual Inspection in Katalon Studio" project.
So I made a wrapper to hide the details.
