package gpusellingsystem;

import java.util.List;

public class Order {
    private int orderId;
    private String username;
    private List<CartItem> items;
    private double total;
    private double discountedTotal;
    private static int nextOrderId = 1000; // Start Order IDs at 1000 to avoid overlap with Product IDs

    public Order(Cart cart, Customer customer) {
        this.orderId = nextOrderId++; // Automatically assign and increment Order ID
        this.username = customer.getUsername();
        this.items = cart.getItems();
        this.total = cart.getTotal();
        // Apply the customer's discount to the total
        this.discountedTotal = this.total * (1 - customer.getDiscount());
    }

    public int getOrderId() {
        return orderId;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Order ID: ").append(orderId).append("\n");
        sb.append("Username: ").append(username).append("\n");
        sb.append("Items:\n");
        for (CartItem item : items) {
            sb.append(item).append("\n");
        }
        sb.append("Total (before discount): RM").append(String.format("%.2f", total)).append("\n");
        sb.append("Discount Applied: ").append(String.format("%.1f%%", (1 - discountedTotal / total) * 100)).append("\n");
        sb.append("Total (after discount): RM").append(String.format("%.2f", discountedTotal));
        return sb.toString();
    }
}