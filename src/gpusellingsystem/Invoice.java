/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package gpusellingsystem;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
/**
 *
 * @author chong
 */
public class Invoice {
    private final Order order;
    private final String paymentMethod;
    private final boolean paid;
    private final String bankName;
    private final String bankUsername;
    private final String deliveryAddress;
    private final String contactInfo;
    private final LocalDate deliveryDate;

    public Invoice(Order order, String paymentMethod, boolean paid, String bankName, String bankUsername,
                   String deliveryAddress, String contactInfo, LocalDate deliveryDate) {
        this.order = order;
        this.paymentMethod = paymentMethod;
        this.paid = paid;
        this.bankName = bankName;
        this.bankUsername = bankUsername;
        this.deliveryAddress = deliveryAddress;
        this.contactInfo = contactInfo;
        this.deliveryDate = deliveryDate;
    }

    public String toFormattedString() {
        StringBuilder sb = new StringBuilder();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        sb.append("==================================================\n");
        sb.append("                  Invoice\n");
        sb.append("==================================================\n");
        sb.append(String.format("Order ID: %d\n", order.getOrderId()));
        sb.append(String.format("Order Date: %s\n", order.getOrderDate().format(formatter)));
        sb.append("--------------------------------------------------\n");
        sb.append("Items:\n");
        sb.append(String.format("%-8s%-20s%-15s%-15s%-15s\n", 
            "ID", "Name", "Unit Price", "Quantity", "Subtotal"));
        sb.append(String.format("%-8s%-20s%-15s%-15s%-15s\n", 
            "--", "--------------------", "---------------", "---------------", "---------------"));
        for (CartItem item : order.getCart().getItems()) {
            Product p = item.getProduct();
            double subtotal = p.getPrice() * item.getQuantity();
            sb.append(String.format("%-8d%-20sRM%-14.2f%-15dRM%-14.2f\n", 
                p.getProductId(), p.getName(), p.getPrice(), item.getQuantity(), subtotal));
        }
        sb.append("--------------------------------------------------\n");
        sb.append(String.format("Total (Before Discount): RM %.2f\n", order.getTotal()));
        sb.append(String.format("Discount: %.1f%%\n", (1 - order.getDiscountedTotal() / order.getTotal()) * 100));
        sb.append(String.format("Total (After Discount): RM %.2f\n", order.getDiscountedTotal()));
        sb.append(String.format("Payment Method: %s\n", paymentMethod.equals("online_banking") ? "Online Banking" : "Cash on Delivery"));
        sb.append(String.format("Payment Status: %s\n", paid ? "Paid" : "Pending"));
        sb.append("--------------------------------------------------\n");
        if (paymentMethod.equals("online_banking")) {
            sb.append("Bank Information:\n");
            sb.append(String.format("  Bank: %s\n", bankName));
            sb.append(String.format("  Username: %s\n", bankUsername));
        } else {
            sb.append("Delivery Information:\n");
            sb.append(String.format("  Address: %s\n", deliveryAddress));
            sb.append(String.format("  Contact: %s\n", contactInfo));
            sb.append(String.format("  Estimated Delivery Date: %s\n", deliveryDate.format(formatter)));
            sb.append("  Note: Please prepare cash for payment on delivery.\n");
        }
        sb.append("==================================================\n");
        return sb.toString();
    }
}