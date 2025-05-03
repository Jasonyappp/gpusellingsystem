package gpusellingsystem;
import java.util.Map;
import java.time.LocalDate;

public abstract class PaymentMethod {
    public static class PaymentResult {
        private final boolean SUCCESS;
        private final String MESSAGE;
        private final Receipt INVOICE;

        public PaymentResult(boolean success, String message, Receipt invoice) {
            this.SUCCESS = success;
            this.MESSAGE = message;
            this.INVOICE = invoice;
        }

        public boolean isSuccess() {
            return SUCCESS;
        }

        public String getMessage() {
            return MESSAGE;
        }

        public Receipt getInvoice() {
            return INVOICE;
        }
    }

    public abstract PaymentResult processPayment(Order order, Customer customer, double paymentAmount, Map<String, String> details);
}