import com.madgag.gif.fmsware.AnimatedGifEncoder;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.FileImageOutputStream;
import javax.swing.*;


public class QuadTreeCompressor {
    private String inputPath;
    private int errorMethod;
    private double threshold;
    private int minBlockSize;
    private String outputPath;
    private String GIFPath;

    public QuadTreeCompressor(String inputPath, int errorMethod, double threshold, 
                            int minBlockSize, String outputPath, String GIFPath) {
        this.inputPath = inputPath;
        this.errorMethod = errorMethod;
        this.threshold = threshold;
        this.minBlockSize = minBlockSize;
        this.outputPath = outputPath;
        this.GIFPath = GIFPath;
    }

    public void startCompress(){
        try{
            // Load gambar
            BufferedImage originalImage = ImageIO.read(new File(inputPath));
            File originalFile = new File(inputPath);
            System.out.println("Loaded image: " + inputPath);
            
            
            // Kompresi gambar
            System.out.println("Starting compression...");
            int width = originalImage.getWidth();
            int height = originalImage.getHeight();
            long mulai = System.currentTimeMillis();
            Quadrant compressed = compress(originalImage, 0, 0, width, height);
            BufferedImage compressedImage = imageReconstruction(compressed);
            long selesai = System.currentTimeMillis();

            // Menyimpan ke memori (bukan ke file fisik)

            File compressedFile = new File(outputPath);
            Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpg");
            if (!writers.hasNext()) {
                throw new IllegalStateException("No writers found for JPEG format.");
            }
            ImageWriter writer = writers.next();

            ImageWriteParam param = writer.getDefaultWriteParam();
            param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            param.setCompressionQuality(0.5f); 

            try (FileImageOutputStream output = new FileImageOutputStream(compressedFile)) {
                writer.setOutput(output);
                writer.write(null, new IIOImage(compressedImage, null, null), param);
                writer.dispose();
            }
            System.out.println("Image saved to: " + outputPath + "\n");
            
            generateGIF(compressed, originalImage.getWidth(), originalImage.getHeight(), GIFPath);
            System.out.println("GIF saved to: " + GIFPath);
            
            // Ukuran dalam memori setelah kompresi
            long compressedFileSize = compressedFile.length();
            long originalFileSize = originalFile.length(); // ukuran file asli dalam byte
            long compressionRatio = (long) ((1 - ((double) compressedFileSize / originalFileSize)) * 100);

            // Menampilkan informasi kompresi
            System.out.println("Compression completed in " + (selesai - mulai) + " ms");
            System.out.println("Original image size: " + originalFileSize + " bytes");
            System.out.println("Compressed image size: " + compressedFileSize + " bytes");
            System.out.println("Persentase kompresi: " + compressionRatio + "%");
            System.out.println("Kedalaman pohon: " + compressed.getMaxDepth(compressed));
            System.out.println("Banyak simpul: " + compressed.getNodesCount(compressed));

            // Menampilkan gambar terkompresi
            displayImage(compressedImage);
            
            

        } catch (IOException e) {
            System.err.println("Error loading the image: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Method compress
    private Quadrant compress(BufferedImage image, int x, int y, int width, int height) {
        int w1 = width / 2;
        int w2 = width - w1;
        int h1 = height / 2;
        int h2 = height - h1;
        int totalPixel = width * height;
        int totalSubPixel1 = w1 * h1;
        int totalSubPixel2 = w2 * h1;
        int totalSubPixel3 = w1 * h2;
        int totalSubPixel4 = w2 * h2;
        if ((totalPixel >= minBlockSize && (totalSubPixel1 < minBlockSize && 
        totalSubPixel2 < minBlockSize && totalSubPixel3 < minBlockSize && 
        totalSubPixel4 < minBlockSize) || totalPixel <= minBlockSize)) {
            Quadrant leaf = new Quadrant(x, y, width, height);
            leaf.setColor(averageColor(image, x, y, width, height));
            leaf.setLeaf(true);
            return leaf;
        }

        double error = ErrorMeasurement(image, x, y, width, height);
        if (error < threshold) {
            Quadrant leaf = new Quadrant(x, y, width, height);
            leaf.setColor(averageColor(image, x, y, width, height));
            leaf.setLeaf(true);
            return leaf;
        }

        // Rekursi dengan bagi gambar menjadi 4 kuadran
        Quadrant[] children = new Quadrant[4];
        children[0] = compress(image, x, y, w1, h1);                 
        children[1] = compress(image, x + w1, y, w2, h1);            
        children[2] = compress(image, x, y + h1, w1, h2);            
        children[3] = compress(image, x + w1, y + h1, w2, h2);       

        Quadrant parent = new Quadrant(x, y, width, height);
        parent.setChildren(children);
        parent.setLeaf(false);

        Color parentColor = averageColorFromChildren(parent); //Set warna buat node yang bukan leaf
        parent.setColor(parentColor);
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

    // Method untuk menampilkan gambar
    public static void displayImage(BufferedImage image) {
        System.out.println("Displaying image...");

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int maxWidth = screenSize.width - 100;
        int maxHeight = screenSize.height - 100;

        int imgWidth = image.getWidth();
        int imgHeight = image.getHeight();

        BufferedImage displayImage = image;

        // Menyesuaikan ukuran gambar jika lebih besar dari layar
        if (imgWidth > maxWidth || imgHeight > maxHeight) {
            double widthRatio = (double) maxWidth / imgWidth;
            double heightRatio = (double) maxHeight / imgHeight;
            double scale = Math.min(widthRatio, heightRatio);

            int newWidth = (int) (imgWidth * scale);
            int newHeight = (int) (imgHeight * scale);

            Image scaledImg = image.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);

            // Konversi scaled image ke BufferedImage
            displayImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = displayImage.createGraphics();
            g2d.drawImage(scaledImg, 0, 0, null);
            g2d.dispose();
        }

        // Menampilkan gambar
        JFrame frame = new JFrame("Image Viewer");
        JLabel label = new JLabel(new ImageIcon(displayImage));
        frame.getContentPane().add(label, BorderLayout.CENTER);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack(); 
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private void generateGIF(Quadrant root, int width, int height, String GIFpath) {
        try {
            AnimatedGifEncoder encoder = new AnimatedGifEncoder();
            encoder.setRepeat(0);
            encoder.setDelay(500); 
            encoder.start(GIFpath);

            int maxDepth = root.getMaxDepth(root);
            for (int i = 0; i <= maxDepth; i++) {  //buat frame
                BufferedImage frame = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
                drawFrame(root, frame, i);
                encoder.addFrame(frame);
            }

            BufferedImage finalFrame = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            imageReconstructionProcess(root, finalFrame);
            encoder.addFrame(finalFrame);

            encoder.finish();
        } catch (Exception e) {
            System.err.println("Error generating GIF: " + e.getMessage());
        }

    }

    private void drawFrame(Quadrant node, BufferedImage frame, int targetDepth) {
        drawFrameRecursive(node, frame, targetDepth, 0);
    }

    private void drawFrameRecursive(Quadrant node, BufferedImage frame, int targetDepth, int currentDepth) {
        if (node == null) return;

        if (node.isLeaf() || currentDepth == targetDepth) {
            for (int i = node.getY(); i < node.getY() + node.getHeight(); i++) {
                for (int j = node.getX(); j < node.getX() + node.getWidth(); j++) {
                    frame.setRGB(j, i, node.getColor().getRGB());
                }
            }
        } else {
            for (Quadrant child : node.getChildren()) {
                drawFrameRecursive(child, frame, targetDepth, currentDepth + 1);
            }
        }
        
    }
    
    
    // Method error Measurement
    private double ErrorMeasurement(BufferedImage image, int x, int y, int width, int height) {
        return switch(this.errorMethod) {
            case 0 -> varianceMeasurement(image, x, y, width, height);
            case 1 -> MADMeasurement(image, x, y, width, height);
            case 2 -> maxPixelDifferenceMeasurement(image, x, y, width, height);
            case 3 -> entropyMeasurement(image, x, y, width, height);
            default -> throw new IllegalArgumentException("Error measurement method invalid: " + this.errorMethod);
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
 
    private Color averageColorFromChildren(Quadrant node) { 
        if (node.getChildren() == null || node.getChildren().length != 4) {
            throw new IllegalArgumentException("Node does not have 4 children");
        }
    
        long r = 0, g = 0, b = 0;
        for (Quadrant child : node.getChildren()) {
            Color c = child.getColor(); 
            r += c.getRed();
            g += c.getGreen();
            b += c.getBlue();
        }
    
        int avgR = (int)(r / 4);
        int avgG = (int)(g / 4);
        int avgB = (int)(b / 4);
    
        return new Color(avgR, avgG, avgB);
    }
    
}