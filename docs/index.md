-   <a href="#ashotwrapper" id="toc-ashotwrapper">AShotWrapper</a>
    -   <a href="#what-is-this" id="toc-what-is-this">What is this?</a>
        -   <a href="#whats" id="toc-whats">What’s ++</a>
    -   <a href="#how-to-use-the-ashotwrapper" id="toc-how-to-use-the-ashotwrapper">How to use the <code>AShotWrapper</code></a>
        -   <a href="#javadoc" id="toc-javadoc">Javadoc</a>
    -   <a href="#sample-codes" id="toc-sample-codes">Sample codes</a>
        -   <a href="#save-a-screenshot-of-the-entire-page-in-png" id="toc-save-a-screenshot-of-the-entire-page-in-png">Save a screenshot of the entire page in PNG</a>
        -   <a href="#save-a-screenshot-of-the-current-viewport-in-png" id="toc-save-a-screenshot-of-the-current-viewport-in-png">Save a screenshot of the current viewport in PNG</a>
        -   <a href="#save-a-screenshot-of-an-element-in-the-page-in-png" id="toc-save-a-screenshot-of-an-element-in-the-page-in-png">Save a screenshot of an element in the page in PNG</a>
        -   <a href="#save-a-screenshot-of-the-entire-page-in-jpeg" id="toc-save-a-screenshot-of-the-entire-page-in-jpeg">Save a screenshot of the entire page in JPEG</a>
        -   <a href="#save-a-screenshot-of-the-current-viewport-in-jpeg" id="toc-save-a-screenshot-of-the-current-viewport-in-jpeg">Save a screenshot of the current viewport in JPEG</a>
        -   <a href="#save-a-screenshot-of-an-element-in-the-page-in-jpeg" id="toc-save-a-screenshot-of-an-element-in-the-page-in-jpeg">Save a screenshot of an element in the page in JPEG</a>
    -   <a href="#study-how-to-reduce-the-screenshot-file-size" id="toc-study-how-to-reduce-the-screenshot-file-size">Study how to reduce the screenshot file size</a>

# AShotWrapper

## What is this?

The [`AShot`](https://github.com/pazone/ashot) is WebDriver screenshot library in Java. My `com.kazurayam.ashotwrapper.AShotWrapper` is a thin wrapper class of the [AShot](https://github.com/pazone/ashot) library.

The `com.kazurayam.ashotwrapper.AShotWrapper` class simplifies using AShot in the [Visual Inspection in Katalon Studio](https://forum.katalon.com/t/visual-inspection-in-katalon-studio-reborn/57440) project.

### What’s ++

The `com.kazurayam.ashotwrapper.AShotWrapper` class provides some additional features that the AShot library doesn’t.

1.  `AShotWrapper` optionally enables you to save screenshots in JPEG format while specifying compression quality. This is helpful in some cases to reduce the size of output files.

2.  `AShotWrapper` optionally enables you to "censor" screenshots. You can paint the HTML elements in the screenshot with grey color. Effectively the painted HTML elements are ignored when you perform visual comparisons.

## How to use the `AShotWrapper`

The artifact is available at the Maven Central repository:

-   <https://mvnrepository.com/artifact/com.kazurayam/ashotwrapper>

You can use Gradle or Maven to use this library in your Java/Groovy project. Assuming you use Gradle, you just want to wraite your \`build.gradle:

    implementation group: 'com.kazurayam', name: 'ashotwrapper', version: '0.2.0'

If you want to use this library in your Katalon Studio project, you want to download 2 jars into the `Drivers` folder of your Katalon Studio project.

-   [ashot](https://repo1.maven.org/maven2/ru/yandex/qatools/ashot/ashot/1.5.4/ashot-1.5.4.jar)

-   visit the page of [ashotwrapper](https://mvnrepository.com/artifact/com.kazurayam/ashotwrapper) in the Maven Central, find the latest version and click the `jar` link to download it.

### Javadoc

Javadoc is [here](https://kazurayam.github.io/ashotwrapper/api/index.html)

## Sample codes

Here I will present a JUnit5 test class [`com.kazurayam.ashotwrapper.samples.AShotWrapperDemo`](https://github.com/kazurayam/ashotwrapper/blob/develop/src/test/java/com/kazurayam/ashotwrapper/samples/AShotWrapperDemo.java) to demonstrate how to use the `AShotWrapper` class.

The test class starts with the package statement, import statements, class declaration and some common boilerplate methods; it is as follows:

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

Now I will show each test methods that demonstrates how to use AShotWrapper, with resulting image files.

### Save a screenshot of the entire page in PNG

The following code takes a screenshot of entire page view of the target URL, save the image in a PNG file.

        @Test
        void test_saveEntirePageImage() throws IOException {
            driver.navigate().to("https://www.iana.org/domains/reserved");
            File file = outputDir.resolve("test_saveEntirePageImage.png").toFile();
            AShotWrapper.saveEntirePageImage(driver, file);
            assertTrue(file.exists());
        }

OUTPUT: [entire page screenshot in PNG](https://kazurayam.github.io/ashotwrapper/samples/com.kazurayam.ashotwrapper.samples.AShotWrapperDemo/test_saveEntirePageImage.png)

### Save a screenshot of the current viewport in PNG

The following code takes a screenshot of current viewport in the browser (not the entire page screen shot), save the image in a PNG file.

        @Test
        void test_savePageImage() throws IOException {
            driver.navigate().to("https://www.iana.org/domains/reserved");
            File file = outputDir.resolve("test_savePageImage.png").toFile();
            AShotWrapper.savePageImage(driver, file);
            assertTrue(file.exists());
        }

OUTPUT: [current viewport screenshot in PNG](https://kazurayam.github.io/ashotwrapper/samples/com.kazurayam.ashotwrapper.samples.AShotWrapperDemo/test_savePageImage.png)

### Save a screenshot of an element in the page in PNG

You can select a single HTML element in the target web page, take the screenshot of the element, and save the image into a PNG file.

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

OUTPUT: [element screenshot in PNG](https://kazurayam.github.io/ashotwrapper/samples/com.kazurayam.ashotwrapper.samples.AShotWrapperDemo/test_saveElementImage.png)

### Save a screenshot of the entire page in JPEG

### Save a screenshot of the current viewport in JPEG

### Save a screenshot of an element in the page in JPEG

## Study how to reduce the screenshot file size
