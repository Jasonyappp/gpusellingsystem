/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package gpusellingsystem;

public class CPU extends Product {
    private int coreCount;

    public CPU(int productId, String name, double price, int quantity, String detail, int coreCount) {
        super(productId, name, price, quantity, detail);
        this.coreCount = coreCount;
    }

    public CPU(int productId, String name, double price, int quantity, String detail) {
        super(productId, name, price, quantity, detail);
        this.coreCount = 4;
    }

    public int getCoreCount() {
        return coreCount;
    }

    @Override
    public String getSpecialFeature() {
        return coreCount + " cores";
    }

    @Override
    public boolean isHighPerformance() {
        return coreCount >= 8;
    }

    @Override
    public String toString() {
        return String.format("CPU [ID: %d, Name: %s, Price: RM%.2f, Quantity: %d, Detail: %s, Cores: %d, High Performance: %s]",
                getProductId(), getName(), getPrice(), getQuantity(), getDetail(), coreCount, isHighPerformance() ? "Yes" : "No");
    }
}