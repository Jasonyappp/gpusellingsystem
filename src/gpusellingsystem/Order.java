package gpusellingsystem;

import java.time.LocalDate;
import java.util.List;
import java.time.format.DateTimeFormatter;
import java.io.Serializable;
import java.util.ArrayList;

public class Order implements Serializable {
    private static final long serialVersionUID = 1L;
    private static int nextOrderId = 1000; // Start Order IDs at 1000 to avoid overlap with Product IDs
    private final int orderId;
    private final String username;
    private final List<CartItem> items;
    private final double total; // Only keep total, remove discountedTotal
    private final LocalDate orderDate;
    private String paymentStatus;

    public Order(Cart cart, Customer customer) {
        this.orderId = nextOrderId++;
        this.username = customer.getUsername();
        // Deep copy items to prevent modifications after order creation
        this.items = new ArrayList<>();
        for (CartItem item : cart.getItems()) {
            this.items.add(new CartItem(item.getProduct(), item.getQuantity()));
        }
        this.total = cart.getTotal();
        this.orderDate = LocalDate.now();
        this.paymentStatus = "Pending";
    }

    // New constructor for loading from file
    public Order(int orderId, String username, Cart cart, LocalDate orderDate, double total) {
        this.orderId = orderId;
        this.username = username;
        // Deep copy items from cart
        this.items = new ArrayList<>();
        for (CartItem item : cart.getItems()) {
            this.items.add(new CartItem(item.getProduct(), item.getQuantity()));
        }
        this.total = total;
        this.orderDate = orderDate;
        this.paymentStatus = "Unknown";
    }

    public int getOrderId() {
        return orderId;
    }

    public String getUsername() {
        return username;
    }

    public List<CartItem> getItems() {
        return new ArrayList<>(items); // Return defensive copy
    }

    public double getTotal() {
        return total;
    }
    
    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public LocalDate getOrderDate() {
        return orderDate;
    }

    // 修改后的 getCart 方法：直接构造 Cart，不触发库存更新
    public Cart getCart() {
        Cart cart = new Cart(username);
        cart.clearItems(); // 清空默认加载的 items
        for (CartItem item : items) {
            cart.addItemDirectly(new CartItem(item.getProduct(), item.getQuantity()));
        }
        return cart;
    }

    // Synchronize nextOrderId with OrderHistory
    public static void setNextOrderId(int id) {
        nextOrderId = Math.max(nextOrderId, id);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("==================================================\n");
        sb.append("Order ID: ").append(orderId).append("\n");
        sb.append("Username: ").append(username).append("\n");
        sb.append("Order Date: ").append(orderDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))).append("\n");
        sb.append("--------------------------------------------------\n");
        sb.append("Items:\n");
        sb.append(String.format("%-8s%-20s%-15s%-15s%-15s\n", 
            "ID", "Name", "Unit Price", "Quantity", "Subtotal"));
        sb.append(String.format("%-8s%-20s%-15s%-15s%-15s\n", 
            "--", "--------------------", "---------------", "---------------", "---------------"));
        for (CartItem item : items) {
            Product p = item.getProduct();
            double subtotal = p.getPrice() * item.getQuantity();
            sb.append(String.format("%-8d%-20sRM%-14.2f%-15dRM%-14.2f\n", 
                p.getProductId(), p.getName(), p.getPrice(), item.getQuantity(), subtotal));
        }
        sb.append("--------------------------------------------------\n");
        sb.append(String.format("Total: RM %.2f\n", total)); 
        sb.append("==================================================\n");
        return sb.toString();
    }
}