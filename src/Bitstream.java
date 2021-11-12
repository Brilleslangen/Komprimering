import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Bitstream {
    private byte[] bitstream;
    private int remainingSpace = 8;
    private int capacity = 128;
    private int bytesUsed = 0;

    public Bitstream() {
        bitstream = new byte[capacity];
    }

    public void add(long charCode, long length) {
        // Increase list size
        if (bytesUsed >= capacity) {
            capacity += 128;
            bitstream = Arrays.copyOf(bitstream, capacity);
        }

        // adding
        boolean overflow = length > remainingSpace; // true
        int shift = (int) length - remainingSpace;
        int bitPlaced = Math.min((int) length, remainingSpace);

        if (shift < 0) {
            bitstream[bytesUsed] |= (byte) (charCode << Math.abs(shift)); // fills byte
        } else {
            bitstream[bytesUsed] |= (byte) (charCode >> shift);
        }

        if (remainingSpace <= length) {
            remainingSpace = 8;
            bytesUsed++;
        } else {
            remainingSpace -= length;
        }

        if (overflow) {
            add(charCode, length - bitPlaced); // 5 0b11111000
        }
    }

    // Check this method if information loss at end
    public byte[] toBytes() {
        int i = bitstream.length-1;
        int bytesToRemove = 0;
        while (bitstream[i] == 0) {
            bytesToRemove++;
            i--;
        }
        bitstream = Arrays.copyOf(bitstream, bitstream.length - bytesToRemove);
        return bitstream;
    }

    public List<Integer> readBitStream(byte[] bitstream) {
        List<Integer> codeStream = new ArrayList<>();

        for (byte b : bitstream) {
            for(int i = 0; i < 8; i++) {
                codeStream.add(readBit(b, i));
            }
        }
        // remove excessive bits
        int i;
        while ((i = codeStream.size()) > 0 && codeStream.get(i - 1) != 1) {
            codeStream = codeStream.subList(0, i - 1);
        }
        return codeStream;
    }

    public int readBit(byte b, int index) {
        index = 7 - index;
        return (b >> index) & 1;
    }
}
