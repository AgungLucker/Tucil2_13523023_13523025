import java.awt.*;
import java.awt.image.BufferedImage;




public class QuadTreeCompressor {
    private String inputPath;
    private int errorMethod;
    private double threshold;
    private int minBlockSize;
    private double targetCompression;
    private String outputPath;

    public QuadTreeCompressor(String inputPath, int errorMethod, double threshold, 
                            int minBlockSize, double targetCompression, String outputPath) {
        this.inputPath = inputPath;
        this.errorMethod = errorMethod;
        this.threshold = threshold;
        this.minBlockSize = minBlockSize;
        this.targetCompression = targetCompression;
        this.outputPath = outputPath;
    }

    // Method compress

    // Method reconstruct gambar hasil compress

    // Method error Measurement
    private double ErrorMeasurement(BufferedImage image, int x, int y, int width, int height) {
        return switch(this.errorMethod) {
            case 0 -> varianceMeasurement(image, x, y, width, height);
            case 1 -> MADMeasurement(image, x, y, width, height);
            case 2 -> maxPixelDifferenceMeasurement(image, x, y, width, height);
            case 3 -> entropyMeasurement(image, x, y, width, height);
            default -> throw new IllegalArgumentException("Opsi Metode perhitungan error tidak valid: " + this.errorMethod);
        };
    }

    // Variance
    private double varianceMeasurement(BufferedImage image, int x, int y, int width, int height) {
        int pixelCount = width * height;
        Color avgColor = averageColor(image, x, y, width, height);
        double varianceR = 0, varianceG = 0, varianceB = 0;

        for (int i = y; i < y + height; i++) {
            for (int j = x; j < x + width; j++) {
                Color pixelColor = new Color(image.getRGB(j, i));
                varianceR += Math.pow(pixelColor.getRed() - avgColor.getRed(), 2);
                varianceG += Math.pow(pixelColor.getGreen() - avgColor.getGreen(), 2);
                varianceB += Math.pow(pixelColor.getBlue() - avgColor.getBlue(), 2);
            }
        }
        return (varianceR + varianceG + varianceB) / (pixelCount * 3.0);
    }

    // Mean Absolute Deviation 
    private double MADMeasurement(BufferedImage image, int x, int y, int width, int height) {
        int pixelCount = width * height;
        Color avgColor = averageColor(image, x, y, width, height);
        double MADR = 0, MADG = 0, MADB = 0;

        for (int i = y; i < y + height; i++) {
            for (int j = x; j < x + width; j++) {
                Color pixelColor = new Color(image.getRGB(j, i));
                MADR += Math.abs(pixelColor.getRed() - avgColor.getRed());
                MADG += Math.abs(pixelColor.getGreen() - avgColor.getGreen());
                MADB += Math.abs(pixelColor.getBlue() - avgColor.getBlue());
            }
        }
        return (MADR + MADG + MADB) / (pixelCount * 3.0);
    }

    // Max Pixel Difference 
    private double maxPixelDifferenceMeasurement(BufferedImage image, int x, int y, int width, int height) {
        int minR = 255, minG = 255, minB = 255;
        int maxR = 0, maxG = 0, maxB = 0;

        for (int i = y; i < y + height; i++) {
            for (int j = x; j < x + width; j++) {
                Color pixelColor = new Color(image.getRGB(j, i));

                minR = Math.min(minR, pixelColor.getRed());
                minG = Math.min(minG, pixelColor.getGreen());
                minB = Math.min(minB, pixelColor.getBlue());
                maxR = Math.max(maxR, pixelColor.getRed());
                maxG = Math.max(maxG, pixelColor.getGreen());
                maxB = Math.max(maxB, pixelColor.getBlue());
            }
        }

        int diffR = maxR - minR;
        int diffG = maxG - minG;
        int diffB = maxB - minB;
        return (diffR + diffG + diffB) / 3.0;
    }

    // Entropy
    private double entropyMeasurement(BufferedImage image, int x, int y, int width, int height) {   
        double entropyR = channelEntropy(image, x, y, width, height, 'R');
        double entropyG = channelEntropy(image, x, y, width, height, 'G');
        double entropyB = channelEntropy(image, x, y, width, height, 'B');
        return (entropyR + entropyG + entropyB) / 3.0;
    }

    private double channelEntropy(BufferedImage image, int x, int y, int width, int height, char channel) {
        int[] colorfreqArray = new int[256];
        int totalPixels = width * height;

        for (int i = y; i < y + height; i++) {
            for (int j = x; j < x + width; j++) {
                Color pixelColor = new Color(image.getRGB(j, i));
                int value = 0;
                if (channel == 'R') {
                    value = pixelColor.getRed();
                } else if (channel == 'G') {
                    value = pixelColor.getGreen();
                } else if (channel == 'B') {
                    value = pixelColor.getBlue();
                }
                colorfreqArray[value]++;
            }
        }

        double channelEntropy = 0.0;
        for (int i = 0; i < colorfreqArray.length; i++) {
            int colorFreq = colorfreqArray[i];
            double colorProbability = (double) colorFreq / totalPixels;
            if (colorProbability > 0) {
                channelEntropy -= colorProbability * Math.log(colorProbability) / Math.log(2);
            }
        }
        return channelEntropy;
    }
    private Color averageColor(BufferedImage image, int x, int y, int width, int height) {
        long r = 0, g = 0, b = 0;
        int pixelCount = width * height;
        for (int i = x; i < x + width; i++) {
            for (int j = y; j < y + height; j++) {
                Color pixelColor = new Color(image.getRGB(j, i));
                r += pixelColor.getRed();
                g += pixelColor.getGreen();
                b += pixelColor.getBlue();
            }
        }
        return new Color((int)(r / pixelCount), (int)(g / pixelCount), (int)(b / pixelCount));
    }
}