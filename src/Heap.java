import java.util.Comparator;
import java.util.LinkedList;

public class Heap {
    private static final byte[] testBytes = "wdevowuhøvbaqvj".getBytes();

    public static void main(String[] args) {
        LinkedList<Node> nodes = constructNodeList(testBytes);
        System.out.println((int) nodes.stream().map(n -> n.frequency).reduce(0, Integer::sum));

        Node root = constructHeap(testBytes);
        System.out.println(root);
    }

    private static Node constructHeap(byte[] bytes) {
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

        @Override
        public String toString() {
            return "Byte: " + charByte + " - Frequency: " + frequency;
        }
    }
}
