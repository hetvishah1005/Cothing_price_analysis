package org.clothing;

import org.clothing.scraper.StoreDataInFile;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

class AVLNode {
    String word;
    Set<String> cellLocations;
    Set<Integer> lineNumbers;

    int height;
    AVLNode left, right;

    AVLNode(String word, String cellLocation, int lineNumber) {
        this.word = word;
        this.cellLocations = new HashSet<>();
        this.cellLocations.add(cellLocation);
        this.lineNumbers = new HashSet<>();
        this.lineNumbers.add(lineNumber);
        this.height = 1;
    }
}

class AVLTree {
    AVLNode root;

    int height(AVLNode node) {
        if (node == null) return 0;
        return node.height;
    }

    int balanceFactor(AVLNode node) {
        if (node == null) return 0;
        return height(node.left) - height(node.right);
    }

    AVLNode rotateRight(AVLNode y) {
        AVLNode x = y.left;
        AVLNode T2 = x.right;

        x.right = y;
        y.left = T2;

        y.height = Math.max(height(y.left), height(y.right)) + 1;
        x.height = Math.max(height(x.left), height(x.right)) + 1;

        return x;
    }

    AVLNode rotateLeft(AVLNode x) {
        AVLNode y = x.right;
        AVLNode T2 = y.left;

        y.left = x;
        x.right = T2;

        x.height = Math.max(height(x.left), height(x.right)) + 1;
        y.height = Math.max(height(y.left), height(y.right)) + 1;

        return y;
    }

    AVLNode insert(AVLNode node, String word, String cellLocation, int lineNumber) {
        if (node == null) return new AVLNode(word, cellLocation, lineNumber);

        if (word.compareTo(node.word) < 0) {
            node.left = insert(node.left, word, cellLocation, lineNumber);
        } else if (word.compareTo(node.word) > 0) {
            node.right = insert(node.right, word, cellLocation, lineNumber);
        } else {
            node.cellLocations.add(cellLocation);
            node.lineNumbers.add(lineNumber); // Store line number
            return node;
        }

        node.height = 1 + Math.max(height(node.left), height(node.right));

        int balance = balanceFactor(node);

        if (balance > 1 && word.compareTo(node.left.word) < 0) {
            return rotateRight(node);
        }

        if (balance < -1 && word.compareTo(node.right.word) > 0) {
            return rotateLeft(node);
        }

        if (balance > 1 && word.compareTo(node.left.word) > 0) {
            node.left = rotateLeft(node.left);
            return rotateRight(node);
        }

        if (balance < -1 && word.compareTo(node.right.word) < 0) {
            node.right = rotateRight(node.right);
            return rotateLeft(node);
        }

        return node;
    }

    void inorder(AVLNode node) {
        if (node != null) {
            inorder(node.left);
            System.out.println(node.word + ": " + node.cellLocations);
            inorder(node.right);
        }
    }
}

public class InvertedIndexing {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        findInvertedIndexing(scanner);
        scanner.close();
    }

    public static void findInvertedIndexing(Scanner scanner) {
        // Prompt the user to enter a word to locate in the CSV files
        System.out.println("Enter a word to locate in the CSV files (Inverted Indexing):");
        String searchWord = scanner.nextLine().trim().toLowerCase();

        // Call inverted indexing method
        handleInvertedIndexing(searchWord);
    }

    public static void handleInvertedIndexing(String searchWord) {
        String[] csvFiles = {
                "Ajio.csv", "Flipkart.csv", "Myntra.csv"
        };
        for (String fileName : csvFiles) {
            String filePath = StoreDataInFile.getFilePath(fileName);
            AVLTree avlTree = buildInvertedIndex(filePath);

            AVLNode node = search(avlTree.root, searchWord);
            System.out.println("-------------------------------------------------\n" +
                    fileName + ": Searching \"" + searchWord + "\"\n" +
                    "-------------------------------------------------");
            if (node != null) {
                try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
                    String line;
                    int lineNumber = 1;
                    while ((line = reader.readLine()) != null) {
                        if (node.lineNumbers.contains(lineNumber)) {
                            System.out.println("Line " + lineNumber + ": " + line);
                        }
                        lineNumber++;
                    }
                    System.out.println();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                System.out.println("Word '" + searchWord + "' not found in the " + fileName + " file\n");
            }
        }
    }

    public static AVLTree buildInvertedIndex(String filePath) {
        AVLTree avlTree = new AVLTree();
        int lineNumber = 1;

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] cells = line.split(","); // Assuming CSV uses comma as delimiter, adjust if needed
                for (String cellContent : cells) {
                    if (cellContent != null && !cellContent.isEmpty()) {
                        String[] words = cellContent.split("\\s+");
                        String cellLocation = Arrays.toString(cells); // You might need to adjust this based on how you want to get cell location
                        for (String word : words) {
                            word = word.toLowerCase();
                            avlTree.root = avlTree.insert(avlTree.root, word, cellLocation, lineNumber);
                        }
                    }
                }
                lineNumber++; // Increment line number after reading each line
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return avlTree;
    }

    static AVLNode search(AVLNode node, String word) {
        if (node == null || node.word.equals(word)) {
            return node;
        }

        if (word.compareTo(node.word) < 0) {
            return search(node.left, word);
        } else {
            return search(node.right, word);
        }
    }
}
