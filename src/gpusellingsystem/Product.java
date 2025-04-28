package gpusellingsystem;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public abstract class Product implements Serializable {
    private static final long serialVersionUID = 1L;
    private int productId;
    private String name;
    private double price;
    private int quantity;
    private String detail;

    public Product(int productId, String name, double price, int quantity, String detail) {
        this.productId = productId;
        this.name = name;
        this.price = price;
        this.quantity = quantity;
        this.detail = detail;
    }

    public static Product getProduct(int productId, ProductManager inventory) {
        return inventory.getProducts().get(productId);
    }

    public static List<Product> getAllProducts(ProductManager inventory) {
        return new ArrayList<>(inventory.getProducts().values());
    }

    public int getProductId() {
        return productId;
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }

    public int getQuantity() {
        return quantity;
    }

    public String getDetail() {
        return detail;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    @Override
    public String toString() {
        return String.format("ID: %d, Name: %s, Price: RM%.1f, Quantity: %d, Detail: %s, Type: %s",
                productId, name, price, quantity, detail, this.getClass().getSimpleName());
    }
}