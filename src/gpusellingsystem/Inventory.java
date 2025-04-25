package gpusellingsystem;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Inventory {
    private Map<Integer, Product> products = new HashMap<>();
    private int nextProductId = 1;
    private static final String FILE_PATH = "inventory_data/inventory.txt";

    public Inventory() {
        loadFromFile(); // Load products from file on initialization
        // If the file was empty or didn't exist, initialize with default products
        if (products.isEmpty()) {
            products.put(1, new GPU(1, "RTX1060", 1000.0, 10, "High-performance GPU"));
            products.put(2, new GPU(2, "RTX2050", 20.0, 50, "Entry-level GPU"));
            nextProductId = 3; // Update nextProductId after adding default products
            saveToFile(); // Save the default products to the file
        }
    }

    public boolean addProduct(String name, double price, int quantity, String detail, String type) {
        if (name == null || name.trim().isEmpty() || price < 0 || quantity < 0 || detail == null || detail.trim().isEmpty()) {
            return false;
        }
        int newProductId = nextProductId;
        while (products.containsKey(newProductId)) {
            newProductId++;
        }
        Product product;
        if ("GPU".equalsIgnoreCase(type)) {
            product = new GPU(newProductId, name, price, quantity, detail);
        } else if ("CPU".equalsIgnoreCase(type)) {
            product = new CPU(newProductId, name, price, quantity, detail);
        } else {
            return false; // Invalid type
        }
        products.put(newProductId, product);
        nextProductId = newProductId + 1;
        saveToFile(); // Save to file after adding a product
        return true;
    }

    public boolean removeProduct(int productId) {
        boolean removed = products.remove(productId) != null;
        if (removed) {
            saveToFile(); // Save to file after removing a product
        }
        return removed;
    }

    public boolean updateProduct(int productId, String name, double price, int quantity, String detail) {
        Product product = products.get(productId);
        if (product == null) {
            return false;
        }
        if (name != null && !name.trim().isEmpty()) {
            product.setName(name);
        }
        if (price >= 0) {
            product.setPrice(price);
        }
        if (quantity >= 0) {
            product.setQuantity(quantity);
        }
        if (detail != null && !detail.trim().isEmpty()) {
            product.setDetail(detail);
        }
        saveToFile(); // Save to file after updating a product
        return true;
    }

    public Product searchProduct(String productName) {
        for (Product product : products.values()) {
            if (product.getName().equalsIgnoreCase(productName)) {
                return product;
            }
        }
        return null;
    }

    public Map<Integer, Product> getProducts() {
        return products;
    }

    private void loadFromFile() {
        products.clear(); // Clear current products to avoid duplicates
        File file = new File(FILE_PATH);
        if (!file.exists()) {
            return; // File doesn't exist, will initialize with default products
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length != 6) { // Now expecting 6 fields including type
                    continue; // Skip malformed lines
                }
                try {
                    int productId = Integer.parseInt(parts[0]);
                    String name = parts[1];
                    double price = Double.parseDouble(parts[2]);
                    int quantity = Integer.parseInt(parts[3]);
                    String detail = parts[4];
                    String type = parts[5];
                    Product product;
                    if ("GPU".equalsIgnoreCase(type)) {
                        product = new GPU(productId, name, price, quantity, detail);
                    } else if ("CPU".equalsIgnoreCase(type)) {
                        product = new CPU(productId, name, price, quantity, detail);
                    } else {
                        continue; // Skip invalid types
                    }
                    products.put(productId, product);
                    // Update nextProductId to be higher than the largest productId
                    if (productId >= nextProductId) {
                        nextProductId = productId + 1;
                    }
                } catch (NumberFormatException e) {
                    continue; // Skip lines with parsing errors
                }
            }
        } catch (IOException e) {
            // Silently handle IOException
        }
    }

    private void saveToFile() {
        try {
            // Create inventory_data folder if it doesn't exist
            File dir = new File("inventory_data");
            if (!dir.exists()) {
                if (!dir.mkdirs()) {
                    return; // Silently handle directory creation failure
                }
            }

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH))) {
                for (Product p : products.values()) {
                    writer.write(String.format("%d,%s,%.2f,%d,%s,%s",
                            p.getProductId(), p.getName(), p.getPrice(), p.getQuantity(), p.getDetail(), p.getClass().getSimpleName()));
                    writer.newLine();
                }
            }
        } catch (IOException e) {
            // Silently handle IOException
        }
    }
}