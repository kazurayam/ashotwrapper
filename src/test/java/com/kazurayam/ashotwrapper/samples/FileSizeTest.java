package com.kazurayam.ashotwrapper.samples;

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import com.kazurayam.ashotwrapper.AShotWrapper;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

/**
 * This class runs AShotWrapper to create a set of screenshots of PNG and
 * JPEG with various compression quality specified. This class runs against
 * 3 URLs as target.
 * - "https://community.developer.atlassian.com/" --- typical web page, mix of text and avator images
 * - "https://offermanwoodshop.com/" --- page with lots of photos
 * - "https://www.fsa.go.jp/kouhou/index.html" --- page without photos
 */

//@Disabled   // this class takes long time to finish.
public class FileSizeTest {

    private static final Path outputDir =
            Paths.get(".").resolve("docs/samples")
                    .resolve(FileSizeTest.class.getName());

    private static WebDriver driver;

    private static final int timeout = 500;

    private static JsonArray targets;

    @BeforeAll
    public static void beforeAll() throws IOException {
        initDir(outputDir);
        targets = loadTargets();
        assert targets.size() == 3;
    }

    private static void initDir(Path dir) throws IOException {
        if (Files.exists(dir)) {
            // delete the directory recursively
            Files.walk(dir)
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        }
        Files.createDirectories(dir);
    }
    private static JsonArray loadTargets() throws IOException {
        InputStream is = FileSizeTest.class.getClassLoader()
                .getResourceAsStream("FileSizeTestTargets.json");
        assert is != null;
        String json = readAllLines(is);
        return JsonParser.parseString(json).getAsJsonArray();
    }

    private static String readAllLines(InputStream inputStream) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int nRead;
        byte[] data = new byte[1024];
        while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }
        buffer.flush();
        byte[] byteArray = buffer.toByteArray();
        String text = new String(byteArray, StandardCharsets.UTF_8);
        return text;
    }

    @BeforeEach
    void beforeEach() {
        driver = openBrowser();
    }

    @AfterEach
    void afterEach() {
        driver.quit();
    }

    private WebDriver openBrowser() {
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--headless");
        driver = new ChromeDriver(options);
        driver.manage().timeouts().implicitlyWait(timeout, TimeUnit.MILLISECONDS);
        driver.manage().window().setSize(new Dimension(1024, 1000));
        return driver;
    }

    @Test
    public void size_measurement() {
        targets.forEach(je -> {
            try {
                // read the data about the target from JSON file
                String url = je.getAsJsonObject().get("URL").getAsString();
                String feature = je.getAsJsonObject().get("feature").getAsString();
                String description = je.getAsJsonObject().get("description").getAsString();
                //System.out.println(String.format("%s %s %s", url, feature, description));

                driver.navigate().to(url);

                Reporter rp = new Reporter();
                rp.setUrl(url);
                rp.setFeature(feature);
                rp.setDescription(description);

                URL urlParsed = toURL(url);
                String host = urlParsed.getHost();
                Path dir = outputDir.resolve(host);
                Files.createDirectories(dir);

                // take a screenshot to save as PNG
                File png = dir.resolve(host + ".png").toFile();
                AShotWrapper.saveEntirePageImage(driver, png);
                rp.setPNG(new FileQualityPair(png, 1.0f));

                // take 10 screenshots as JPEG with step-wised compression quality
                for (int i = 10; i > 0; i--) {
                    float quality = i * 0.1f;
                    File jpg = dir.resolve(host + "-" + (i * 10) + ".jpg").toFile();
                    AShotWrapper.saveEntirePageImageAsJpeg(driver, jpg, quality);
                    rp.addJPEG(new FileQualityPair(jpg, quality));
                }

                // compile the report in Markdown format
                File reportFile = dir.resolve(host + "-report.md").toFile();
                rp.report(reportFile);

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private URL toURL(String url) {
        URL urlParsed;
        try {
            urlParsed = new URL(url);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        return urlParsed;
    }
    class Reporter {
        private String url = null;
        private String feature = null;
        private String description = null;
        private FileQualityPair png;
        private List<FileQualityPair> jpegs = new ArrayList<FileQualityPair>();
        void setUrl(String url) { this.url = url; }
        void setFeature(String feature) { this.feature = feature; }
        void setDescription(String description) {this.description = description; }
        void setPNG(FileQualityPair png) {
            this.png = png;
        }
        void addJPEG(FileQualityPair jpeg) {
            this.jpegs.add(jpeg);
        }
        void report(File markdown) throws IOException {
            StringBuilder sb = new StringBuilder();
            sb.append("# " + url + "\n\n");
            sb.append(feature + "\n\n");
            sb.append("|File|Quality|Size(bytes)|% to PNG|\n");
            sb.append("|:---|------:|----------:|-----:|\n");
            sb.append(String.format("| [%s](./%s) | %1.1f | %,d | %d%% |\n",
                    png.getFile().getName(), png.getFile().getName(),
                    png.getQuality(), png.getFile().length(),
                    100));
            jpegs.forEach(pair -> {
                sb.append(String.format("| [%s](./%s) | %1.1f | %,d | %d%% |\n",
                        pair.getFile().getName(), pair.getFile().getName(),
                        pair.getQuality(), pair.getFile().length(),
                        calcPercentageToPNG(pair.getFile().length(), png.getFile().length())));
            });
            sb.append("\n");
            sb.append(description + "\n\n");
            String text = sb.toString();
            Files.write(markdown.toPath(), text.getBytes());
        }
        private int calcPercentageToPNG(long jpegLength, long pngLength) {
            long delta = pngLength - jpegLength;
            return 100 - (int)((delta * 100) / pngLength);
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
