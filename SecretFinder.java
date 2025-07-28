import java.io.*;
import java.math.BigInteger;
import java.util.*;
import java.util.regex.*;

public class SecretFinder {

    static class Point {
        int x;
        BigInteger y;

        Point(int x, BigInteger y) {
            this.x = x;
            this.y = y;
        }
    }

    // Decode value from any base
    private static BigInteger decode(String value, int base) {
        return new BigInteger(value, base);
    }

    // Lagrange Interpolation to find f(0) using K points
    private static BigInteger lagrangeInterpolationAtZero(List<Point> points) {
        BigInteger result = BigInteger.ZERO;
        int k = points.size();

        for (int i = 0; i < k; i++) {
            BigInteger xi = BigInteger.valueOf(points.get(i).x);
            BigInteger yi = points.get(i).y;

            BigInteger numerator = BigInteger.ONE;
            BigInteger denominator = BigInteger.ONE;

            for (int j = 0; j < k; j++) {
                if (i != j) {
                    BigInteger xj = BigInteger.valueOf(points.get(j).x);
                    numerator = numerator.multiply(xj.negate()); // (0 - xj)
                    denominator = denominator.multiply(xi.subtract(xj));
                }
            }

            BigInteger term = yi.multiply(numerator).divide(denominator);
            result = result.add(term);
        }

        return result;
    }

    // Manual JSON parsing (no external library)
    private static List<Point> parseJsonFile(String filePath) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(filePath));
        StringBuilder jsonBuilder = new StringBuilder();
        String line;

        while ((line = br.readLine()) != null) {
            jsonBuilder.append(line.trim());
        }
        br.close();

        String json = jsonBuilder.toString();

        // Extract "n" and "k" using regex
        int n = -1, k = -1;
        Pattern nPattern = Pattern.compile("\"n\"\\s*:\\s*(\\d+)");
        Pattern kPattern = Pattern.compile("\"k\"\\s*:\\s*(\\d+)");

        Matcher nMatcher = nPattern.matcher(json);
        Matcher kMatcher = kPattern.matcher(json);

        if (nMatcher.find()) {
            n = Integer.parseInt(nMatcher.group(1));
        }
        if (kMatcher.find()) {
            k = Integer.parseInt(kMatcher.group(1));
        }

        if (n == -1 || k == -1) {
            throw new RuntimeException("Failed to extract 'n' or 'k' from JSON.");
        }

        List<Point> allPoints = new ArrayList<>();

        // Extract all key:value pairs like "1": {"base": "10", "value": "4"}
        Pattern pointPattern = Pattern.compile("\"(\\d+)\"\\s*:\\s*\\{[^}]*\"base\"\\s*:\\s*\"(\\d+)\"[^}]*\"value\"\\s*:\\s*\"(\\w+)\"");
        Matcher pointMatcher = pointPattern.matcher(json);

        while (pointMatcher.find()) {
            try {
                int x = Integer.parseInt(pointMatcher.group(1));
                int base = Integer.parseInt(pointMatcher.group(2));
                String value = pointMatcher.group(3);
                BigInteger y = decode(value, base);
                allPoints.add(new Point(x, y));
            } catch (Exception e) {
                System.err.println("Skipping invalid point: " + e.getMessage());
            }
        }

        if (allPoints.size() < k) {
            System.err.println("Warning: Not enough valid points found. Needed " + k + ", found " + allPoints.size());
        }

        allPoints.sort(Comparator.comparingInt(p -> p.x));
        return allPoints.subList(0, Math.min(k, allPoints.size()));
    }

    public static void main(String[] args) throws IOException {
        String file1 = "testcase1.json";
        String file2 = "testcase2.json";

        List<Point> points1 = parseJsonFile(file1);
        List<Point> points2 = parseJsonFile(file2);

        System.out.println("Decoded Points from testcase1:");
        for (Point p : points1) {
            System.out.println("x = " + p.x + ", y = " + p.y);
        }
        System.out.println();

        System.out.println("Decoded Points from testcase2:");
        for (Point p : points2) {
            System.out.println("x = " + p.x + ", y = " + p.y);
        }
        System.out.println();

        BigInteger secret1 = lagrangeInterpolationAtZero(points1);
        BigInteger secret2 = lagrangeInterpolationAtZero(points2);

        System.out.println("Secret from testcase1: " + secret1);
        System.out.println("Secret from testcase2: " + secret2);
    }
}
