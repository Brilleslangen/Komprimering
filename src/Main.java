import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Main {
    public static void main(String[] args) {
        if (args[0].equals("c")){
            compress(args[1]);
            System.out.println("Komprimering fullført");
            return;
        } else if (args[0].equals("d")) {
            decompress(args[1]);
            System.out.println("Utpakking fullført");
            return;
        }
        System.out.println("Du skrev inn noe ugyldig. Dobbeltsjekk at du har skrevet inn filnavn til orginalen" +
                "\n Skriv \"c <navn på orginalfil> for å komprimere\n" +
                "skriv \"d <navn på orginalfill>\" for å pakke-ut");
    }
    private static void compress(String filename) {
        String compressedName = "compressed-" + filename.split("\\.")[0] + ".Z";
        byte[] LZ_Compressed = LZ77.compress(readFile(filename));
        byte[] HUFF_LZ_Compressed = HuffmanTree.compress(LZ_Compressed);
        writeCompressedFile(HUFF_LZ_Compressed, compressedName);
    }

    private static void decompress(String filename) {
        String compressedName = "compressed-" + filename.split("\\.")[0] + ".Z";
        String decompressedName = "decompressed-" + filename;
        byte[] HUFF_decompressed = HuffmanTree.decompress(readCompressedFile(compressedName));
        byte[] LZ_HUFF_decompressed = LZ77.decompress(HUFF_decompressed);
        writeFile(LZ_HUFF_decompressed, decompressedName);
    }

    private static byte[] readCompressedFile(String filename) {
        byte[] bytes;
        try (DataInputStream dis = new DataInputStream((new BufferedInputStream(new FileInputStream(System.getProperty("user.dir") + "/" + filename))))) {
            bytes = dis.readAllBytes();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("An error occured while writing file.");
        }
        return bytes;
    }

    private static void writeCompressedFile(byte[] data, String filename) {
        try (DataOutputStream dos = new DataOutputStream(
                new BufferedOutputStream(new FileOutputStream(System.getProperty("user.dir") + "/" + filename)))) {

            for (byte b : data) {
                dos.writeByte(b);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("An error occured while writing file.");
        }
    }

    private static byte[] readFile(String filename) {
        try {
            return Files.readAllBytes(Paths.get(System.getProperty("user.dir") + "/" + filename));
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("An error occured while loading file.");
        }
    }

    private static void writeFile(byte[] data, String filename) {
        File file = new File(System.getProperty("user.dir") + "/" + filename);
        try {
            Files.write(file.toPath(), data);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("An error occured while writing file.");
        }
    }
}
