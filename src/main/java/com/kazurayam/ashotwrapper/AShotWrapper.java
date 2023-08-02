package com.kazurayam.ashotwrapper;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import ru.yandex.qatools.ashot.AShot;
import ru.yandex.qatools.ashot.Screenshot;
import ru.yandex.qatools.ashot.coordinates.Coords;
import ru.yandex.qatools.ashot.coordinates.WebDriverCoordsProvider;
import ru.yandex.qatools.ashot.shooting.ShootingStrategies;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.FileImageOutputStream;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.List;
import java.util.Set;

public class AShotWrapper {

    /**
     * censor means 検閲 in Japanese.
     * @param screenshot AShot's Screenshot instance
     * @return a BufferedImage in which some portions are painted out
     */
    protected static BufferedImage censor(Screenshot screenshot) {
        Color PAINT_IT_COLOR = Color.LIGHT_GRAY;
        BufferedImage bi = screenshot.getImage();
        Graphics2D g2D = bi.createGraphics();
        g2D.setColor(PAINT_IT_COLOR);
        Set<Coords> paintedAreas = screenshot.getIgnoredAreas();
        for (Coords rect : paintedAreas) {
            int x = (int)rect.getX();
            int y = (int)rect.getY();
            int width = (int)rect.getWidth();
            int height = (int)rect.getHeight();
            g2D.fillRect(x, y, width, height);
        }
        return bi;
    }


    /**
     * Resize the source image to have the given width while retaining
     * the aspect ratio unchanged
     *
     * @param sourceImage the source image as a BufferedImage object
     * @param targetWidth resize the sourceImage to this width,
     *                    retaining the aspect ratio (=width/height) unchanged
     * @return a BufferedImage object resized
     */
    protected static BufferedImage resize(BufferedImage sourceImage, int targetWidth) {
        if (targetWidth < 0) return sourceImage;
        int sourceWidth  = sourceImage.getWidth();
        int sourceHeight = sourceImage.getHeight();
        int targetHeight = (int)Math.round((sourceHeight * targetWidth * 1.0) / sourceWidth);

        // create output image
        BufferedImage outputImage = new BufferedImage(targetWidth, targetHeight, sourceImage.getType());

        // scales the input image to the output image
        Graphics2D g2d = outputImage.createGraphics();
        g2d.drawImage(sourceImage, 0, 0, targetWidth, targetHeight, null);
        g2d.dispose();

        return outputImage;
    }

    public static void saveElementImage(WebDriver webDriver, By by, File file)
            throws IOException {
        saveElementImage(webDriver, by, new Options.Builder().build(), file);
    }

    /**
     * takes screenshot of the specified WebElement in the target WebPage,
     * and save it into the output file in PNG format.
     *
     * @param webDriver WebDriver instance
     * @param by By instance
     * @param options AShot.Options instance; should specify DevicePixelRatio
     * @param file file as output
     * @throws IOException when the parent directory is not there, etc
     */
    public static void saveElementImage(WebDriver webDriver, By by,
                                        Options options, File file)
            throws IOException {
        BufferedImage image = takeElementImage(webDriver, by, options);
        writePNG(image, file);
    }

    public static void saveElementImageAsJpeg(WebDriver webDriver, By by,
                                              File file,
                                              float compressionQuality) throws IOException {
        saveElementImageAsJpeg(webDriver, by,
                new Options.Builder().build(), file, compressionQuality);
    }

    public static void saveElementImageAsJpeg(WebDriver webDriver, By by,
                                              Options options, File file,
                                              float compressionQuality)
            throws IOException {
        if (compressionQuality < 0.0f || 1.0f < compressionQuality) {
            throw new IllegalArgumentException(
                    "compression quality must be in the range of [0.1f, 1.0f]");
        }
        BufferedImage image = takeElementImage(webDriver, by, options);
        writeJPEG(image, file, compressionQuality);
    }

    public static void saveEntirePageImage(WebDriver webDriver, File file) throws IOException {
        saveEntirePageImage(webDriver, new Options.Builder().build(), file);
    }

    public static void saveEntirePageImage(WebDriver webDriver, Options options, File file)
            throws IOException {
        BufferedImage image = takeEntirePageImage(webDriver, options);
        writePNG(image, file);
    }

    public static void saveEntirePageImageAsJpeg(WebDriver webDriver, File file, float compressionQuality) throws IOException {
        saveEntirePageImageAsJpeg(webDriver, new Options.Builder().build(), file, compressionQuality);
    }

    public static void saveEntirePageImageAsJpeg(WebDriver webDriver, Options options, File file,
                                                 float compressionQuality) throws IOException {
        if (compressionQuality < 0.1f || 1.0f < compressionQuality) {
            throw new IllegalArgumentException(
                    "compression quality must be in the range of [0.1f, 1.0f]");
        }
        BufferedImage image = takeEntirePageImage(webDriver, options);
        writeJPEG(image, file, compressionQuality);
    }

    public static void savePageImage(WebDriver webDriver, File file) throws IOException {
        savePageImage(webDriver, new Options.Builder().build(), file);
    }

    public static void savePageImage(WebDriver webDriver, Options options, File file) throws IOException {
        BufferedImage image = takePageImage(webDriver, options);
        writePNG(image, file);
    }

    public static void savePageImageAsJpeg(WebDriver webDriver, File file, float compressionQuality) throws IOException {
        savePageImageAsJpeg(webDriver, new Options.Builder().build(), file, compressionQuality);
    }

    public static void savePageImageAsJpeg(WebDriver webDriver, Options options, File file, float compressionQuality) throws IOException {
        BufferedImage image = takePageImage(webDriver, options);
        writeJPEG(image, file, compressionQuality);
    }

    public static BufferedImage takeElementImage(WebDriver webDriver, By by) {
        return takeElementImage(webDriver, by, AShotWrapper.Options.DEFAULT_OPTIONS);
    }

    /**
     * takes screenshot of the specified WebElement in the target WebPage,
     * returns it as a BufferedImage object.
     *
     * If the specified webElement is not found, then screenshot of whole page
     * will be returned.
     *
     * @param webDriver WebDriver instance
     * @param by By instance
     * @param options AShotWrapper.Options instance. Should specify DevicePixelRatio
     * @return BufferedImage
     */
    public static BufferedImage takeElementImage(WebDriver webDriver, By by, Options options) {
        int timeout = options.getTimeout();
        WebElement webElement = webDriver.findElement(by);
        float dpr = options.getDevicePixelRatio();
        Screenshot screenshot = new AShot().
                coordsProvider(new WebDriverCoordsProvider()).
                shootingStrategy(
                        ShootingStrategies.viewportPasting(ShootingStrategies.scaling(dpr), timeout)).
                takeScreenshot(webDriver, webElement);
        return screenshot.getImage();
    }

    public static BufferedImage takeEntirePageImage(WebDriver webDriver) {
        return takeEntirePageImage(webDriver, Options.DEFAULT_OPTIONS);
    }

    /**
     * takes screenshot of the entire page
     * while ignoring some elements specified
     * returns it as a BufferedImage object
     *
     * @param webDriver WebDriver instance
     * @param options AShotWrapper.Options instance; should specify DevicePixelRatio
     * @return BufferedImage
     */
    public static BufferedImage takeEntirePageImage(WebDriver webDriver, Options options) {
        int timeout = options.getTimeout();
        float dpr = options.getDevicePixelRatio();
        AShot aShot = new AShot().
                coordsProvider(new WebDriverCoordsProvider()).
                shootingStrategy(
                        ShootingStrategies.viewportPasting(ShootingStrategies.scaling(dpr), timeout));
        return perform(webDriver, aShot, options);
    }

    private static BufferedImage perform(WebDriver webDriver, AShot aShot, Options options) {
        List<By> byList = options.getIgnoredElements();
        for (By by : byList) {
            aShot = aShot.addIgnoredElement(by);
            //println "added ignored element ${by}";
        }
        Screenshot screenshot = aShot.takeScreenshot(webDriver);

        // paint specific web elements in the page with grey color
        BufferedImage censored = censor(screenshot);

        BufferedImage result;
        // if required, resize the image to make its byte-size smaller
        //println "options.getWidth() is ${options.getWidth()}"
        if (options.getWidth() > 0) {
            result = resize(censored, options.getWidth());
        } else {
            result = censored;
        }
        return result;
    }

    public static BufferedImage takePageImage(WebDriver webDriver) {
        return takePageImage(webDriver, Options.DEFAULT_OPTIONS);
    }

    /**
     * takes screenshot of the current viewport of the web page
     * while ignoring some elements specified
     * returns it as a BufferedImage object
     *
     * @param webDriver WebDriver instance
     * @param options AShotWrapper.Options instance; should specify DevicePixelRatio
     * @return BufferedImage
     */
    public static BufferedImage takePageImage(WebDriver webDriver, Options options) {
        float dpr = options.getDevicePixelRatio();
        AShot aShot = new AShot().
                coordsProvider(new WebDriverCoordsProvider()).
                shootingStrategy(
                        ShootingStrategies.scaling(dpr));  // No image processing is performed
        return perform(webDriver, aShot, options);
    }

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

    public static void writePNG(BufferedImage image, File file) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(file)) {
            ImageIO.write(image, "PNG", fos);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    private static BufferedImage removeAlphaChannel(BufferedImage img) {
        if (!img.getColorModel().hasAlpha()) {
            return img;
        }
        BufferedImage target = createImage(img.getWidth(), img.getHeight(), false);
        Graphics2D g = target.createGraphics();
        // g.setColor(new Color(color, false));
        g.fillRect(0, 0, img.getWidth(), img.getHeight());
        g.drawImage(img, 0, 0, null);
        g.dispose();
        return target;
    }
    private static BufferedImage createImage(int width, int height, boolean hasAlpha) {
        return new BufferedImage(width, height, hasAlpha ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB);
    }

    /**
     *
     */
    public static class Options {

        public static Options DEFAULT_OPTIONS = new Options.Builder().build();

        static private final int SCROLLING_TIMEOUT_DEFAULT = 500;
        static private final int MAXIMUM_IMAGE_WIDTH = 4000;

        private final int timeout;
        private final List<By> ignoredElements;
        private final int width;
        private final float devicePixelRatio;

        public static class Builder {

            private int timeout;
            private final List<By> ignoredElements;
            private int width;
            private float devicePixelRatio;

            public Builder() {
                timeout = SCROLLING_TIMEOUT_DEFAULT;
                ignoredElements = new ArrayList<By>();   // no elements to ignore
                width = -1;  // not specified
                devicePixelRatio = 2.0f;
            }
            /**
             * set scrolling timeout
             * @param value in millisecond. Optional. Defaults to 500 milli seconds
             * @return Builder instace
             */
            public Builder timeout(int value) {
                if (value < 0) {
                    throw new IllegalArgumentException("value(${value}) must not be negative");
                }
                if (value > SCROLLING_TIMEOUT_DEFAULT * 10) {
                    throw new IllegalArgumentException("value(${value}) must be less than " +
                            "or equal to ${DEFAULT_SCROLLING_TIMEOUT * 10} milli-seconds.");
                }
                this.timeout = value;
                return this;
            }
            public Builder addIgnoredElement(By by) {
                Objects.requireNonNull(by, "argument by must not be null");
                this.ignoredElements.add(by);
                return this;
            }
            public Builder width(int value) {
                if (value <= 0) {
                    throw new IllegalArgumentException("value(${value}) must not be negative or equal to 0");
                }
                if (value > MAXIMUM_IMAGE_WIDTH) {
                    throw new IllegalArgumentException("value(${value}) must be less than or equal to ${MAXIMUM_IMAGE_WIDTH}");
                }
                this.width = value;
                return this;
            }
            public Builder devicePixelRatio(float value) {
                this.devicePixelRatio = value;
                return this;
            }
            public Options build() {
                return new Options(this);
            }
        }

        private Options(Builder builder) {
            this.timeout = builder.timeout;
            this.ignoredElements = builder.ignoredElements;
            this.width = builder.width;
            this.devicePixelRatio = builder.devicePixelRatio;
        }

        public int getTimeout() {
            return this.timeout;
        }

        public List<By> getIgnoredElements() {
            return this.ignoredElements;
        }

        public int getWidth() {
            return this.width;
        }

        public float getDevicePixelRatio() {
            return this.devicePixelRatio;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("{\"Options\": {");
            sb.append("\"timeout\":");
            sb.append(timeout);
            sb.append(",");
            sb.append("\"ignoredElements\":");
            sb.append("[");
            for (int i = 0; i < ignoredElements.size(); i++) {
                if (i > 0) {
                    sb.append(",");
                }
                sb.append("\"");
                sb.append(ignoredElements.get(i).toString());
                sb.append("\"");
            }
            sb.append("]");
            sb.append(",");
            sb.append("\"width\":");
            sb.append(width);
            sb.append(",");
            sb.append("\"devicePixelRatio\":");
            sb.append(devicePixelRatio);
            sb.append("}");
            sb.append("}");
            return sb.toString();
        }
    }

    public static class DevicePixelRatioResolver {

        private DevicePixelRatioResolver() {}

        public static float resolveDPR(WebDriver driver) {
            JavascriptExecutor js = (JavascriptExecutor)driver;
            // When window.devicePixelRatio of 2.5, js.executeScript will return a type Double but
            // When window.devicePixelRatio of 2.0, js.executeScript will return a type Long.
            // The returned type moves depending on the value.
            // We need to be careful in type conversion
            Object dpr = js.executeScript("return window.devicePixelRatio;");
            if (dpr instanceof Long) {
                return ((Long) dpr).floatValue();
            } else if (dpr instanceof Double) {
                return ((Double) dpr).floatValue();
            } else {
                throw new RuntimeException("dpr.getClass()=" + dpr.getClass().getName());
            }
        }
    }
}
