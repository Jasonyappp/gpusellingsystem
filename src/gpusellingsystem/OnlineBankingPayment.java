package gpusellingsystem;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class OnlineBankingPayment extends PaymentMethod {
    private static final double MAYBANK_DISCOUNT = 0.03; 
    private static final double CIMB_DISCOUNT = 0.04;
    private static final double PUBLIC_BANK_DISCOUNT = 0.05; 

    @Override
    public PaymentResult processPayment(Order order, Customer customer, double paymentAmount, Map<String, String> details) {
        double expectedAmount = order.getTotal(); 
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
                bankDiscount = 0.0;
        }

        Receipt invoice = new Receipt(order, customer, "online_banking", true, bankName, bankUsername, null, null, null, bankDiscount);
        return new PaymentResult(true, "Online banking payment successful!", invoice, bankDiscount);
    }

    public static class PaymentResult extends PaymentMethod.PaymentResult {
        private final double bankDiscount;

        public PaymentResult(boolean success, String message, Receipt invoice, double bankDiscount) {
            super(success, message, invoice);
            this.bankDiscount = bankDiscount;
        }

        public double getBankDiscount() {
            return bankDiscount;
        }
    }
}