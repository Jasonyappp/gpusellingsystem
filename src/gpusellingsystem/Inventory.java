package gpusellingsystem;


import java.util.HashMap;
import java.util.Map;

public class Inventory {
    // 封装：私有产品列表
    private Map<Integer, Product> products = new HashMap<>();
    private int nextProductId = 1;

    public Inventory() {
        // 初始化产品，添加quantity和detail
        products.put(1, new Product(1, "RTX1060", 1000.0, 10, "High-performance GPU"));
        products.put(2, new Product(2, "RTX2050", 20.0, 50, "Entry-level GPU"));
    }

    public boolean addProduct(String name, double price, int quantity, String detail) {
        if (name == null || name.trim().isEmpty() || price < 0 || quantity < 0 || detail == null || detail.trim().isEmpty()) {
            return false;
        }
        int newProductId = nextProductId;
        while (products.containsKey(newProductId)) {
            newProductId++;
        }
        products.put(newProductId, new Product(newProductId, name, price, quantity, detail));
        nextProductId = newProductId + 1;
        return true;
    }

    public boolean removeProduct(int productId) {
        return products.remove(productId) != null;
    }

    public boolean updateProduct(int productId, String newName, double newPrice, int newQuantity, String newDetail) {
        Product product = products.get(productId);
        if (product == null) return false;
        if (newName != null && !newName.isEmpty()) product.setName(newName);
        if (newPrice >= 0) product.setPrice(newPrice);
        if (newQuantity >= 0) product.setQuantity(newQuantity);
        if (newDetail != null && !newDetail.isEmpty()) product.setDetail(newDetail);
        return true;
    }

    public Product searchProduct(String productName) {
        for (Product product : products.values()) {
            if (product.getName().equalsIgnoreCase(productName)) {
                return product;
            }
        }
        return null;
    }

    public Map<Integer, Product> getProducts() {
        return products;
    }
}
