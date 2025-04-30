package gpusellingsystem;

import java.time.LocalDate;
import java.util.List;
import java.time.format.DateTimeFormatter;
import java.io.Serializable;
import java.util.ArrayList;

public class Order implements Serializable {
    private static final long serialVersionUID = 1L;
    private static int nextOrderId = 1000; // Start Order IDs at 1000 to avoid overlap with Product IDs
    private final int ORDER_ID;
    private final String USERNAME;
    private final List<CartItem> ITEMS;
    private final double TOTAL; // Only keep total, remove discountedTotal
    private final LocalDate ORDER_DATE;
    private String paymentStatus;
    private String paymentMethod; // 支付方式（"online_banking" 或 "cod_payment"）
    private String bankName;     // 银行名称（仅在线支付）
    private String bankUsername; // 银行用户名（仅在线支付）
    private double bankDiscount;  // 银行折扣（仅在线支付）
    private String deliveryAddress; // 配送地址（仅COD）
    private String contactInfo;    // 联系信息（仅COD）

    public Order(Cart cart, Customer customer) {
        this.ORDER_ID = nextOrderId++;
        this.USERNAME = customer.getUsername();
        // Deep copy items to prevent modifications after order creation
        this.ITEMS = new ArrayList<>();
        for (CartItem item : cart.getItems()) {
            this.ITEMS.add(new CartItem(item.getProduct(), item.getQuantity()));
        }
        this.TOTAL = cart.getTotal();
        this.ORDER_DATE = LocalDate.now();
        this.paymentStatus = "Pending";
        this.paymentMethod = null;
        this.bankName = null;
        this.bankUsername = null;
        this.bankDiscount = 0.0;
        this.deliveryAddress = null;
        this.contactInfo = null;
    }

    // New constructor for loading from file
    public Order(int orderId, String username, Cart cart, LocalDate orderDate, double total,
                 String paymentStatus, String paymentMethod, String bankName, String bankUsername, 
                 double bankDiscount, String deliveryAddress, String contactInfo) {
        this.ORDER_ID = orderId;
        this.USERNAME = username;
        // Deep copy items from cart
        this.ITEMS = new ArrayList<>();
        for (CartItem item : cart.getItems()) {
            this.ITEMS.add(new CartItem(item.getProduct(), item.getQuantity()));
        }
        this.TOTAL = total;
        this.ORDER_DATE = orderDate;
        this.paymentStatus = paymentStatus;
        this.paymentMethod = paymentMethod;
        this.bankName = bankName;
        this.bankUsername = bankUsername;
        this.bankDiscount = bankDiscount;
        this.deliveryAddress = deliveryAddress;
        this.contactInfo = contactInfo;
    }

    public int getOrderId() {
        return ORDER_ID;
    }

    public String getUsername() {
        return USERNAME;
    }

    public List<CartItem> getItems() {
        return new ArrayList<>(ITEMS); // Return defensive copy
    }

    public double getTotal() {
        return TOTAL;
    }
    
    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public LocalDate getOrderDate() {
        return ORDER_DATE;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public String getBankUsername() {
        return bankUsername;
    }

    public void setBankUsername(String bankUsername) {
        this.bankUsername = bankUsername;
    }

    public double getBankDiscount() {
        return bankDiscount;
    }

    public void setBankDiscount(double bankDiscount) {
        this.bankDiscount = bankDiscount;
    }

    public String getDeliveryAddress() {
        return deliveryAddress;
    }

    public void setDeliveryAddress(String deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
    }

    public String getContactInfo() {
        return contactInfo;
    }

    public void setContactInfo(String contactInfo) {
        this.contactInfo = contactInfo;
    }

    // Synchronize nextOrderId with OrderHistory
    public static void setNextOrderId(int id) {
        nextOrderId = Math.max(nextOrderId, id);
    }

    public Cart getCart() {
        Cart cart = new Cart(USERNAME);
        cart.clearItems(); // 清空默认加载的 items
        for (CartItem item : ITEMS) {
            cart.addItemDirectly(new CartItem(item.getProduct(), item.getQuantity()));
        }
        return cart;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        // 标题和分隔线
        sb.append("==============================================================\n");
        sb.append("                      Order Confirmation                      \n");
        sb.append("==============================================================\n");
        // 订单基本信息
        sb.append(String.format("Order ID: %-10d\n", ORDER_ID));
        sb.append(String.format("Username: %-20s\n", USERNAME));
        sb.append(String.format("Order Date: %-15s\n", ORDER_DATE.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))));
        sb.append("--------------------------------------------------------------\n");
        // 物品列表表头
        sb.append("Items:\n");
        sb.append(String.format("%-8s%-30s%-15s%-10s%-15s\n", 
                "ID", "Name", "Unit Price", "Quantity", "Subtotal"));
        sb.append("--------+------------------------------+---------------+----------+---------------\n");
        // 物品详情
        for (CartItem item : ITEMS) {
            Product p = item.getProduct();
            double subtotal = p.getPrice() * item.getQuantity();
            sb.append(String.format("%-8d%-30sRM%-13.2f%-10dRM%-13.2f\n", 
                    p.getProductId(), p.getName(), p.getPrice(), item.getQuantity(), subtotal));
        }
        // 总计和底部分隔线
        sb.append("--------------------------------------------------------------\n");
        sb.append(String.format("%-63sRM%-13.2f\n", "Total:", TOTAL));
        if (paymentMethod != null) {
            sb.append(String.format("Payment Method: %s\n", paymentMethod));
            if (paymentMethod.equals("online_banking")) {
                sb.append(String.format("Bank: %s\n", bankName != null ? bankName : "Unknown"));
                sb.append(String.format("Bank Username: %s\n", bankUsername != null ? bankUsername : "Unknown"));
                sb.append(String.format("Bank Discount: %.1f%%\n", bankDiscount * 100));
            } else if (paymentMethod.equals("cod_payment")) {
                sb.append(String.format("Delivery Address: %s\n", deliveryAddress != null ? deliveryAddress : "Unknown"));
                sb.append(String.format("Contact Info: %s\n", contactInfo != null ? contactInfo : "Unknown"));
            }
        }
        sb.append("==============================================================\n");
        return sb.toString();
    }
}