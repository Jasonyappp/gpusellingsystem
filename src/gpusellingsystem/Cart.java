package gpusellingsystem;

import java.util.ArrayList;
import java.util.List;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;

public class Cart implements Serializable {
    private static final long serialVersionUID = 1L;
    private List<CartItem> items;
    private final int userId;

    public Cart(int userId) {
        this.userId = userId;
        items = new ArrayList<>();
        loadFromFile();
    }

    public boolean addItem(Product product, int quantity, ProductManager inventory) {
        int currentQuantity = 0;
        for (CartItem item : items) {
            if (item.getProduct().getProductId() == product.getProductId()) {
                currentQuantity = item.getQuantity();
                break;
            }
        }
        if (currentQuantity + quantity > product.getQuantity()) {
            return false;
        }
        for (CartItem item : items) {
            if (item.getProduct().getProductId() == product.getProductId()) {
                item.setQuantity(item.getQuantity() + quantity);
                inventory.updateProduct(product.getProductId(), null, -1, product.getQuantity() - quantity, null);
                saveToFile();
                return true;
            }
        }
        items.add(new CartItem(product, quantity));
        inventory.updateProduct(product.getProductId(), null, -1, product.getQuantity() - quantity, null);
        saveToFile();
        return true;
    }

    public void addItemDirectly(CartItem item) {
        items.add(item);
    }

    public void clearItems() {
        items.clear();
    }

    public boolean editQuantity(int productId, int newQuantity, ProductManager inventory) {
        Product product = Product.getProduct(productId, inventory);
        if (product == null) {
            return false;
        }
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
        int stockAdjustment = currentQuantity - newQuantity;
        int newStock = product.getQuantity() + stockAdjustment;
        if (newStock < 0) {
            return false;
        }
        if (newQuantity <= 0) {
            items.remove(targetItem);
        } else {
            targetItem.setQuantity(newQuantity);
        }
        inventory.updateProduct(productId, null, -1, newStock, null);
        saveToFile();
        return true;
    }

    public double getTotal() {
        double total = 0;
        for (CartItem item : items) {
            total += item.getSubtotal();
        }
        return total;
    }

    public List<CartItem> getItems() {
        return new ArrayList<>(items);
    }

    public void clearCart() {
        items.clear();
        try {
            String filePath = "cart_data/user_" + userId + "_cart.txt";
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
        items.clear();
        String filePath = "cart_data/user_" + userId + "_cart.txt";
        File file = new File(filePath);
        if (!file.exists()) {
            return;
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length != 5) {
                    continue;
                }
                int productId = Integer.parseInt(parts[0]);
                String name = parts[1];
                double price = Double.parseDouble(parts[2]);
                int quantity = Integer.parseInt(parts[3]);
                Product product = new GPU(productId, name, price, 0, "Loaded from cart");
                items.add(new CartItem(product, quantity));
            }
        } catch (IOException | NumberFormatException e) {
            System.err.println("Error loading cart from file: " + e.getMessage());
        }
    }

    private void saveToFile() {
        try {
            File dir = new File("cart_data");
            if (!dir.exists()) {
                if (!dir.mkdirs()) {
                    System.err.println("Error creating cart_data directory");
                    return;
                }
            }
            String filePath = "cart_data/user_" + userId + "_cart.txt";
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
        StringBuilder sb = new StringBuilder();
        if (items.isEmpty()) {
            return "Your cart is empty!";
        }
        sb.append("=== Your Cart ===\n");
        sb.append(String.format("%-8s%-30s%-15s%-10s%-15s\n", 
            "ID", "Product Name", "Unit Price", "Quantity", "Subtotal"));
        sb.append(String.format("%-8s%-30s%-15s%-10s%-15s\n", 
            "--------", "------------------------------", "---------------", "----------", "---------------"));
        for (CartItem item : items) {
            Product p = item.getProduct();
            double subtotal = p.getPrice() * item.getQuantity();
            sb.append(String.format("%-8d%-30sRM%-13.2f%-10dRM%-13.2f\n", 
                p.getProductId(), p.getName(), p.getPrice(), item.getQuantity(), subtotal));
        }
        sb.append("-------------------------------------------------------------\n");
        sb.append(String.format("%-63sRM%-13.2f\n", "Total:", getTotal()));
        return sb.toString();
    }
}