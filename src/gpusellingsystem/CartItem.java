/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package gpusellingsystem;
import java.io.Serializable;
/**
 *
 * @author jason
 */
public class CartItem implements Serializable {
    private static final long serialVersionUID = 1L;
    private Product product;
    private int quantity;

    public CartItem(Product product, int quantity) {
        this.product = product;
        this.quantity = quantity;
    }

    public Product getProduct() {
        return product;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getSubtotal() {
        return product.getPrice() * quantity;
    }
    
    public void setQuantity(int quantity){
    
        this.quantity = quantity;
    }

    @Override
    public String toString() {
        return String.format("%-8d%-20sRM%-14.2f%-15dRM%-14.2f", 
            product.getProductId(), product.getName(), product.getPrice(), quantity, getSubtotal());
    }
}