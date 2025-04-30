/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package gpusellingsystem;
import java.time.LocalDate;
import java.util.Map;
/**
 *
 * @author chong
 */
public class CODPayment extends PaymentMethod {
    @Override
    public PaymentResult processPayment(Order order, Customer customer, double paymentAmount, Map<String, String> details) {
        double expectedAmount = order.getTotal();
        if (paymentAmount != expectedAmount) {
            return new PaymentResult(false, String.format("Payment amount (RM %.2f) does not match order total (RM %.2f)!", 
                paymentAmount, expectedAmount), null);
        }

        String deliveryAddress = details.get("deliveryAddress");
        String contactInfo = details.get("contactInfo");

        if (deliveryAddress == null || deliveryAddress.isEmpty() || contactInfo == null || contactInfo.isEmpty()) {
            return new PaymentResult(false, "Delivery address or contact information cannot be empty!", null);
        }
        
        if (!contactInfo.matches("\\d{10,15}")) {
        return new PaymentResult(false, "Contact information must contain only numbers!", null);
    }

        LocalDate deliveryDate = LocalDate.now().plusDays(3);
        Invoice invoice = new Invoice(order, customer, "cod_payment", false, null, null, deliveryAddress, contactInfo, deliveryDate);
        return new PaymentResult(true, "Cash on delivery order confirmed!", invoice);
    }
}