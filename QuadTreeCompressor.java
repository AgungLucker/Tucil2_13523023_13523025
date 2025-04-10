import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.*;




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

    public void startCompress(){
        try{
            BufferedImage originalImage = ImageIO.read(new File(inputPath));
            int width = originalImage.getWidth();
            int height = originalImage.getHeight();
            System.out.println("Starting compression...");
            Quadrant compressed = compress(originalImage, 0, 0, width, height);
            BufferedImage compressedImage = imageReconstruction(compressed);
            displayImage(compressedImage);

        } catch (IOException e) {
            System.err.println("Error loading the image: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Method compress
    private Quadrant compress(BufferedImage image, int x, int y, int width, int height) {
        if (width <= minBlockSize || height <= minBlockSize) {
            Quadrant leaf = new Quadrant(x, y, width, height);
            leaf.setColor(averageColor(image, x, y, width, height));
            leaf.setLeaf(true);
            return leaf;
        }
        if (errorMethod == 4) {
            BufferedImage normalizedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Color avgColor = averageColor(image, x, y, width, height);
            int rgb = avgColor.getRGB(); 
            for (int i = 0; i < height; i++) {
                for (int j = 0; j < width; j++) {
                    normalizedImage.setRGB(j, i, rgb);
                }
            }
            double ssim = SSIMMeasurement(image, normalizedImage, x, y, width, height);
            if (ssim >= threshold) {
                Quadrant leaf = new Quadrant(x, y, width, height);
                leaf.setColor(averageColor(image, x, y, width, height));
                leaf.setLeaf(true);
                System.out.printf("SSIM @ (%d,%d,%d,%d): %.4f\n", x, y, width, height, ssim);

                return leaf;
            }
            // ssim < threshold maka bagi 
            System.out.printf("DIV SSIM @4 (%d,%d,%d,%d): %.4f\n", x, y, width, height, ssim);

        } else {
            double error = ErrorMeasurement(image, x, y, width, height);
            if (error < threshold) {
                Quadrant leaf = new Quadrant(x, y, width, height);
                leaf.setColor(averageColor(image, x, y, width, height));
                leaf.setLeaf(true);
                return leaf;
            }
        }

        int halfWidth = width / 2;
        int halfHeight = height / 2;

        Quadrant[] children = new Quadrant[4];
        children[0] = compress(image, x, y, halfWidth, halfHeight); // Top-left
        children[1] = compress(image, x + halfWidth, y, halfWidth, halfHeight); // Top-right
        children[2] = compress(image, x, y + halfHeight, halfWidth, halfHeight); // Bottom-left
        children[3] = compress(image, x + halfWidth, y + halfHeight, halfWidth, halfHeight); // Bottom-right

        Quadrant parent = new Quadrant(x, y, width, height);
        parent.setChildren(children);
        parent.setLeaf(false);
        return parent;
    }
    public BufferedImage imageReconstruction(Quadrant root) {
        BufferedImage reconstructedImage = new BufferedImage(root.getWidth(), root.getHeight(), BufferedImage.TYPE_INT_RGB);
        imageReconstructionProcess(root, reconstructedImage);
        return reconstructedImage;
    }
    // Method reconstruct gambar hasil compress
    public void imageReconstructionProcess(Quadrant node, BufferedImage reconstructedImage) {
        if(node.isLeaf()){
            for (int i= node.getY();i<node.getY()+node.getHeight();i++){
                for (int j=node.getX();j<node.getX()+node.getWidth();j++){
                    reconstructedImage.setRGB(j,i,node.getColor().getRGB());
                }
            }
        }else{
            for (Quadrant child:node.getChildren()){
                imageReconstructionProcess(child, reconstructedImage);
            }
        }
    }

    // Method to display image
    public void displayImage (BufferedImage image){
        System.out.println("Displaying image...");
        JFrame frame = new JFrame("Image");
        JLabel label = new JLabel(new ImageIcon(image));
        frame.getContentPane().add(label, BorderLayout.CENTER);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        //Get screen size
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int maxWidth = screenSize.width - 100;
        int maxHeight = screenSize.height - 100;

        // Adjust frame size if larger than screen
        int frameWidth = Math.min(image.getWidth(), maxWidth);
        int frameHeight = Math.min(image.getHeight(), maxHeight);

        frame.setSize(frameWidth, frameHeight);
        frame.setLocationRelativeTo(null);      
        frame.setVisible(true);
    }

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
        double varianceR = calculateVariance(image, x, y, width, height, 'R');
        double varianceG = calculateVariance(image, x, y, width, height, 'G');
        double varianceB = calculateVariance(image, x, y, width, height, 'B');
        return (varianceR + varianceG + varianceB) / 3.0;
    }

    private double calculateVariance(BufferedImage image, int x, int y, int width, int height, char channel) {
        double variance = 0.0;
        Color avgColor = averageColor(image, x, y, width, height);
        for (int i = y; i < y + height; i++) {
            for (int j = x; j < x + width; j++) {
                Color pixelColor = new Color(image.getRGB(j, i));
                if (channel == 'R') {
                    variance += Math.pow(pixelColor.getRed() - avgColor.getRed(), 2);
                } else if (channel == 'G') {
                    variance += Math.pow(pixelColor.getGreen() - avgColor.getGreen(), 2);
                } else if (channel == 'B') {
                    variance += Math.pow(pixelColor.getBlue() - avgColor.getBlue(), 2);
                }
            }
        }
        return variance / (width * height);
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

    private double SSIMMeasurement(BufferedImage image, BufferedImage normalizedImage, int x, int y, int width, int height) {
        double SSIMr = 0.0, SSIMg = 0.0, SSIMb = 0.0;
        SSIMr = calculateSSIM(image, normalizedImage, x, y, width, height, 'R');
        SSIMg = calculateSSIM(image, normalizedImage, x, y, width, height, 'G');
        SSIMb = calculateSSIM(image, normalizedImage, x, y, width, height, 'B');
        return (SSIMr * 0.299 + SSIMg * 0.587 + SSIMb * 0.114);
    }

    private double calculateSSIM(BufferedImage image, BufferedImage normalizedImage, int x, int y, int width, int height, char channel) {
        double varianceImage = 0;
        double varianceNormalizedImage = 0;
        double covariance = 0;
        Color avgColorImage = averageColor(image, x, y, width, height);

        Color avgColorNormalizedImage = averageColor(normalizedImage, x, y, width, height);
        if (channel == 'R') {
            varianceImage = calculateVariance(image, x, y, width, height, 'R');
            varianceNormalizedImage = calculateVariance(normalizedImage, x, y, width, height, 'R');
            covariance = calculateCovariance(image, normalizedImage, x, y, width, height, 'R');
            return ((2 * avgColorImage.getRed() * avgColorNormalizedImage.getRed() + 6.5025) *(2 * covariance + 58.5225)) /
            ((avgColorImage.getRed() * avgColorImage.getRed() + avgColorNormalizedImage.getRed() * avgColorNormalizedImage.getRed() + 6.5025) *
            (varianceImage + varianceNormalizedImage + 58.5225));

        } else if (channel == 'G') {
            varianceImage = calculateVariance(image, x, y, width, height, 'G');
            varianceNormalizedImage = calculateVariance(normalizedImage, x, y, width, height, 'G');
            covariance = calculateCovariance(image, normalizedImage, x, y, width, height, 'G');
            return ((2 * avgColorImage.getGreen() * avgColorNormalizedImage.getGreen() + 6.5025) * (2 * covariance + 58.5225))/ 
            ((avgColorImage.getGreen() * avgColorImage.getGreen() + avgColorNormalizedImage.getGreen() * avgColorNormalizedImage.getGreen() + 6.5025) * 
            (varianceImage + varianceNormalizedImage + 58.5225));
        } else if (channel == 'B') {
            varianceImage = calculateVariance(image, x, y, width, height, 'B');
            varianceNormalizedImage = calculateVariance(normalizedImage, x, y, width, height, 'B');
            covariance = calculateCovariance(image, normalizedImage, x, y, width, height, 'B');
            return ((2 * avgColorImage.getBlue() * avgColorNormalizedImage.getBlue() + 6.5025) * (2 * covariance + 58.5225))/ 
            ((avgColorImage.getBlue() * avgColorImage.getBlue() + avgColorNormalizedImage.getBlue() * avgColorNormalizedImage.getBlue() + 6.5025) * 
            (varianceImage + varianceNormalizedImage + 58.5225));
        } else {
            return 0.0; // Invalid channel
        }
    }

    private double calculateCovariance(BufferedImage image, BufferedImage normalizedImage, int x, int y, int width, int height, char channel) {
        double covariance = 0.0;
        Color avgColorImage = averageColor(image, x, y, width, height);
        Color avgColorNormalizedImage = averageColor(normalizedImage, x, y, width, height);

        for (int i = y; i < y + height; i++) {
            for (int j = x; j < x + width; j++) {
                Color pixelColor = new Color(image.getRGB(j, i));
                Color normalizedPixelColor = new Color(normalizedImage.getRGB(j, i));
                if (channel == 'R') {
                    covariance += (pixelColor.getRed() - avgColorImage.getRed()) * (normalizedPixelColor.getRed() - avgColorNormalizedImage.getRed());
                } else if (channel == 'G') {
                    covariance += (pixelColor.getGreen() - avgColorImage.getGreen()) * (normalizedPixelColor.getGreen() - avgColorNormalizedImage.getGreen());
                } else if (channel == 'B') {
                    covariance += (pixelColor.getBlue() - avgColorImage.getBlue()) * (normalizedPixelColor.getBlue() - avgColorNormalizedImage.getBlue());
                }
            }
        }
        return covariance / (width * height);
    }

    private Color averageColor(BufferedImage image, int x, int y, int width, int height) {
        long r = 0, g = 0, b = 0;
        int pixelCount = width * height;
        for (int i = y; i < y + height; i++) {
            for (int j = x; j < x + width; j++) {
                Color pixelColor = new Color(image.getRGB(j, i));
                r += pixelColor.getRed();
                g += pixelColor.getGreen();
                b += pixelColor.getBlue();
            }
        }
        return new Color((int)(r / pixelCount), (int)(g / pixelCount), (int)(b / pixelCount));
    }
}