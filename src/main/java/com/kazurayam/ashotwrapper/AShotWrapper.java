package com.kazurayam.ashotwrapper;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import ru.yandex.qatools.ashot.AShot;
import ru.yandex.qatools.ashot.Screenshot;
import ru.yandex.qatools.ashot.coordinates.Coords;
import ru.yandex.qatools.ashot.coordinates.WebDriverCoordsProvider;
import ru.yandex.qatools.ashot.shooting.ShootingStrategies;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Objects;
import java.util.List;
import java.util.Set;

public class AShotWrapper {

    /**
     * takes screenshot of the specified WebElement in the target WebPage,
     * and save it into the output file in PNG format.
     *
     * @param webDriver
     * @param by
     * @param file
     */
    public static void saveElementImage(WebDriver webDriver, By by, Options options, File file)
            throws FileNotFoundException {
        BufferedImage image = takeElementImage(webDriver, by, options);
        try (FileOutputStream fos = new FileOutputStream(file)){
            ImageIO.write(image, "PNG", fos);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void saveElementImage(WebDriver webDriver, By by, File file)
            throws FileNotFoundException {
        saveElementImage(webDriver, by, new Options.Builder().build(), file);
    }

    public static void saveEntirePageImage(WebDriver webDriver, Options options, File file) {
        BufferedImage image = takeEntirePageImage(webDriver, options);
        try (FileOutputStream fos = new FileOutputStream(file)) {
            ImageIO.write(image, "PNG", fos);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void saveEntirePageImage(WebDriver webDriver, File file) {
        saveEntirePageImage(webDriver, new Options.Builder().build(), file);
    }

    /**
     * takes screenshot of the specified WebElement in the target WebPage,
     * returns it as a BufferedImage object.
     *
     * If the specified webElement is not found, then screenshot of whole page
     * will be returned.
     *
     * @param webDriver
     * @param by
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

    public static BufferedImage takeElementImage(WebDriver webDriver, By by) {
        return takeElementImage(webDriver, by, AShotWrapper.Options.DEFAULT_OPTIONS);
    }

    /**
     * takes screenshot of the entire page
     * while ignoring some elements specified
     * returns it as a BufferedImage object
     *
     * @param webDriver
     * @return BufferedImage
     */
    public static BufferedImage takeEntirePageImage(WebDriver webDriver, Options options) {
        int timeout = options.getTimeout();
        float dpr = options.getDevicePixelRatio();
        List<By> byList = options.getIgnoredElements();
        AShot aShot = new AShot().
                coordsProvider(new WebDriverCoordsProvider()).
                shootingStrategy(
                        ShootingStrategies.viewportPasting(ShootingStrategies.scaling(dpr), timeout));
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

    public static BufferedImage takeEntirePageImage(WebDriver webDriver) {
        return takeEntirePageImage(webDriver, Options.DEFAULT_OPTIONS);
    }

    /**
     * censor means 検閲 in Japanese.
     *
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
     * Resize the source image to have the given width while retaining the aspect ratio unchanged
     *
     * @param sourceImage raw Screenshot image
     * @param targetWidth resize the sourceImage to this width, retaining the aspect ratio unchanged
     * @return resized image
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
             * @return
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
}
