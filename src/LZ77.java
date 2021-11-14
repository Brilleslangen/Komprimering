import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class LZ77 {
    private static final int WINDOW_SIZE = Byte.MAX_VALUE, BUFFER_SIZE = Byte.MAX_VALUE;
    private static final String testWord = "a aasqzzz ax xzywplm";
    private static final byte[] bytes = testWord.getBytes();

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


    public static byte[] decompress(byte[] encodedStream) {
        ArrayList<Byte> decodedStream = new ArrayList<>();

        for (int position = 0; position < encodedStream.length; position++) {
            // if uncompressed
            if (encodedStream[position] < 0) {
                byte firstByte = encodedStream[position];
                position++;
                byte secondByte = encodedStream[position];
                int sequenceSize = Math.abs((short) (((firstByte & 0xFF) << 8) | (secondByte & 0xFF)));
                for (int i = 0; i < sequenceSize; i++) {
                    position++;
                    decodedStream.add(encodedStream[position]);
                }
                continue;
            }

            // if sequence reference
            int offset = encodedStream[position];
            int length = encodedStream[position + 1];

            for (int l = 0; l < length; l++) {
                if (decodedStream.size() - offset == -82) {
                    System.out.println("Stream size: " + decodedStream.size() + " - " + "Offset: " + offset);
                }
                decodedStream.add(decodedStream.get(decodedStream.size() - offset));
            }
        }
        return unwrap(decodedStream);
    }

    public static byte[] compress(byte[] input) {
        ArrayList<Byte> output = new ArrayList<>();
        ArrayList<Byte> uncompressables = new ArrayList<>();

        // Setting pointer to start of stream
        for (int position = 0; position < input.length;) {
            // Finding the longest common byte pattern for window and buffer
            int pointerLength = 0;
            int pointerOffset = 0;
            boolean matchFound = false;

            for (int peekLength = BUFFER_SIZE; peekLength > 2; peekLength--) {
                for (int offset = 0; offset <= WINDOW_SIZE - peekLength; offset++) {
                    int peekEnd = Math.min(position + peekLength, input.length);
                    byte[] peek = Arrays.copyOfRange(input, position, peekEnd);
                    int windowStart = Math.max(position - WINDOW_SIZE + offset, 0);
                    int windowEnd = Math.max(position - WINDOW_SIZE + offset + peekLength, 0);
                    byte[] window = Arrays.copyOfRange(input, windowStart, windowEnd);

                    if (peek.length < 3) {
                        break;
                    }

                    // If a match is found, save the pointer
                    if (Arrays.equals(peek, window)) {
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
            boolean maxedOut = -uncompressables.size() -1  == Short.MIN_VALUE;

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
        short sequenceSize = (short) -uncompressables.size();
        byte firstByte = (byte) (sequenceSize >> 8);
        byte secondByte = (byte) sequenceSize;
        output.add(firstByte);
        output.add(secondByte);
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

