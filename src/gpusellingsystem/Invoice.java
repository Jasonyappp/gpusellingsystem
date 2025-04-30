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
    private final Order ORDER;
    private final Customer CUSTOMER; // Add Customer to calculate discount
    private final String PAYMENTMETHOD;
    private final boolean PAID;
    private final String BANKNAME;
    private final String BANKUSERNAME;
    private final String DELIVERYADDRESS;
    private final String CONTACTINFO;
    private final LocalDate DELIVERYDATE;
    private final double BANKDISCOUNT;

    public Invoice(Order order, Customer customer, String paymentMethod, boolean paid, String bankName, String bankUsername,
                   String deliveryAddress, String contactInfo, LocalDate deliveryDate) {
        this(order, customer, paymentMethod, paid, bankName, bankUsername, deliveryAddress, contactInfo, deliveryDate, 0.0);
    }

    public Invoice(Order order, Customer customer, String paymentMethod, boolean paid, String bankName, String bankUsername,
                   String deliveryAddress, String contactInfo, LocalDate deliveryDate, double bankDiscount) {
        this.ORDER = order;
        this.CUSTOMER = customer;
        this.PAYMENTMETHOD = paymentMethod;
        this.PAID = paid;
        this.BANKNAME = bankName;
        this.BANKUSERNAME = bankUsername;
        this.DELIVERYADDRESS = deliveryAddress;
        this.CONTACTINFO = contactInfo;
        this.DELIVERYDATE = deliveryDate;
        this.BANKDISCOUNT = bankDiscount;
    }

    public String toFormattedString() {
        StringBuilder sb = new StringBuilder();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        sb.append("==============================================================\n");
        // 动态居中 "Receipt"
        String title = "Receipt";
        int totalWidth = 78; // 行宽，与表格长度一致
        int titleLength = title.length(); // "Receipt" 长度为 7
        int padding = (totalWidth - titleLength) / 2; // 左侧空格数量
        String centeredTitle = " ".repeat(padding) + title + "\n"; // 左侧填充空格
        sb.append(centeredTitle);
        sb.append("==============================================================\n");
        sb.append(String.format("Order ID: %d\n", ORDER.getOrderId()));
        sb.append(String.format("Order Date: %s\n", ORDER.getOrderDate().format(formatter)));
        sb.append("------------------------------------------------------------------------------\n");
        sb.append("Items:\n");
        sb.append(String.format("%-4s  %-30s  %-15s  %-10s  %-15s\n", 
                "ID", "Name", "Unit Price", "Quantity", "Subtotal"));
        sb.append("----  ------------------------------  ---------------  ----------  ---------------\n");
        for (CartItem item : ORDER.getCart().getItems()) {
            Product p = item.getProduct();
            double subtotal = p.getPrice() * item.getQuantity();
            sb.append(String.format("%-4d  %-30s  RM%-13.2f  %-10d  RM%-13.2f\n", 
                    p.getProductId(), p.getName(), p.getPrice(), item.getQuantity(), subtotal));
        }
        sb.append("------------------------------------------------------------------------------\n");
        double total = ORDER.getTotal();
        sb.append(String.format("%-30s: RM %-10.2f\n", "Total (Before Discount)", total));
        double customerDiscount = CUSTOMER.getDiscount();
        if (customerDiscount < 0 || customerDiscount > 1) {
            customerDiscount = 0; // Fallback to no discount if invalid
        }
        double totalAfterCustomerDiscount = total * (1 - customerDiscount);
        sb.append(String.format("%-30s: %-10.1f%%\n", "Customer Discount", customerDiscount * 100));
        sb.append(String.format("%-30s: RM %-10.2f\n", "Total (After Customer Discount)", totalAfterCustomerDiscount));
        sb.append(String.format("%-30s: %-10s\n", "Payment Method", 
                PAYMENTMETHOD.equals("online_banking") ? "Online Banking" : "Cash on Delivery"));
        if (PAYMENTMETHOD.equals("online_banking")) {
            sb.append(String.format("%-30s: %-10s\n", "Bank", BANKNAME));
            sb.append(String.format("%-30s: %-10.1f%%\n", "Bank Discount", BANKDISCOUNT * 100));
            sb.append(String.format("%-30s: RM %-10.2f\n", "Total (After Bank Discount)", 
                    totalAfterCustomerDiscount * (1 - BANKDISCOUNT)));
        }
        sb.append("------------------------------------------------------------------------------\n");
        if (PAYMENTMETHOD.equals("online_banking")) {
            sb.append("Bank Information:\n");
            sb.append(String.format("  %-12s: %s\n", "Bank", BANKNAME));
            sb.append(String.format("  %-12s: %s\n", "Username", BANKUSERNAME));
        } else {
            sb.append("Delivery Information:\n");
            sb.append(String.format("  %-12s: %s\n", "Address", DELIVERYADDRESS));
            sb.append(String.format("  %-12s: %s\n", "Contact", CONTACTINFO));
            sb.append(String.format("  %-12s: %s\n", "Estimated Delivery Date", DELIVERYDATE.format(formatter)));
            sb.append(String.format("  %-12s: %s\n", "Note", "Please prepare cash for payment on delivery."));
        }
        sb.append("==============================================================\n");
        return sb.toString();
    }

    // Add getters for payment calculations
    public double getTotalAfterCustomerDiscount() {
        double total = ORDER.getTotal();
        double customerDiscount = CUSTOMER.getDiscount();
        if (customerDiscount < 0 || customerDiscount > 1) {
            customerDiscount = 0;
        }
        return total * (1 - customerDiscount);
    }

    public double getBankDiscount() {
        return BANKDISCOUNT;
    }
}