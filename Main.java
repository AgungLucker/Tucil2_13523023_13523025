import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter the input image path (absolute path):");
        String inputPath = scanner.nextLine();
        System.out.println("Enter the error method:");
        System.out.println("0 Variance");
        System.out.println("1 Mean Absolute Deviation");
        System.out.println("2 Max Pixel Difference");
        System.out.println("3 Entropy");
        System.out.println("4 SSIM");
        int errorMethod = Integer.parseInt(scanner.nextLine());
        System.out.println("Enter the threshold for compression:");
        double threshold = Double.parseDouble(scanner.nextLine());
        System.out.println("Enter the minimum block size:");
        int minBlockSize = Integer.parseInt(scanner.nextLine());
        System.out.println("Enter the target compression ratio:");
        double targetCompression = Double.parseDouble(scanner.nextLine());
        System.out.println("Enter the output image path (absolute path):");
        String outputPath = scanner.nextLine();



        QuadTreeCompressor compressor = new QuadTreeCompressor(inputPath, errorMethod, threshold, minBlockSize, targetCompression, outputPath);
        compressor.startCompress(); 
        scanner.close();
    }
}