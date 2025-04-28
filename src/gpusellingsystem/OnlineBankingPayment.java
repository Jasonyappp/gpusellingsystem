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
    private static final double MAYBANK_DISCOUNT = 0.03; // 3% 折扣
    private static final double CIMB_DISCOUNT = 0.04; // 4% 折扣
    private static final double PUBLIC_BANK_DISCOUNT = 0.05; // 5% 折扣

    @Override
    public PaymentResult processPayment(Order order, double paymentAmount, Map<String, String> details) {
        double expectedAmount = order.getDiscountedTotal();
        if (Math.abs(paymentAmount - expectedAmount) > 0.01) {
            return new PaymentResult(false, String.format("Payment amount (RM %.2f) does not match order total (RM %.2f)!", 
                paymentAmount, expectedAmount), null, 0.0);
        }

        String bankName = details.get("bankName");
        String bankUsername = details.get("bankUsername");
        String bankPassword = details.get("bankPassword");

        List<String> validBanks = Arrays.asList("Maybank", "CIMB", "Public Bank");
        if (bankName == null || !validBanks.contains(bankName)) {
            return new PaymentResult(false, "Invalid bank name! Supported banks: Maybank, CIMB, Public Bank.", null, 0.0);
        }
        if (bankUsername == null || bankUsername.isEmpty() || bankPassword == null || bankPassword.isEmpty()) {
            return new PaymentResult(false, "Bank username or password cannot be empty!", null, 0.0);
        }

        // Determine bank-specific discount
        double bankDiscount;
        switch (bankName) {
            case "Maybank":
                bankDiscount = MAYBANK_DISCOUNT;
                break;
            case "CIMB":
                bankDiscount = CIMB_DISCOUNT;
                break;
            case "Public Bank":
                bankDiscount = PUBLIC_BANK_DISCOUNT;
                break;
            default:
                bankDiscount = 0.0; // Fallback, should not occur due to validation
        }

        // Calculate final amount after bank discount
        double finalAmount = expectedAmount * (1 - bankDiscount);
        Invoice invoice = new Invoice(order, "online_banking", true, bankName, bankUsername, null, null, null, bankDiscount);
        return new PaymentResult(true, "Online banking payment successful!", invoice, bankDiscount);
    }

    // Extend PaymentResult to include bankDiscount
    public static class PaymentResult extends PaymentMethod.PaymentResult {
        private final double bankDiscount;

        public PaymentResult(boolean success, String message, Invoice invoice, double bankDiscount) {
            super(success, message, invoice);
            this.bankDiscount = bankDiscount;
        }

        public double getBankDiscount() {
            return bankDiscount;
        }
    }
}
