import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class LZ77 {
    private static final int WINDOW_SIZE = Byte.MAX_VALUE, BUFFER_SIZE = Byte.MAX_VALUE;
    private static final String testWord = "a aasqzzz ax xzywplm";
    private static final byte[] bytes = testWord.getBytes();


    public static void main(String[] args) {
        simulate("diverselyx.lyx");
    }

    private static void simulate(String filename) {
        String compFilename = "compressed-" + filename.split("\\.")[0] + ".Z";
        String uncompFilename = "uncompressed-" + filename;

        // Compress
        byte[] compressed = compress(readFile(filename));
        writeCompressedFile(compressed, compFilename);

        // Decompress
        byte[] decompressed = decompress(readCompressedFile(compFilename));
        writeFile(decompressed, uncompFilename);
    }

    private static void readWriteTest() {
        byte[] compressed = compress(bytes);
        writeCompressedFile(compressed, "test.Z");

        // Decompress
        byte[] decompressed = decompress(readCompressedFile("test.Z"));
        writeFile(decompressed, "test.txt");
    }

    private static void translateTest() {
        byte[] compressed = compress(bytes);
        System.out.println("Word to compress : \n" + testWord + "\n");
        System.out.println("Compressed: ");
        for (byte b : compressed) {
            System.out.print(b + " ");
        }

        System.out.println("\nDecompressed: ");
        byte[] decompressed = decompress(compressed);
        for (byte b : decompressed) {
            System.out.print((char) b);
        }
    }

    private static byte[] readCompressedFile(String filename) {
        byte[] bytes;
        try (DataInputStream dis = new DataInputStream((new BufferedInputStream(new FileInputStream(System.getProperty("user.dir") + "/" + filename))))) {
            bytes = dis.readAllBytes();
            dis.close();
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
            dos.flush();
            dos.close();
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


    private static byte[] decompress(byte[] encodedStream) {
        ArrayList<Byte> decodedStream = new ArrayList<>();

        for (int position = 0; position < encodedStream.length; position++) {
            // if uncompressed
            if (encodedStream[position] < 0) {
                for (int i = 0; i < Math.abs(encodedStream[position - i]); i++) {
                    position++;
                    decodedStream.add(encodedStream[position]);
                }
                continue;
            }

            // if sequence reference
            int offset = encodedStream[position];
            int length = encodedStream[position + 1];
            position++;

            for (int l = 0; l < length; l++) {
                decodedStream.add(decodedStream.get(decodedStream.size() - offset));
            }
        }
        return unwrap(decodedStream);
    }

    private static byte[] compress(byte[] input) {
        ArrayList<Byte> output = new ArrayList<>();
        ArrayList<Byte> uncompressables = new ArrayList<>();

        // Setting pointer to start of stream
        for (int position = 0; position < input.length;) {
            // Finding the longest common byte pattern for window and buffer
            int pointerLength = 0;
            int pointerOffset = 0;
            boolean matchFound = false;

            for (int peekLength = BUFFER_SIZE; peekLength > 0; peekLength--) {
                for (int offset = 0; offset <= WINDOW_SIZE - peekLength; offset++) {
                    int peekEnd = Math.min(position + peekLength, input.length);
                    byte[] peek = Arrays.copyOfRange(input, position, peekEnd);
                    int windowStart = Math.max(position - WINDOW_SIZE + offset, 0);
                    int windowEnd = Math.max(position - WINDOW_SIZE + offset + peekLength, 0);
                    byte[] window = Arrays.copyOfRange(input, windowStart, windowEnd);

                    // If a match is found, save the pointer
                    if (Arrays.equals(peek, window) && peek.length > 2) {
                        pointerOffset = position - windowStart;
                        pointerLength = peek.length;
                        position += peek.length;
                        matchFound = true;
                        break;
                    }
                }
                if (matchFound)
                    break;
            }

            boolean lastEntry = position >= input.length-1;
            boolean maxedOut = -uncompressables.size() == Byte.MIN_VALUE;

            if (matchFound) {
                // Transfer uncompressables
                if (uncompressables.size() > 0) {
                    transferUncompressables(output, uncompressables);
                }

                // Add matched sequence
                output.add((byte) pointerOffset);
                output.add((byte) pointerLength);
                continue;
            }

            // If no match
            uncompressables.add(input[position]);
            position++;

            if (lastEntry || maxedOut) {
                transferUncompressables(output, uncompressables);
            }
        }
        return unwrap(output);
    }

    private static void transferUncompressables(ArrayList<Byte> output, ArrayList<Byte> uncompressables) {
        output.add((byte) -uncompressables.size());
        output.addAll(uncompressables);
        uncompressables.clear();
    }

    private static byte[] unwrap(ArrayList<Byte> bigBytes) {
        byte[] bytes = new byte[bigBytes.size()];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = bigBytes.get(i);
        }
        return bytes;
    }
}

