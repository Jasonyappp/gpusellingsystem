package gpusellingsystem;

import java.util.ArrayList;
import java.util.List;
import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 *
 * @author jason
 */
public class OrderHistory {
    private List<Order> orders;
    private final String username;

    public OrderHistory(String username) {
        this.username = username;
        this.orders = new ArrayList<>();
        loadFromFile();
    }

    public void addOrder(Order order) {
        if (order.getUsername().equals(username)) {
            orders.add(order);
            saveToFile();
        }
    }

    public Order getOrder(int orderId) {
        for (Order order : orders) {
            if (order.getOrderId() == orderId) {
                return order;
            }
        }
        return null;
    }

    private void loadFromFile() {
        orders.clear();
        String filePath = "order_data/" + username + "_orders.txt";
        File file = new File(filePath);
        if (!file.exists()) {
            return;
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length != 5) {
                    continue; // Skip malformed lines
                }
                int orderId = Integer.parseInt(parts[0]);
                String username = parts[1];
                LocalDate orderDate = LocalDate.parse(parts[2], DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                double total = Double.parseDouble(parts[3]);
                String paymentStatus = parts[4];

                // Create a dummy empty Cart
                Cart cart = new Cart(username);
                cart.clearItems(); // Ensure cart is empty
                // Create the Order object
                Order order = new Order(orderId, username, cart, orderDate, total);
                order.setPaymentStatus(paymentStatus);
                orders.add(order);
                Order.setNextOrderId(orderId + 1);
            }
        } catch (IOException | NumberFormatException e) {
            System.err.println("Error loading order history: " + e.getMessage());
        }
    }

    private void saveToFile() {
        try {
            // Create order_data folder if it doesn't exist
            File dir = new File("order_data");
            if (!dir.exists()) {
                if (!dir.mkdirs()) {
                    System.err.println("Error creating order_data directory");
                    return;
                }
            }

            // Create file path: order_data/(username)_orders.txt
            String filePath = "order_data/" + username + "_orders.txt";
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
                for (Order order : orders) {
                    writer.write(String.format("%d,%s,%s,%.2f,%s",
                        order.getOrderId(),
                        order.getUsername(),
                        order.getOrderDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                        order.getTotal(),
                        order.getPaymentStatus() != null ? order.getPaymentStatus() : "Unknown"));
                    writer.newLine();
                }
            }
        } catch (IOException e) {
            System.err.println("Error saving order history: " + e.getMessage());
        }
    }

    @Override
    public String toString() {
        if (orders.isEmpty()) {
            return "You have no past orders.";
        }
        StringBuilder sb = new StringBuilder();
        int index = 1;
        for (Order order : orders) {
            sb.append(index).append(". Order Id: ").append(order.getOrderId()).append("\n");
            sb.append("   Username: ").append(order.getUsername()).append("\n");
            sb.append("   Order Date: ").append(order.getOrderDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))).append("\n");
            sb.append("   Payment Status: ").append(order.getPaymentStatus() != null ? order.getPaymentStatus() : "Unknown").append("\n");
            index++;
        }
        return sb.toString();
    }
}