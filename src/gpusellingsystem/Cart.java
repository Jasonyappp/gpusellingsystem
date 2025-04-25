package gpusellingsystem;

import java.util.ArrayList;
import java.util.List;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class Cart {
    private List<CartItem> items;
    private String username;

    public Cart(String username) {
        this.username = username;
        items = new ArrayList<>();
        loadFromFile();
    }

    public boolean addItem(Product product, int quantity, Inventory inventory) {
        loadFromFile(); // Ensure the latest cart data is loaded
        int currentQuantity = 0;
        for (CartItem item : items) {
            if (item.getProduct().getProductId() == product.getProductId()) {
                currentQuantity = item.getQuantity();
                break;
            }
        }
        if (currentQuantity + quantity > product.getQuantity()) {
            return false; // Stock limit exceeded
        }
        for (CartItem item : items) {
            if (item.getProduct().getProductId() == product.getProductId()) {
                item.setQuantity(item.getQuantity() + quantity);
                // Update stock: reduce by the quantity added
                inventory.updateProduct(product.getProductId(), null, -1, product.getQuantity() - quantity, null);
                saveToFile();
                return true;
            }
        }
        items.add(new CartItem(product, quantity));
        // Update stock: reduce by the quantity added
        inventory.updateProduct(product.getProductId(), null, -1, product.getQuantity() - quantity, null);
        saveToFile();
        return true;
    }

    public boolean editQuantity(int productId, int newQuantity, Inventory inventory) {
        loadFromFile(); // Ensure the latest cart data is loaded
        Product product = Product.getProduct(productId, inventory);
        if (product == null) {
            return false;
        }
        // Find the current quantity in the cart
        CartItem targetItem = null;
        int currentQuantity = 0;
        for (CartItem item : items) {
            if (item.getProduct().getProductId() == productId) {
                targetItem = item;
                currentQuantity = item.getQuantity();
                break;
            }
        }
        if (targetItem == null) {
            return false;
        }
        // Check if the new quantity exceeds available stock
        int stockAdjustment = currentQuantity - newQuantity; // Positive if reducing quantity, negative if increasing
        int newStock = product.getQuantity() + stockAdjustment;
        if (newStock < 0) {
            return false; // Not enough stock to increase quantity
        }
        // Update the cart
        if (newQuantity <= 0) {
            items.remove(targetItem);
        } else {
            targetItem.setQuantity(newQuantity);
        }
        // Update the stock in inventory
        inventory.updateProduct(productId, null, -1, newStock, null);
        saveToFile();
        return true;
    }

    public double getTotal() {
        loadFromFile(); // Ensure the latest cart data is loaded
        double total = 0;
        for (CartItem item : items) {
            total += item.getSubtotal();
        }
        return total;
    }

    public List<CartItem> getItems() {
        loadFromFile(); // Ensure the latest cart data is loaded
        return items;
    }

    public void clearCart() {
        items.clear();
        // Delete the cart file
        try {
            String filePath = "cart_data/" + username + "_cart.txt";
            File file = new File(filePath);
            if (file.exists()) {
                if (!file.delete()) {
                    System.err.println("Error deleting cart file: Unable to delete " + filePath);
                }
            }
        } catch (Exception e) {
            System.err.println("Error deleting cart file: " + e.getMessage());
        }
    }

    private void loadFromFile() {
        items.clear(); // Clear current items to avoid duplicates
        String filePath = "cart_data/" + username + "_cart.txt";
        File file = new File(filePath);
        if (!file.exists()) {
            return; // File doesn't exist, nothing to load
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length != 5) {
                    continue; // Skip malformed lines
                }
                int productId = Integer.parseInt(parts[0]);
                String name = parts[1];
                double price = Double.parseDouble(parts[2]);
                int quantity = Integer.parseInt(parts[3]);
                // Create a Product instance (we don't need inventory here since we're just displaying)
                Product product = new Product(productId, name, price, 0, "Loaded from cart") {};
                items.add(new CartItem(product, quantity));
            }
        } catch (IOException | NumberFormatException e) {
            System.err.println("Error loading cart from file: " + e.getMessage());
        }
    }

    private void saveToFile() {
        try {
            // Create cart_data folder if it doesn't exist
            File dir = new File("cart_data");
            if (!dir.exists()) {
                if (!dir.mkdirs()) {
                    System.err.println("Error creating cart_data directory");
                    return;
                }
            }
            
            // Create file path: cart_data/(username)_cart.txt
            String filePath = "cart_data/" + username + "_cart.txt";
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
                for (CartItem item : items) {
                    Product p = item.getProduct();
                    writer.write(String.format("%d,%s,%.2f,%d,%.2f",
                        p.getProductId(), p.getName(), p.getPrice(), item.getQuantity(), item.getSubtotal()));
                    writer.newLine();
                }
            }
        } catch (IOException e) {
            System.err.println("Error saving cart to file: " + e.getMessage());
        }
    }

    @Override
    public String toString() {
        loadFromFile(); // Ensure the latest cart data is loaded
        StringBuilder sb = new StringBuilder();
        for (CartItem item : items) {
            sb.append(item).append("\n");
        }
        sb.append("Total: RM").append(String.format("%.2f", getTotal()));
        return sb.toString();
    }
}