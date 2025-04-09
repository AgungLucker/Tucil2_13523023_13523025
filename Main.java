public class Main {
    public static void main(String[] args) {
        System.out.println("Enter the input image path:");
        String inputPath = System.console().readLine();
        System.out.println("Enter the error method (0 for MSE, 1 for MAE):");
        int errorMethod = Integer.parseInt(System.console().readLine());
        System.out.println("Enter the threshold for compression:");
        double threshold = Double.parseDouble(System.console().readLine());
        System.out.println("Enter the minimum block size:");
        int minBlockSize = Integer.parseInt(System.console().readLine());
        System.out.println("Enter the target compression ratio:");
        double targetCompression = Double.parseDouble(System.console().readLine());
        System.out.println("Enter the output image path:");
        String outputPath = System.console().readLine();

        new QuadTreeCompressor(inputPath, errorMethod, threshold, minBlockSize, targetCompression, outputPath);
    }
}