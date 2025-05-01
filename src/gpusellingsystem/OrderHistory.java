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
                String[] parts = line.split("\\|", -1); // 使用 | 分隔元数据和 Items
                String[] metaParts = parts[0].split(",", -1); // 元数据部分
                if (metaParts.length < 5) {
                    continue;
                }
                int orderId = Integer.parseInt(metaParts[0]);
                String username = metaParts[1];
                LocalDate orderDate = LocalDate.parse(metaParts[2], DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                double total = Double.parseDouble(metaParts[3]);
                String paymentStatus = metaParts[4].isEmpty() ? "Unknown" : metaParts[4];
                String paymentMethod = metaParts.length > 5 && !metaParts[5].isEmpty() ? metaParts[5] : null;
                String bankName = metaParts.length > 6 && !metaParts[6].isEmpty() ? metaParts[6] : null;
                String bankUsername = metaParts.length > 7 && !metaParts[7].isEmpty() ? metaParts[7] : null;
                double bankDiscount = metaParts.length > 8 ? Double.parseDouble(metaParts[8]) : 0.0;
                String deliveryAddress = metaParts.length > 9 && !metaParts[9].isEmpty() ? metaParts[9] : null;
                String contactInfo = metaParts.length > 10 && !metaParts[10].isEmpty() ? metaParts[10] : null;

                // 创建 Cart 并加载 Items
                Cart cart = new Cart(username);
                cart.clearItems();
                if (parts.length > 1 && !parts[1].isEmpty()) {
                    String[] itemParts = parts[1].split(";", -1);
                    for (String itemStr : itemParts) {
                        if (itemStr.isEmpty()) continue;
                        String[] itemData = itemStr.split(",", -1);
                        if (itemData.length != 5) continue; // 包括 productType
                        int productId = Integer.parseInt(itemData[0]);
                        String name = itemData[1];
                        double price = Double.parseDouble(itemData[2]);
                        int quantity = Integer.parseInt(itemData[3]);
                        String productType = itemData[4];
                        Product product;
                        if ("GPU".equalsIgnoreCase(productType)) {
                            product = new GPU(productId, name, price, 0, "Loaded from order");
                        } else if ("CPU".equalsIgnoreCase(productType)) {
                            product = new CPU(productId, name, price, 0, "Loaded from order");
                        } else {
                            continue; // 跳过无效产品类型
                        }
                        cart.addItemDirectly(new CartItem(product, quantity));
                    }
                }

                // 创建 Order 对象
                Order order = new Order(orderId, username, cart, orderDate, total, paymentStatus,
                                        paymentMethod, bankName, bankUsername, bankDiscount,
                                        deliveryAddress, contactInfo);
                orders.add(order);
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
                    // 保存订单元数据
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
                    // 保存 Items 列表
                    writer.write("|"); // 使用分隔符区分元数据和 Items
                    for (CartItem item : order.getItems()) {
                        Product p = item.getProduct();
                        writer.write(String.format("%d,%s,%.2f,%d,%s;",
                            p.getProductId(), p.getName(), p.getPrice(), item.getQuantity(),
                            p.getClass().getSimpleName()));
                    }
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