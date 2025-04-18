import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        double threshold;
        int errorMethod, minBlockSize;
        String inputPath, outputPath, GIFPath;
        Scanner scanner = new Scanner(System.in);
        do {
            System.out.println();
            System.out.println("Enter the input image path (absolute path):");
            System.out.print(">> ");
            inputPath = scanner.nextLine().trim();
            if (!(inputPath.toLowerCase().endsWith(".png") ||
            inputPath.toLowerCase().endsWith(".jpg") ||
            inputPath.toLowerCase().endsWith(".jpeg"))) {
                System.out.println("Invalid image file. Must end with .jpg, .jpeg, and .png");
                System.out.println();
            } else {
                break;
            }
        } while (true);

        do {
            System.out.println();
            System.out.println("Enter the error method:");
            System.out.println("0 Variance");
            System.out.println("1 Mean Absolute Deviation");
            System.out.println("2 Max Pixel Difference");
            System.out.println("3 Entropy");
            System.out.print(">> ");
            errorMethod = Integer.parseInt(scanner.nextLine());
            if (errorMethod < 0 || errorMethod > 4) {
                System.out.println("Invalid error method. Please enter a number between 0 and 3.");
                System.out.println();
            } else {
                break;
            }
        } while (true);

        do {
            System.out.println();
            System.out.println("Enter the threshold for compression:");
            System.out.print(">> ");
            threshold = Double.parseDouble(scanner.nextLine());
            if (threshold < 0) {
                System.out.println("Invalid threshold number");
                System.out.println();
            } else {
                if (errorMethod == 3) {
                    if (threshold > 8.0) {
                        System.out.println("Invalid threshold number for entropy. Please enter a number between 0 and 8.");
                    } else {
                        break;
                    }
                } else if (errorMethod == 1 || errorMethod == 2) {
                    if (threshold > 255.0) {
                        System.out.println("Invalid threshold number for mean absolute deviation or max pixel difference. Please enter a number between 0 and 255.");
                    } else {
                        break;
                    }
                } else {
                    break;
                }
            }
        } while (true);

        do {
            System.out.println();
            System.out.println("Enter the minimum block size:");
            System.out.print(">> ");
            minBlockSize = Integer.parseInt(scanner.nextLine());
            if (minBlockSize < 1) {
                System.out.println("Invalid minimum block size.");
                System.out.println();
            } else {
                break;
            }
        } while (true);

        do {
            System.out.println();
            System.out.println("Enter the output image path (absolute path):");
            System.out.print(">> ");
            outputPath = scanner.nextLine().trim();
            if (!(outputPath.toLowerCase().endsWith(".png") ||
            outputPath.toLowerCase().endsWith(".jpg") ||
            outputPath.toLowerCase().endsWith(".jpeg"))) {
                System.out.println("Invalid image file. Must end with .jpg, .jpeg, and .png");
                System.out.println();
            } else {
                break;
            }
        } while (true);

        do {
            System.out.println();
            System.out.println("Enter the output GIF path (absolute path):");
            System.out.print(">> ");
            GIFPath = scanner.nextLine().trim();
            if (!GIFPath.toLowerCase().endsWith(".gif")) {
                System.out.println("Invalid GIF file. Must end with .gif");
                System.out.println();
            } else {
                break;
            }
        } while (true);



        QuadTreeCompressor compressor = new QuadTreeCompressor(inputPath, errorMethod, threshold, minBlockSize, outputPath, GIFPath);
        compressor.startCompress(); 
        scanner.close();
    }
}