import java.io.*;
import java.math.BigInteger;
import java.util.*;

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

    // Improved manual JSON parsing for your format (without external libs)
    private static List<Point> parseJsonFile(String filePath) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(filePath));
        StringBuilder jsonBuilder = new StringBuilder();
        String line;

        while ((line = br.readLine()) != null) {
            jsonBuilder.append(line.trim());
        }

        String json = jsonBuilder.toString();

        // Extract n and k from keys block using regex
        int n = Integer.parseInt(json.replaceAll(".\"n\"\\s:\\s*(\\d+).*", "$1"));
        int k = Integer.parseInt(json.replaceAll(".\"k\"\\s:\\s*(\\d+).*", "$1"));

        List<Point> allPoints = new ArrayList<>();

        // Check keys from 0 up to 100 to find points
        for (int i = 0; i <= 100; i++) {
            String regexBase = "\"" + i + "\"\\s*:\\s*\\{[^}]\"base\"\\s:\\s*\"(\\d+)\"";
            String regexValue = "\"" + i + "\"\\s*:\\s*\\{[^}]\"value\"\\s:\\s*\"([^\"]+)\"";

            if (json.matches("." + regexBase + ".") && json.matches("." + regexValue + ".")) {
                try {
                    int base = Integer.parseInt(json.replaceAll("." + regexBase + ".", "$1"));
                    String value = json.replaceAll("." + regexValue + ".", "$1");

                    BigInteger y = new BigInteger(value, base);
                    allPoints.add(new Point(i, y));
                } catch (Exception e) {
                    System.err.println("Failed to parse value for key " + i + ": " + e.getMessage());
                }
            }
        }

        if (allPoints.size() < k) {
            System.err.println("Warning: Not enough valid points found. Needed " + k + ", found " + allPoints.size());
        }

        allPoints.sort(Comparator.comparingInt(p -> p.x));
        return allPoints.subList(0, Math.min(k, allPoints.size())); // safe sublist
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