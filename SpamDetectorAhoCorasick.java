import java.util.*;

public class SpamDetectorAhoCorasick {
    static class Node {
        // Stores the immediate children of the node (key: character, value: child node)
        Map<Character, Node> children = new HashMap<>();
        // The failure link of the node
        Node fail;

        List<String> outputs = new ArrayList<>();
    }

    private Node root;

    public SpamDetectorAhoCorasick(List<String> keywords) {
        root = new Node();
        buildTrie(keywords);
        buildFailureLinks();
    }

    private void buildTrie(List<String> keywords) {
        for (String keyword : keywords) {
            Node node = root;
            for (char c : keyword.toCharArray()) {
                node = node.children.computeIfAbsent(c, k -> new Node());
            }
            node.outputs.add(keyword);
        }
    }

    private void buildFailureLinks() {
        Queue<Node> queue = new LinkedList<>();
        root.fail = root;

        // Initialize fail links of root's children to root
        for (Node child : root.children.values()) {
            child.fail = root;
            queue.add(child);
        }

        while (!queue.isEmpty()) {
            Node current = queue.poll();

            for (Map.Entry<Character, Node> entry : current.children.entrySet()) {
                char c = entry.getKey();
                Node child = entry.getValue();

                Node failNode = current.fail;
                while (failNode != root && !failNode.children.containsKey(c)) {
                    failNode = failNode.fail;
                }
                child.fail = failNode.children.getOrDefault(c, root);
                child.outputs.addAll(child.fail.outputs);

                queue.add(child);
            }
        }
    }

    public List<String> search(String text) {
        List<String> results = new ArrayList<>();
        Node node = root;

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);

            while (node != root && !node.children.containsKey(c)) {
                node = node.fail;
            }

            node = node.children.getOrDefault(c, root);

            if (!node.outputs.isEmpty()) {
                results.addAll(node.outputs);
            }
        }

        return results;
    }

    // Test example
    public static void main(String[] args) {
        List<String> spamKeywords = Arrays.asList("free", "winner", "prize", "claim", "urgent", "offer");
        SpamDetectorAhoCorasick aho = new SpamDetectorAhoCorasick(spamKeywords);

        String testSms = "Congratulations! You are a winner! Claim your free prize now!";
        List<String> found = aho.search(testSms.toLowerCase());

        System.out.println("Detected spam keywords:");
        for (String keyword : found) {
            System.out.println(keyword);
        }
    }
}
