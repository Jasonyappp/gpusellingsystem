/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package gpusellingsystem;
import java.util.ArrayList;
import java.util.List;
import java.io.*;
/**
 *
 * @author jason
 */
public class OrderHistory implements Serializable {
    private static final long serialVersionUID = 1L;
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
        File file = new File("order_data/" + username + "_orders.dat");
        if (!file.exists()) {
            return;
        }
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            List<?> loadedOrders = (List<?>) ois.readObject();
            for (Object obj : loadedOrders) {
                if (obj instanceof Order) {
                    orders.add((Order) obj);
                    Order.setNextOrderId(((Order) obj).getOrderId() + 1);
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error loading order history: " + e.getMessage());
            // 检测是否是序列化不兼容问题（例如，NotSerializableException）
            if (e.getMessage().contains("NotSerializableException")) {
                // 备份旧文件
                File backupFile = new File("order_data/backup_" + username + "_orders.dat");
                if (file.renameTo(backupFile)) {
                    System.err.println("Backed up corrupted order history to: " + backupFile.getPath());
                } else {
                    System.err.println("Failed to back up corrupted order history file: " + file.getPath());
                }
                System.err.println("Incompatible order data detected. Resetting order history for user: " + username);
                orders.clear(); // 清空订单列表
            }
        }
    }

    private void saveToFile() {
        try {
            File dir = new File("order_data");
            if (!dir.exists() && !dir.mkdirs()) {
                System.err.println("Error creating order_data directory");
                return;
            }
            File file = new File("order_data/" + username + "_orders.dat");
            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
                oos.writeObject(orders);
            }
        } catch (IOException e) {
            System.err.println("Error saving order history: " + e.getMessage());
        }
    }

    @Override
    public String toString() {
        if (orders.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (Order order : orders) {
            sb.append(order).append("\n");
        }
        return sb.toString();
    }
}