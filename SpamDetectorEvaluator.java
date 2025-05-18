import java.io.*;
import java.util.*;

public class SpamDetectorEvaluator {

    public static void main(String[] args) {
        // Replace with your spam keywords, same as in your SpamDetectorAhoCorasick
        List<String> spamKeywords = Arrays.asList(
            "bonus", "get", "free", "100", "win", "deposit", "claim",
            "real", "cash", "account", "makukuha", "join", "gcash",
            "daily", "iyong", "new", "libreng", "please", "deposito",
            "libre", "manalo", "winner", "play", "day", "visit", "sign", "ka", "pesos", "register", "enjoy", "welcome", "deposito", "libreng", "daily"
        );

        String csvPath;

        SpamDetectorAhoCorasick detector = new SpamDetectorAhoCorasick(spamKeywords);

        if (args.length < 1) {
            // Change this to the dataset you want to use
            // The dataset needs to have the following columns: (text, spam)
            csvPath = "accuracy-test.csv";
        }else{
            // If an argument exists, use this as the dataset
            csvPath = args[0];
        }

        String csvFile = csvPath;
        int total = 0;
        int correct = 0;

        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
            String line;
            // Read header
            line = br.readLine();
            if (line == null) {
                System.out.println("Empty CSV file");
                return;
            }

            while ((line = br.readLine()) != null) {
                // Assuming CSV format: text,is_spam
                // and that text can contain commas, so we split only on last comma
                int lastComma = line.lastIndexOf(',');
                if (lastComma == -1) continue;

                String text = line.substring(0, lastComma).trim();
                String label = line.substring(lastComma + 1).trim().toLowerCase();

                Map<String, Integer> matches = detector.searchWithMatches(text.toLowerCase());
                int spamWeight = matches.values().stream().mapToInt(Integer::intValue).sum();

                String predictedSpamLevel;
                if (spamWeight == 0) {
                    predictedSpamLevel = "not spam";
                } else if (spamWeight <= 2) {
                    predictedSpamLevel = "likely spam";
                } else if (spamWeight <= 4) {
                    predictedSpamLevel = "most likely spam";
                } else {
                    predictedSpamLevel = "definite spam";
                }

                // Simplify predicted label to binary spam or not spam for accuracy test
                boolean predictedSpam = spamWeight > 1;
                boolean actualSpam = label.equals("true") || label.equals("yes") || label.equals("1");

                if (predictedSpam == actualSpam) {
                    correct++;
                }
                total++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (total == 0) {
            System.out.println("No test data found.");
            return;
        }

        double accuracy = 100.0 * correct / total;
        System.out.printf("Tested %d samples\n", total);
        System.out.printf("Correct predictions: %d\n", correct);
        System.out.printf("Accuracy: %.2f%%\n", accuracy);
    }
}
