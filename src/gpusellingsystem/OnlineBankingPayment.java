/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package gpusellingsystem;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
/**
 *
 * @author chong
 */
public class OnlineBankingPayment extends PaymentMethod {
    @Override
    public PaymentResult processPayment(Order order, double paymentAmount, Map<String, String> details) {
        double expectedAmount = order.getDiscountedTotal();
        if (paymentAmount != expectedAmount) {
            return new PaymentResult(false, String.format("Payment amount (RM %.2f) does not match order total (RM %.2f)!", 
                paymentAmount, expectedAmount), null);
        }

        String bankName = details.get("bankName");
        String bankUsername = details.get("bankUsername");
        String bankPassword = details.get("bankPassword");

        List<String> validBanks = Arrays.asList("Maybank", "CIMB", "Public Bank");
        if (bankName == null || !validBanks.contains(bankName)) {
            return new PaymentResult(false, "Invalid bank name! Supported banks: Maybank, CIMB, Public Bank.", null);
        }
        if (bankUsername == null || bankUsername.isEmpty() || bankPassword == null || bankPassword.isEmpty()) {
            return new PaymentResult(false, "Bank username or password cannot be empty!", null);
        }

        Invoice invoice = new Invoice(order, "online_banking", true, bankName, bankUsername, null, null, null);
        return new PaymentResult(true, "Online banking payment successful!", invoice);
    }
}
