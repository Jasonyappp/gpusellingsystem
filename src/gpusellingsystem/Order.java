/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package gpusellingsystem;

/**
 *
 * @author jason
 */
import java.util.List;

public class Order {
    private int orderId;
    private List<CartItem> items;
    private double total;

    public Order(int orderId, Cart cart) {
        this.orderId = orderId;
        this.items = cart.getItems();
        this.total = cart.getTotal();
    }

    public int getOrderId() {
        return orderId;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Order ID: ").append(orderId).append("\nItems:\n");
        for (CartItem item : items) {
            sb.append(item).append("\n");
        }
        sb.append("Total: $").append(total);
        return sb.toString();
    }
}
