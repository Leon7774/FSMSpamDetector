import java.util.*;
import java.util.regex.Pattern;

/**
 * A Spam Detector using Finite State Machine (FSM) theory
 * This program analyzes text input to detect common spam patterns based on:
 * 1. Excessive special characters
 * 2. Suspicious phrases often found in spam
 * 3. Excessive capitalization
 * 4. URL patterns
 * 5. Dollar amount patterns
 */
public class SpamDetectorFSM {

    // Enum for possible states in the FSM
    private enum State {
        NORMAL,            // Normal content
        POTENTIAL_SPAM,    // Some spam indicators detected
        LIKELY_SPAM,       // Multiple spam indicators
        CONFIRMED_SPAM     // Definite spam
    }

    // Store current state
    private State currentState;

    // Counters for different spam indicators
    private int specialCharCount;
    private int capsCount;
    private int spamPhraseCount;
    private int urlCount;
    private int dollarPatternCount;

    // Patterns for spam detection
    private final List<String> spamPhrases = Arrays.asList(
            "free money", "make money fast", "get rich", "buy now", "limited time",
            "act now", "click here", "cash bonus", "no credit check", "100% free",
            "unlimited income", "discount", "guaranteed", "no risk", "winner"
    );

    private final Pattern urlPattern = Pattern.compile(
            "https?://\\S+|www\\.\\S+|\\S+\\.(?:com|net|org|biz|info)\\S*"
    );

    private final Pattern dollarPattern = Pattern.compile(
            "\\$\\d+(?:\\.\\d{2})?|\\d+\\s*dollars"
    );

    // Thresholds for state transitions
    private static final int POTENTIAL_SPAM_THRESHOLD = 3;
    private static final int LIKELY_SPAM_THRESHOLD = 5;
    private static final int CONFIRMED_SPAM_THRESHOLD = 8;

    public SpamDetectorFSM() {
        resetFSM();
    }

    /**
     * Reset the FSM to initial state
     */
    public void resetFSM() {
        currentState = State.NORMAL;
        specialCharCount = 0;
        capsCount = 0;
        spamPhraseCount = 0;
        urlCount = 0;
        dollarPatternCount = 0;
    }

    /**
     * Process input text and update FSM state
     *
     * @param input Text to analyze
     * @return true if the text is considered spam, false otherwise
     */
    public boolean processInput(String input) {
        resetFSM();

        if (input == null || input.trim().isEmpty()) {
            return false;
        }

        // Convert to lowercase for case-insensitive matching
        String lowerInput = input.toLowerCase();

        // Count special characters
        specialCharCount = countSpecialChars(input);

        // Count capitalized words
        capsCount = countCapitalizedWords(input);

        // Count spam phrases
        spamPhraseCount = countSpamPhrases(lowerInput);

        // Count URLs
        urlCount = countMatches(urlPattern, input);

        // Count dollar patterns
        dollarPatternCount = countMatches(dollarPattern, input);

        // Calculate total spam score
        int spamScore = calculateSpamScore();

        // Update FSM state based on spam score
        updateState(spamScore);

        // Return true if the state is LIKELY_SPAM or CONFIRMED_SPAM
        return currentState == State.LIKELY_SPAM || currentState == State.CONFIRMED_SPAM;
    }

    /**
     * Count special characters in the input
     */
    private int countSpecialChars(String input) {
        int count = 0;
        for (char c : input.toCharArray()) {
            if (!Character.isLetterOrDigit(c) && !Character.isWhitespace(c)) {
                count++;
            }
        }
        return count;
    }

    /**
     * Count capitalized words in the input
     */
    private int countCapitalizedWords(String input) {
        int count = 0;
        String[] words = input.split("\\s+");

        for (String word : words) {
            if (word.length() > 0 && Character.isUpperCase(word.charAt(0))) {
                count++;
            }

            // Check for ALLCAPS words (length > 3)
            if (word.length() > 3 && word.equals(word.toUpperCase()) &&
                    !word.equals(word.toLowerCase())) {
                count += 2; // ALLCAPS words count double
            }
        }

        return count;
    }

    /**
     * Count occurrences of spam phrases in the input
     */
    private int countSpamPhrases(String input) {
        int count = 0;
        for (String phrase : spamPhrases) {
            if (input.contains(phrase)) {
                count++;
            }
        }
        return count;
    }

    /**
     * Count matches of a pattern in the input
     */
    private int countMatches(Pattern pattern, String input) {
        java.util.regex.Matcher matcher = pattern.matcher(input);
        int count = 0;
        while (matcher.find()) {
            count++;
        }
        return count;
    }

    /**
     * Calculate the total spam score based on all indicators
     */
    private int calculateSpamScore() {
        int score = 0;

        // Special characters
        if (specialCharCount > 5) {
            score += Math.min(specialCharCount / 2, 5);
        }

        // Capitalized words
        if (capsCount > 3) {
            score += Math.min(capsCount / 2, 5);
        }

        // Spam phrases
        score += spamPhraseCount * 2;

        // URLs
        score += urlCount * 2;

        // Dollar patterns
        score += dollarPatternCount * 2;

        return score;
    }

    /**
     * Update the FSM state based on the spam score
     */
    private void updateState(int spamScore) {
        if (spamScore >= CONFIRMED_SPAM_THRESHOLD) {
            currentState = State.CONFIRMED_SPAM;
        } else if (spamScore >= LIKELY_SPAM_THRESHOLD) {
            currentState = State.LIKELY_SPAM;
        } else if (spamScore >= POTENTIAL_SPAM_THRESHOLD) {
            currentState = State.POTENTIAL_SPAM;
        } else {
            currentState = State.NORMAL;
        }
    }

    /**
     * Get the current state of the FSM
     */
    public State getCurrentState() {
        return currentState;
    }

    /**
     * Get detailed spam analysis
     */
    public String getAnalysisDetails() {
        StringBuilder analysis = new StringBuilder();
        analysis.append("Spam Analysis:\n");
        analysis.append("- Special Characters: ").append(specialCharCount).append("\n");
        analysis.append("- Capitalized/ALL CAPS Words: ").append(capsCount).append("\n");
        analysis.append("- Spam Phrases Detected: ").append(spamPhraseCount).append("\n");
        analysis.append("- URLs Detected: ").append(urlCount).append("\n");
        analysis.append("- Dollar Patterns: ").append(dollarPatternCount).append("\n");
        analysis.append("- Current State: ").append(currentState).append("\n");
        analysis.append("- Verdict: ").append(isSpam() ? "SPAM" : "NOT SPAM").append("\n");

        return analysis.toString();
    }

    /**
     * Check if the input is considered spam
     */
    public boolean isSpam() {
        return currentState == State.LIKELY_SPAM || currentState == State.CONFIRMED_SPAM;
    }

    /**
     * Main method for demonstration
     */
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        SpamDetectorFSM detector = new SpamDetectorFSM();

        System.out.println("Spam Detector FSM");
        System.out.println("Enter text to analyze (type 'exit' to quit):");

        while (true) {
            System.out.print("> ");
            String input = scanner.nextLine();

            if (input.equalsIgnoreCase("exit")) {
                break;
            }

            detector.processInput(input);
            System.out.println(detector.getAnalysisDetails());
        }

        scanner.close();
        System.out.println("Goodbye!");
    }
}