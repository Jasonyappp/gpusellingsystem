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
        private final boolean SUCCESS;
        private final String MESSAGE;
        private final Invoice INVOICE;

        public PaymentResult(boolean success, String message, Invoice invoice) {
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

        public Invoice getInvoice() {
            return INVOICE;
        }
    }

    public abstract PaymentResult processPayment(Order order, Customer customer, double paymentAmount, Map<String, String> details);
}