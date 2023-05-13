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
    -   <a href="#study-how-to-reduce-file-size-of-screenshots-using-jpeg-format" id="toc-study-how-to-reduce-file-size-of-screenshots-using-jpeg-format">Study how to reduce file size of screenshots using JPEG format</a>
        -   <a href="#web-page-with-lots-of-photos" id="toc-web-page-with-lots-of-photos">Web page with lots of photos</a>
        -   <a href="#text-only-web-page" id="toc-text-only-web-page">Text-only web page</a>
        -   <a href="#ordinary-web-page-with-text-and-a-few-images" id="toc-ordinary-web-page-with-text-and-a-few-images">Ordinary web page with text and a few images</a>

# AShotWrapper

back to the [repository](https://github.com/kazurayam/ashotwrapper)

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

The following code takes a screenshot of entire page view of the [target URL](https://www.iana.org/domains/reserved), save the image in a PNG file.

        @Test
        void test_saveEntirePageImage() throws IOException {
            driver.navigate().to("https://www.iana.org/domains/reserved");
            File file = outputDir.resolve("test_saveEntirePageImage.png").toFile();
            AShotWrapper.saveEntirePageImage(driver, file);
            assertTrue(file.exists());
        }

OUTPUT: [entire page screenshot in PNG](https://kazurayam.github.io/ashotwrapper/samples/com.kazurayam.ashotwrapper.samples.AShotWrapperDemo/test_saveEntirePageImage.png)

### Save a screenshot of the current viewport in PNG

The following code takes a screenshot of current viewport of the [target web page](https://www.iana.org/domains/reserved) in the browser (not the entire page screenshot), save the image in a PNG file.

        @Test
        void test_savePageImage() throws IOException {
            driver.navigate().to("https://www.iana.org/domains/reserved");
            File file = outputDir.resolve("test_savePageImage.png").toFile();
            AShotWrapper.savePageImage(driver, file);
            assertTrue(file.exists());
        }

OUTPUT: [current viewport screenshot in PNG](https://kazurayam.github.io/ashotwrapper/samples/com.kazurayam.ashotwrapper.samples.AShotWrapperDemo/test_savePageImage.png)

### Save a screenshot of an element in the page in PNG

You can select a single HTML element in the [target web page](http://example.com/), take the screenshot of the element, and save the image into a PNG file.

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

You can save screenshot images into files in JPEG format. In some cases, a screenshot in JPEG of a web page can be much smaller than PNG.

        @Test
        void test_saveEntirePageImageAsJpeg() throws IOException {
            driver.navigate().to("https://www.iana.org/domains/reserved");
            File file = outputDir.resolve("test_saveEntirePageImageAsJpeg.jpg").toFile();
            AShotWrapper.saveEntirePageImageAsJpeg(driver, file, 0.7f);
            assertTrue(file.exists());
        }

OUTPUT: [entire page screenshot in JPEG](https://kazurayam.github.io/ashotwrapper/samples/com.kazurayam.ashotwrapper.samples.AShotWrapperDemo/test_saveEntirePageImageAsJpeg.jpg)

You can also take screenshot of current viewport and selected HTML element in JPEG as well.

## Study how to reduce file size of screenshots using JPEG format

Selenium WebDriver supports taking screenshot of the browser window. See [tutorials](https://www.browserstack.com/guide/take-screenshots-in-selenium). WebDriver produces files always in PNG format. Sometimes, screenshots in PNG format can be very large in byte size. For example, [this sample screenshot in PNG](https://kazurayam.github.io/ashotwrapper/samples/com.kazurayam.ashotwrapper.samples.FileSizeTest/offermanwoodshop.com/offermanwoodshop.com.png) is as large as 6.5 Mega bytes. If you are going to take many screenshots in your testing project, the size of screenshot images matters. Large image files are difficult to manage and utilize. So I want to make the screenshot image files as small as possible. But how to?

I found some libraries that compress a PNG file into another PNG file of smaller size, for example [Pngquant](https://pngquant.org/). But I do not like to depend on those external libraries. I want a solution that I can use on top of Java8. A well-know resolution is to save screenshots in JPEG, not PNG, while specifying compression quality.

I have written a method `writeJPEG` which does this. With this method, `AShotWrapper` can save any `BufferedImage` object into JPEG file which specifying compression quality like 1.0f, 0.9f, 0.8f, 0.7f, …​ , 0.1f.

        /**
         * write a BufferedImage object into a file in JPEG format with some compression applied
         *
         * @param image BufferedImage
         * @param file File
         * @param compressionQuality [0.0f, 1.0f]
         * @throws IOException when some io failed
         */
        public static void writeJPEG(BufferedImage image, File file, float compressionQuality)
                throws IOException {
            Objects.requireNonNull(image);
            Objects.requireNonNull(file);
            if (compressionQuality < 0.1f || 1.0f < compressionQuality) {
                throw new IllegalArgumentException("compressionQuality must be in the range of [0.1f, 1.0f]");
            }
            ImageWriter jpgWriter = ImageIO.getImageWritersByFormatName("jpg").next();
            ImageWriteParam jpgWriteParam = jpgWriter.getDefaultWriteParam();
            jpgWriteParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            jpgWriteParam.setCompressionQuality(compressionQuality);
            //
            ImageOutputStream outputStream = new FileImageOutputStream(file);
            jpgWriter.setOutput(outputStream);
            IIOImage outputImage =
                    new IIOImage(removeAlphaChannel(image), null, null);
            jpgWriter.write(null, outputImage, jpgWriteParam);
            jpgWriter.dispose();
        }

In theory, the smaller the compression quality is, we can expect the resulting JPEG file will have smaller size at the cost of poorer quality of image view.

Practically, how large the screenshots of web pages will be in PNG, in JPEG with 1.0f, 0.9f, …​ , 0.1f? Different design of web pages may result different file sizes. My ultimate question is, **given a URL to take screenshot, which format should I use: PNG or JPEG? If I choose JPEG, then what value of compression quality should I specify?**

In order to answer to the question, I have created a JUnit5 test, named [FileSizeTest](https://github.com/kazurayam/ashotwrapper/blob/develop/src/test/java/com/kazurayam/ashotwrapper/samples/FileSizeTest.java). This test targets 3 public URL, take screenshots in PNG and JPEG with varying compression quality; the test compiles tables where you can find how large in % each JPEG files are against the baseline PNG file.

### Web page with lots of photos

-   <https://offermanwoodshop.com/>

A web page that is composed of lots of eye-catching photos

<table>
<colgroup>
<col style="width: 25%" />
<col style="width: 25%" />
<col style="width: 25%" />
<col style="width: 25%" />
</colgroup>
<thead>
<tr class="header">
<th style="text-align: left;">File</th>
<th style="text-align: left;">Quality</th>
<th style="text-align: left;">Size(bytes)</th>
<th style="text-align: left;">% to PNG</th>
</tr>
</thead>
<tbody>
<tr class="odd">
<td style="text-align: left;"><p><a href="https://kazurayam.github.io/ashotwrapper/samples/com.kazurayam.ashotwrapper.samples.FileSizeTest/offermanwoodshop.com/offermanwoodshop.com.png">offermanwoodshop.com.png</a></p></td>
<td style="text-align: left;"><p>1.0</p></td>
<td style="text-align: left;"><p>6,588,545</p></td>
<td style="text-align: left;"><p>100%</p></td>
</tr>
<tr class="even">
<td style="text-align: left;"><p><a href="https://kazurayam.github.io/ashotwrapper/samples/com.kazurayam.ashotwrapper.samples.FileSizeTest/offermanwoodshop.com/offermanwoodshop.com-100.jpg">offermanwoodshop.com-100.jpg</a></p></td>
<td style="text-align: left;"><p>1.0</p></td>
<td style="text-align: left;"><p>3,041,837</p></td>
<td style="text-align: left;"><p>47%</p></td>
</tr>
<tr class="odd">
<td style="text-align: left;"><p><a href="https://kazurayam.github.io/ashotwrapper/samples/com.kazurayam.ashotwrapper.samples.FileSizeTest/offermanwoodshop.com/offermanwoodshop.com-90.jpg">offermanwoodshop.com-90.jpg</a></p></td>
<td style="text-align: left;"><p>0.9</p></td>
<td style="text-align: left;"><p>1,187,056</p></td>
<td style="text-align: left;"><p>19%</p></td>
</tr>
<tr class="even">
<td style="text-align: left;"><p><a href="https://kazurayam.github.io/ashotwrapper/samples/com.kazurayam.ashotwrapper.samples.FileSizeTest/offermanwoodshop.com/offermanwoodshop.com-80.jpg">offermanwoodshop.com-80.jpg</a></p></td>
<td style="text-align: left;"><p>0.8</p></td>
<td style="text-align: left;"><p>843,636</p></td>
<td style="text-align: left;"><p>13%</p></td>
</tr>
<tr class="odd">
<td style="text-align: left;"><p><a href="https://kazurayam.github.io/ashotwrapper/samples/com.kazurayam.ashotwrapper.samples.FileSizeTest/offermanwoodshop.com/offermanwoodshop.com-70.jpg">offermanwoodshop.com-70.jpg</a></p></td>
<td style="text-align: left;"><p>0.7</p></td>
<td style="text-align: left;"><p>692,684</p></td>
<td style="text-align: left;"><p>11%</p></td>
</tr>
<tr class="even">
<td style="text-align: left;"><p><a href="https://kazurayam.github.io/ashotwrapper/samples/com.kazurayam.ashotwrapper.samples.FileSizeTest/offermanwoodshop.com/offermanwoodshop.com-60.jpg">offermanwoodshop.com-60.jpg</a></p></td>
<td style="text-align: left;"><p>0.6</p></td>
<td style="text-align: left;"><p>595,758</p></td>
<td style="text-align: left;"><p>10%</p></td>
</tr>
<tr class="odd">
<td style="text-align: left;"><p><a href="https://kazurayam.github.io/ashotwrapper/samples/com.kazurayam.ashotwrapper.samples.FileSizeTest/offermanwoodshop.com/offermanwoodshop.com-50.jpg">offermanwoodshop.com-50.jpg</a></p></td>
<td style="text-align: left;"><p>0.5</p></td>
<td style="text-align: left;"><p>531,487</p></td>
<td style="text-align: left;"><p>9%</p></td>
</tr>
<tr class="even">
<td style="text-align: left;"><p><a href="https://kazurayam.github.io/ashotwrapper/samples/com.kazurayam.ashotwrapper.samples.FileSizeTest/offermanwoodshop.com/offermanwoodshop.com-40.jpg">offermanwoodshop.com-40.jpg</a></p></td>
<td style="text-align: left;"><p>0.4</p></td>
<td style="text-align: left;"><p>469,988</p></td>
<td style="text-align: left;"><p>8%</p></td>
</tr>
<tr class="odd">
<td style="text-align: left;"><p><a href="https://kazurayam.github.io/ashotwrapper/samples/com.kazurayam.ashotwrapper.samples.FileSizeTest/offermanwoodshop.com/offermanwoodshop.com-30.jpg">offermanwoodshop.com-30.jpg</a></p></td>
<td style="text-align: left;"><p>0.3</p></td>
<td style="text-align: left;"><p>404,645</p></td>
<td style="text-align: left;"><p>7%</p></td>
</tr>
<tr class="even">
<td style="text-align: left;"><p><a href="https://kazurayam.github.io/ashotwrapper/samples/com.kazurayam.ashotwrapper.samples.FileSizeTest/offermanwoodshop.com/offermanwoodshop.com-20.jpg">offermanwoodshop.com-20.jpg</a></p></td>
<td style="text-align: left;"><p>0.2</p></td>
<td style="text-align: left;"><p>324,059</p></td>
<td style="text-align: left;"><p>5%</p></td>
</tr>
<tr class="odd">
<td style="text-align: left;"><p><a href="https://kazurayam.github.io/ashotwrapper/samples/com.kazurayam.ashotwrapper.samples.FileSizeTest/offermanwoodshop.com/offermanwoodshop.com-10.jpg">offermanwoodshop.com-10.jpg</a></p></td>
<td style="text-align: left;"><p>0.1</p></td>
<td style="text-align: left;"><p>223,726</p></td>
<td style="text-align: left;"><p>4%</p></td>
</tr>
</tbody>
</table>

Photo-rich page results in the screenshots of very large size. PNG is surprisingly lager than JPEG of the compression quality 1.0. PNG is not suitable for photo-rich pages. You should save the screenshots of photo-rich pages in JPEG, seriously.

### Text-only web page

-   <https://www.fsa.go.jp/kouhou/index.html>

Text-rich page without eye-catching photos

<table>
<colgroup>
<col style="width: 25%" />
<col style="width: 25%" />
<col style="width: 25%" />
<col style="width: 25%" />
</colgroup>
<thead>
<tr class="header">
<th style="text-align: left;">File</th>
<th style="text-align: left;">Quality</th>
<th style="text-align: left;">Size(bytes)</th>
<th style="text-align: left;">% to PNG</th>
</tr>
</thead>
<tbody>
<tr class="odd">
<td style="text-align: left;"><p><a href="https://kazurayam.github.io/ashotwrapper/samples/com.kazurayam.ashotwrapper.samples.FileSizeTest/www.fsa.go.jp/www.fsa.go.jp.png">www.fsa.go.jp.png</a></p></td>
<td style="text-align: left;"><p>1.0</p></td>
<td style="text-align: left;"><p>295,798</p></td>
<td style="text-align: left;"><p>100%</p></td>
</tr>
<tr class="even">
<td style="text-align: left;"><p><a href="https://kazurayam.github.io/ashotwrapper/samples/com.kazurayam.ashotwrapper.samples.FileSizeTest/www.fsa.go.jp/www.fsa.go.jp-100.jpg">www.fsa.go.jp-100.jpg</a></p></td>
<td style="text-align: left;"><p>1.0</p></td>
<td style="text-align: left;"><p>569,674</p></td>
<td style="text-align: left;"><p>192%</p></td>
</tr>
<tr class="odd">
<td style="text-align: left;"><p><a href="https://kazurayam.github.io/ashotwrapper/samples/com.kazurayam.ashotwrapper.samples.FileSizeTest/www.fsa.go.jp/www.fsa.go.jp-90.jpg">www.fsa.go.jp-90.jpg</a></p></td>
<td style="text-align: left;"><p>0.9</p></td>
<td style="text-align: left;"><p>280,978</p></td>
<td style="text-align: left;"><p>95%</p></td>
</tr>
<tr class="even">
<td style="text-align: left;"><p><a href="https://kazurayam.github.io/ashotwrapper/samples/com.kazurayam.ashotwrapper.samples.FileSizeTest/www.fsa.go.jp/www.fsa.go.jp-80.jpg">www.fsa.go.jp-80.jpg</a></p></td>
<td style="text-align: left;"><p>0.8</p></td>
<td style="text-align: left;"><p>214,878</p></td>
<td style="text-align: left;"><p>73%</p></td>
</tr>
<tr class="odd">
<td style="text-align: left;"><p><a href="https://kazurayam.github.io/ashotwrapper/samples/com.kazurayam.ashotwrapper.samples.FileSizeTest/www.fsa.go.jp/www.fsa.go.jp-70.jpg">www.fsa.go.jp-70.jpg</a></p></td>
<td style="text-align: left;"><p>0.7</p></td>
<td style="text-align: left;"><p>182,895</p></td>
<td style="text-align: left;"><p>62%</p></td>
</tr>
<tr class="even">
<td style="text-align: left;"><p><a href="https://kazurayam.github.io/ashotwrapper/samples/com.kazurayam.ashotwrapper.samples.FileSizeTest/www.fsa.go.jp/www.fsa.go.jp-60.jpg">www.fsa.go.jp-60.jpg</a></p></td>
<td style="text-align: left;"><p>0.6</p></td>
<td style="text-align: left;"><p>161,678</p></td>
<td style="text-align: left;"><p>55%</p></td>
</tr>
<tr class="odd">
<td style="text-align: left;"><p><a href="https://kazurayam.github.io/ashotwrapper/samples/com.kazurayam.ashotwrapper.samples.FileSizeTest/www.fsa.go.jp/www.fsa.go.jp-50.jpg">www.fsa.go.jp-50.jpg</a></p></td>
<td style="text-align: left;"><p>0.5</p></td>
<td style="text-align: left;"><p>146,857</p></td>
<td style="text-align: left;"><p>50%</p></td>
</tr>
<tr class="even">
<td style="text-align: left;"><p><a href="https://kazurayam.github.io/ashotwrapper/samples/com.kazurayam.ashotwrapper.samples.FileSizeTest/www.fsa.go.jp/www.fsa.go.jp-40.jpg">www.fsa.go.jp-40.jpg</a></p></td>
<td style="text-align: left;"><p>0.4</p></td>
<td style="text-align: left;"><p>133,145</p></td>
<td style="text-align: left;"><p>46%</p></td>
</tr>
<tr class="odd">
<td style="text-align: left;"><p><a href="https://kazurayam.github.io/ashotwrapper/samples/com.kazurayam.ashotwrapper.samples.FileSizeTest/www.fsa.go.jp/www.fsa.go.jp-30.jpg">www.fsa.go.jp-30.jpg</a></p></td>
<td style="text-align: left;"><p>0.3</p></td>
<td style="text-align: left;"><p>117,464</p></td>
<td style="text-align: left;"><p>40%</p></td>
</tr>
<tr class="even">
<td style="text-align: left;"><p><a href="https://kazurayam.github.io/ashotwrapper/samples/com.kazurayam.ashotwrapper.samples.FileSizeTest/www.fsa.go.jp/www.fsa.go.jp-20.jpg">www.fsa.go.jp-20.jpg</a></p></td>
<td style="text-align: left;"><p>0.2</p></td>
<td style="text-align: left;"><p>98,488</p></td>
<td style="text-align: left;"><p>34%</p></td>
</tr>
<tr class="odd">
<td style="text-align: left;"><p><a href="https://kazurayam.github.io/ashotwrapper/samples/com.kazurayam.ashotwrapper.samples.FileSizeTest/www.fsa.go.jp/www.fsa.go.jp-10.jpg">www.fsa.go.jp-10.jpg</a></p></td>
<td style="text-align: left;"><p>0.1</p></td>
<td style="text-align: left;"><p>73,360</p></td>
<td style="text-align: left;"><p>25%</p></td>
</tr>
</tbody>
</table>

Text-rich page results in the screenshots of small size. PNG is smaller than JPEG of the compression quality 1.0. PNG is suitable for text-rich pages.

### Ordinary web page with text and a few images

-   <https://community.developer.atlassian.com/>

Mixture of texts and small number of images. There are a lot of web sites on the net like this.

<table>
<colgroup>
<col style="width: 25%" />
<col style="width: 25%" />
<col style="width: 25%" />
<col style="width: 25%" />
</colgroup>
<thead>
<tr class="header">
<th style="text-align: left;">File</th>
<th style="text-align: left;">Quality</th>
<th style="text-align: left;">Size(bytes)</th>
<th style="text-align: left;">% to PNG</th>
</tr>
</thead>
<tbody>
<tr class="odd">
<td style="text-align: left;"><p><a href="https://kazurayam.github.io/ashotwrapper/samples/com.kazurayam.ashotwrapper.samples.FileSizeTest/community.developer.atlassian.com/community.developer.atlassian.com.png">community.developer.atlassian.com.png</a></p></td>
<td style="text-align: left;"><p>1.0</p></td>
<td style="text-align: left;"><p>582,684</p></td>
<td style="text-align: left;"><p>100%</p></td>
</tr>
<tr class="even">
<td style="text-align: left;"><p><a href="https://kazurayam.github.io/ashotwrapper/samples/com.kazurayam.ashotwrapper.samples.FileSizeTest/community.developer.atlassian.com/community.developer.atlassian.com-100.jpg">community.developer.atlassian.com-100.jpg</a></p></td>
<td style="text-align: left;"><p>1.0</p></td>
<td style="text-align: left;"><p>1,288,390</p></td>
<td style="text-align: left;"><p>221%</p></td>
</tr>
<tr class="odd">
<td style="text-align: left;"><p><a href="https://kazurayam.github.io/ashotwrapper/samples/com.kazurayam.ashotwrapper.samples.FileSizeTest/community.developer.atlassian.com/community.developer.atlassian.com-90.jpg">community.developer.atlassian.com-90.jpg</a></p></td>
<td style="text-align: left;"><p>0.9</p></td>
<td style="text-align: left;"><p>562,293</p></td>
<td style="text-align: left;"><p>97%</p></td>
</tr>
<tr class="even">
<td style="text-align: left;"><p><a href="https://kazurayam.github.io/ashotwrapper/samples/com.kazurayam.ashotwrapper.samples.FileSizeTest/community.developer.atlassian.com/community.developer.atlassian.com-80.jpg">community.developer.atlassian.com-80.jpg</a></p></td>
<td style="text-align: left;"><p>0.8</p></td>
<td style="text-align: left;"><p>410,043</p></td>
<td style="text-align: left;"><p>71%</p></td>
</tr>
<tr class="odd">
<td style="text-align: left;"><p><a href="https://kazurayam.github.io/ashotwrapper/samples/com.kazurayam.ashotwrapper.samples.FileSizeTest/community.developer.atlassian.com/community.developer.atlassian.com-70.jpg">community.developer.atlassian.com-70.jpg</a></p></td>
<td style="text-align: left;"><p>0.7</p></td>
<td style="text-align: left;"><p>340,999</p></td>
<td style="text-align: left;"><p>59%</p></td>
</tr>
<tr class="even">
<td style="text-align: left;"><p><a href="https://kazurayam.github.io/ashotwrapper/samples/com.kazurayam.ashotwrapper.samples.FileSizeTest/community.developer.atlassian.com/community.developer.atlassian.com-60.jpg">community.developer.atlassian.com-60.jpg</a></p></td>
<td style="text-align: left;"><p>0.6</p></td>
<td style="text-align: left;"><p>295,414</p></td>
<td style="text-align: left;"><p>51%</p></td>
</tr>
<tr class="odd">
<td style="text-align: left;"><p><a href="https://kazurayam.github.io/ashotwrapper/samples/com.kazurayam.ashotwrapper.samples.FileSizeTest/community.developer.atlassian.com/community.developer.atlassian.com-50.jpg">community.developer.atlassian.com-50.jpg</a></p></td>
<td style="text-align: left;"><p>0.5</p></td>
<td style="text-align: left;"><p>264,916</p></td>
<td style="text-align: left;"><p>46%</p></td>
</tr>
<tr class="even">
<td style="text-align: left;"><p><a href="https://kazurayam.github.io/ashotwrapper/samples/com.kazurayam.ashotwrapper.samples.FileSizeTest/community.developer.atlassian.com/community.developer.atlassian.com-40.jpg">community.developer.atlassian.com-40.jpg</a></p></td>
<td style="text-align: left;"><p>0.4</p></td>
<td style="text-align: left;"><p>236,851</p></td>
<td style="text-align: left;"><p>41%</p></td>
</tr>
<tr class="odd">
<td style="text-align: left;"><p><a href="https://kazurayam.github.io/ashotwrapper/samples/com.kazurayam.ashotwrapper.samples.FileSizeTest/community.developer.atlassian.com/community.developer.atlassian.com-30.jpg">community.developer.atlassian.com-30.jpg</a></p></td>
<td style="text-align: left;"><p>0.3</p></td>
<td style="text-align: left;"><p>206,214</p></td>
<td style="text-align: left;"><p>36%</p></td>
</tr>
<tr class="even">
<td style="text-align: left;"><p><a href="https://kazurayam.github.io/ashotwrapper/samples/com.kazurayam.ashotwrapper.samples.FileSizeTest/community.developer.atlassian.com/community.developer.atlassian.com-20.jpg">community.developer.atlassian.com-20.jpg</a></p></td>
<td style="text-align: left;"><p>0.2</p></td>
<td style="text-align: left;"><p>169,583</p></td>
<td style="text-align: left;"><p>30%</p></td>
</tr>
<tr class="odd">
<td style="text-align: left;"><p><a href="https://kazurayam.github.io/ashotwrapper/samples/com.kazurayam.ashotwrapper.samples.FileSizeTest/community.developer.atlassian.com/community.developer.atlassian.com-10.jpg">community.developer.atlassian.com-10.jpg</a></p></td>
<td style="text-align: left;"><p>0.1</p></td>
<td style="text-align: left;"><p>121,342</p></td>
<td style="text-align: left;"><p>21%</p></td>
</tr>
</tbody>
</table>

PNG has smaller file size than JPEG of the compression quality 1.0. The JPEG of compression quality 0.9f is quite similar to PNG. Yes, JPEG could tuned to be smaller than PNG but is not so much significant.
