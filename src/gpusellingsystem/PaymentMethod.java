/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package gpusellingsystem;
import java.util.Map;
import java.time.LocalDate;
/**
 *
 * @author chong
 */
public abstract class PaymentMethod {
    public static class PaymentResult {
        private final boolean success;
        private final String message;
        private final Invoice invoice;

        public PaymentResult(boolean success, String message, Invoice invoice) {
            this.success = success;
            this.message = message;
            this.invoice = invoice;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }

        public Invoice getInvoice() {
            return invoice;
        }
    }

    public abstract PaymentResult processPayment(Order order, double paymentAmount, Map<String, String> details);

}