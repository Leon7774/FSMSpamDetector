import java.util.*;

public class SpamDetectorAhoCorasick {
    static class Node {
        Map<Character, Node> children = new HashMap<>();
        Node fail;
        List<String> outputs = new ArrayList<>();
    }

    private Node root;

    private static final Map<Character, char[]> substitutions = new HashMap<>() {{
        put('a', new char[]{'a', '@', '4'});
        put('e', new char[]{'e', '3'});
        put('i', new char[]{'i', '1', '!'});
        put('o', new char[]{'o', '0'});
        put('s', new char[]{'s', '$', '5'});
        put('l', new char[]{'l', '1', '|'});
        put('t', new char[]{'t', '7'});
        put('n', new char[]{'n'});
        put('w', new char[]{'w'});
        put('r', new char[]{'r'});
    }};

    public SpamDetectorAhoCorasick(List<String> keywords) {
        root = new Node();

        Set<String> allVariants = new HashSet<>();
        for (String keyword : keywords) {
            allVariants.addAll(generateVariants(keyword.toLowerCase()));
        }

        buildTrie(new ArrayList<>(allVariants));
        buildFailureLinks();
    }

    private Set<String> generateVariants(String word) {
        Set<String> variants = new HashSet<>();
        backtrackGenerate(word.toCharArray(), 0, new char[word.length()], variants);
        return variants;
    }

    private void backtrackGenerate(char[] word, int index, char[] current, Set<String> variants) {
        if (index == word.length) {
            variants.add(new String(current));
            return;
        }

        char c = word[index];
        char[] subs = substitutions.getOrDefault(c, new char[]{c});
        for (char sub : subs) {
            current[index] = sub;
            backtrackGenerate(word, index + 1, current, variants);
        }
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

    // Returns spam weight and matched words as a Map<String, Integer> (word -> count)
    public Map<String, Integer> searchWithMatches(String text) {
        Map<String, Integer> matches = new HashMap<>();
        Node node = root;
        text = text.toLowerCase();

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);

            while (node != root && !node.children.containsKey(c)) {
                node = node.fail;
            }

            node = node.children.getOrDefault(c, root);

            for (String output : node.outputs) {
                matches.put(output, matches.getOrDefault(output, 0) + 1);
            }
        }

        return matches;
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        List<String> spamKeywords = Arrays.asList("bonus", "get", "free", "100", "win", "deposit", "claim", "real", "cash", "account", "makukuha", "join", "gcash", "daily", "iyong", "new", "libreng", "please", "deposito", "libre", "manalo", "winner");

        SpamDetectorAhoCorasick detector = new SpamDetectorAhoCorasick(spamKeywords);

        while(true) {
            System.out.println("Enter a message to check for spam:");
            String input = scanner.nextLine();

            if(input.equals("q") || input.equals("quit") || input.equals("exit")) {
                break;
            }

            Map<String, Integer> results = detector.searchWithMatches(input.toLowerCase());

            int spamWeight = results.values().stream().mapToInt(Integer::intValue).sum();

            System.out.println("\nSpam weight: " + spamWeight);
            System.out.println("Matched words and counts:");
            results.forEach((word, count) -> System.out.println("  " + word + ": " + count));

            String spamLevel;
            if (spamWeight == 0) {
                spamLevel = "Not spam";
            } else if (spamWeight <= 2) {
                spamLevel = "Likely spam";
            } else if (spamWeight <= 4) {
                spamLevel = "Most likely spam";
            } else {
                spamLevel = "Definite spam";
            }
            System.out.println("Spam Level: " + spamLevel + "\n");

        }

    }
}
