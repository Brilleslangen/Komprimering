import java.math.BigInteger;
import java.util.*;

public class Heap {
    private static byte[] bytes = "hahahaha".getBytes();
    private static long[][] codes = new long[256][2];
    private static Node root;

    public static void main(String[] args) {
        System.out.println(removeDuplicates(bytes).length);
        root = constructHeap(bytes);
        byte[] compressed = compress(bytes, root);
        System.out.println("done compressing");
        byte[] decompressed = decompress(compressed, root);

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
            if (currNode.isLeaf()) {
                decompressed.add(currNode.charByte);
                currNode = root;
            }

            // continue reading
            if (bit == 0) {
                currNode = currNode.right;
                continue;
            }
            currNode = root.left;
        }
        return unwrap(decompressed);
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

    public static Node constructHeap(byte[] bytes) {
        LinkedList<Node> nodes = constructNodeList(bytes);
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

    private static class Node {
        private byte charByte;
        private int frequency;
        private Node left;
        private Node right;

        public Node(byte charByte, int frequency) {
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
