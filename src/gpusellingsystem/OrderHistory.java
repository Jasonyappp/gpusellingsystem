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

    public Order getOrderByIndex(int index) {
        if (index < 1 || index > orders.size()) {
            return null;
        }
        return orders.get(index - 1);
    }

    public int getOrderCount() {
        return orders.size();
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
                if (parts.length != 5 && parts.length != 9 && parts.length != 11) { // 支持旧格式和新格式
                    continue;
                }
                int orderId = Integer.parseInt(parts[0]);
                String username = parts[1];
                LocalDate orderDate = LocalDate.parse(parts[2], DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                double total = Double.parseDouble(parts[3]);
                String paymentStatus = parts[4];

                Cart cart = new Cart(username);
                cart.clearItems();

                if (parts.length == 5) {
                    // 旧格式：没有支付细节
                    Order order = new Order(orderId, username, cart, orderDate, total, paymentStatus, 
                                            null, null, null, 0.0, null, null);
                    orders.add(order);
                } else if (parts.length == 9) {
                    // 中间格式：只有在线支付细节
                    String paymentMethod = parts[5].isEmpty() ? null : parts[5];
                    String bankName = parts[6].isEmpty() ? null : parts[6];
                    String bankUsername = parts[7].isEmpty() ? null : parts[7];
                    double bankDiscount = Double.parseDouble(parts[8]);
                    Order order = new Order(orderId, username, cart, orderDate, total, paymentStatus, 
                                            paymentMethod, bankName, bankUsername, bankDiscount, null, null);
                    orders.add(order);
                } else {
                    // 新格式：包含在线支付和COD支付细节
                    String paymentMethod = parts[5].isEmpty() ? null : parts[5];
                    String bankName = parts[6].isEmpty() ? null : parts[6];
                    String bankUsername = parts[7].isEmpty() ? null : parts[7];
                    double bankDiscount = Double.parseDouble(parts[8]);
                    String deliveryAddress = parts[9].isEmpty() ? null : parts[9];
                    String contactInfo = parts[10].isEmpty() ? null : parts[10];
                    Order order = new Order(orderId, username, cart, orderDate, total, paymentStatus, 
                                            paymentMethod, bankName, bankUsername, bankDiscount, 
                                            deliveryAddress, contactInfo);
                    orders.add(order);
                }
                Order.setNextOrderId(orderId + 1);
            }
        } catch (IOException | NumberFormatException e) {
            System.err.println("Error loading order history: " + e.getMessage());
        }
    }

    private void saveToFile() {
        try {
            File dir = new File("order_data");
            if (!dir.exists()) {
                if (!dir.mkdirs()) {
                    System.err.println("Error creating order_data directory");
                    return;
                }
            }

            String filePath = "order_data/" + username + "_orders.txt";
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
                for (Order order : orders) {
                    writer.write(String.format("%d,%s,%s,%.2f,%s,%s,%s,%s,%.2f,%s,%s",
                        order.getOrderId(),
                        order.getUsername(),
                        order.getOrderDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                        order.getTotal(),
                        order.getPaymentStatus() != null ? order.getPaymentStatus() : "Unknown",
                        order.getPaymentMethod() != null ? order.getPaymentMethod() : "",
                        order.getBankName() != null ? order.getBankName() : "",
                        order.getBankUsername() != null ? order.getBankUsername() : "",
                        order.getBankDiscount(),
                        order.getDeliveryAddress() != null ? order.getDeliveryAddress() : "",
                        order.getContactInfo() != null ? order.getContactInfo() : ""));
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