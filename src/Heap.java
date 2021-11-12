import java.math.BigInteger;
import java.util.*;

public class Heap {
    private static byte[] bytes = "Tror denne shiten fungererer.".getBytes();
    private static long[][] codes = new long[256][2];
    private static int[] frequencies = new int[256];
    private static final short STOP = 0b100000000;

    public static void main(String[] args) {
        // Construct frequencies and heap
        constructFrequencyArr(bytes);
        Node root = constructHeap(frequencies);

        // compress and decompress
        byte[] compressed = compress(bytes, root);
        byte[] decompressed = decompress(compressed, root);

        printCodes();
        for (byte b : decompressed) {
            System.out.print((char)b);
        }
    }

    public static byte[] compress(byte[] bytes, Node root) {
        Bitstream bitstream = new Bitstream();
        root.generateCode("");
        for (byte b : bytes) {
            bitstream.add(codes[b & 0xFF][0], codes[b & 0xFF][1]);
        }
        return bitstream.toBytes();
    }

    public static byte[] decompress(byte[] bytes, Node root) {
        ArrayList<Byte> decompressed = new ArrayList<>();
        Node currNode = root;
        Bitstream bitstream = new Bitstream();
        for (int bit : bitstream.readBitStream(bytes)) {
            System.out.print(bit);
            if (currNode.isLeaf()) {
                if (currNode.charByte == STOP)
                    break;

                decompressed.add((byte) currNode.charByte);
                currNode = root;
            }

            // continue reading
            if (bit == 0) {
                currNode = currNode.right;
                continue;
            }
            currNode = currNode.left;
        }
        System.out.println();
        return unwrap(decompressed);
    }

    private static void printCodes() {
        for(int i = 0; i < codes.length; i++) {
            if (codes[i][1] != 0) {
                String binary = Long.toBinaryString(codes[i][0]);
                String additionalZeros = "";
                for (int zeros = 0; zeros < codes[i][1] - binary.length(); zeros++) {
                    additionalZeros += "0";
                }
                System.out.println((char) i + ": " + additionalZeros + binary );
            }
        }
    }

    private static byte[] removeDuplicates(byte[] bytes) {
        ArrayList<Byte> uniques = new ArrayList<>();
        for (byte b : bytes) {
            if (!uniques.contains(b)) {
                uniques.add(b);
            }
        }
        return unwrap(uniques);
    }

    public static Node constructHeap(int[] frequencies) {
        LinkedList<Node> nodes = constructNodeList(frequencies);
        Node thisNode;
        while (nodes.size() > 1 && (thisNode = nodes.pollLast()) != null) {
            Node nextNode = nodes.pollLast();
            Node combinedNode = new Node(thisNode, nextNode);

            // Place combined node correctly into list again
            boolean added = false;
            for (Node node : nodes) {
                if (combinedNode.frequency >= node.frequency) {
                    nodes.add(nodes.indexOf(node), combinedNode);
                    added = true;
                    break;
                }
            }
            if (!added) {
                nodes.addLast(combinedNode);
            }
        }
        return nodes.pollLast();
    }

    private static LinkedList<Node> constructNodeList(byte[] bytes) {
        LinkedList<Node> nodes = new LinkedList<>();
        for (byte b : bytes) {
            boolean nodeExists = false;
            for (Node node : nodes) {
                if (node.charByte == b) {
                    node.frequency++;
                    nodeExists = true;
                    break;
                }
            }
            if (nodeExists)
                continue;
            nodes.add(new Node(b, 1));
        }
        nodes.sort(Comparator.comparingInt(a -> a.frequency));
        return nodes;
    }

    private static LinkedList<Node> constructNodeList(int[] frequencies) {
        LinkedList<Node> nodes = new LinkedList<>();
        for (int i = 0; i < frequencies.length; i++) {
            byte b = (byte) i;
            boolean nodeExists = false;
            if (frequencies[i] <= 0)
                continue;

            for (Node node : nodes) {
                if (node.charByte == b) {
                    node.frequency++;
                    nodeExists = true;
                    break;
                }
            }
            if (!nodeExists)
                nodes.add(new Node(b, 1));
        }
        nodes.sort(Comparator.comparingInt(a -> a.frequency));
        return nodes;
    }

    private static void constructFrequencyArr(byte[] bytes) {
        frequencies = new int[256];
        for (byte b : bytes) {
            frequencies[b & 0xFF]++;
        }
    }

    private static class Node {
        private short charByte;
        private int frequency;
        private Node left;
        private Node right;

        public Node(short charByte, int frequency) {
            this.charByte = charByte;
            this.frequency = frequency;
        }

        public Node(Node left, Node right) {
            this.left = left;
            this.right = right;
            this.frequency = left.frequency + right.frequency;
        }

        public void generateCode(String currCode) {
            if (isLeaf()) {
                codes[charByte & 0xFF][0] = new BigInteger(currCode, 2).longValue();
                codes[charByte & 0xFF][1] = currCode.length();
                return;
            }
            right.generateCode(currCode + "0");
            left.generateCode(currCode + "1");
        }

        private boolean isLeaf() {
            return left == null && right == null;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Node node = (Node) o;
            return charByte == node.charByte;
        }

        @Override
        public String toString() {
            return "Byte: " + charByte + " - Frequency: " + frequency;
        }
    }

    private static byte[] unwrap(ArrayList<Byte> bigBytes) {
        byte[] bytes = new byte[bigBytes.size()];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = bigBytes.get(i);
        }
        return bytes;
    }
}
