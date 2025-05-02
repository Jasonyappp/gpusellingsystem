package gpusellingsystem;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ProductManager {
    private Map<Integer, Product> products = new HashMap<>();
    private int nextProductId = 1;
    private static final String FILE_PATH = "inventory_data/inventory.txt";

    public ProductManager() {
        loadFromFile();
    }

    public boolean addProduct(String name, double price, int quantity, String detail, String type, int spec) {
        if (name == null || name.trim().isEmpty() || price < 0 || quantity < 0 || detail == null || detail.trim().isEmpty() || spec < 1) {
            return false;
        }
        if (productNameExistsForType(name, type)) {
            return false;
        }
        int newProductId = nextProductId;
        while (products.containsKey(newProductId)) {
            newProductId++;
        }
        Product product;
        if ("GPU".equalsIgnoreCase(type)) {
            product = new GPU(newProductId, name, price, quantity, detail, spec);
        } else if ("CPU".equalsIgnoreCase(type)) {
            product = new CPU(newProductId, name, price, quantity, detail, spec);
        } else {
            return false;
        }
        products.put(newProductId, product);
        nextProductId = newProductId + 1;
        saveToFile();
        return true;
    }

    private boolean productNameExistsForType(String name, String type) {
        for (Product product : products.values()) {
            if (product.getName().equalsIgnoreCase(name) && 
                product.getClass().getSimpleName().equalsIgnoreCase(type)) {
                return true;
            }
        }
        return false;
    }

    public boolean removeProduct(int productId) {
        boolean removed = products.remove(productId) != null;
        if (removed) {
            saveToFile();
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
        saveToFile();
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
    
    public Product searchProductById(int productId) {
        return products.get(productId);
    }

    public Map<Integer, Product> getProducts() {
        return products;
    }

    private void loadFromFile() {
        products.clear();
        File file = new File(FILE_PATH);
        if (!file.exists()) {
            return;
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length != 7) {
                    continue;
                }
                try {
                    int productId = Integer.parseInt(parts[0]);
                    String name = parts[1];
                    double price = Double.parseDouble(parts[2]);
                    int quantity = Integer.parseInt(parts[3]);
                    String detail = parts[4];
                    String type = parts[5];
                    int spec = Integer.parseInt(parts[6]);
                    Product product;
                    if ("GPU".equalsIgnoreCase(type)) {
                        product = new GPU(productId, name, price, quantity, detail, spec);
                    } else if ("CPU".equalsIgnoreCase(type)) {
                        product = new CPU(productId, name, price, quantity, detail, spec);
                    } else {
                        continue;
                    }
                    products.put(productId, product);
                    if (productId >= nextProductId) {
                        nextProductId = productId + 1;
                    }
                } catch (NumberFormatException e) {
                    continue;
                }
            }
        } catch (IOException e) {
            System.err.println("Failed to load products from file: " + e.getMessage());
        }
    }

    private void saveToFile() {
        try {
            File dir = new File("inventory_data");
            if (!dir.exists()) {
                if (!dir.mkdirs()) {
                    System.err.println("Failed to create directory: " + dir.getAbsolutePath());
                    return;
                }
            }

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH))) {
                for (Product p : products.values()) {
                    String spec = p instanceof GPU ? String.valueOf(((GPU) p).getVRAM()) : String.valueOf(((CPU) p).getCoreCount());
                    writer.write(String.format("%d,%s,%.2f,%d,%s,%s,%s",
                            p.getProductId(), p.getName(), p.getPrice(), p.getQuantity(), p.getDetail(), 
                            p.getClass().getSimpleName(), spec));
                    writer.newLine();
                }
            }
        } catch (IOException e) {
            System.err.println("Failed to save products to file: " + e.getMessage());
        }
    }
}