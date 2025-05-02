/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package gpusellingsystem;

public class GPU extends Product {
    private int vram;

    public GPU(int productId, String name, double price, int quantity, String detail, int vram) {
        super(productId, name, price, quantity, detail);
        this.vram = vram;
    }

    public GPU(int productId, String name, double price, int quantity, String detail) {
        super(productId, name, price, quantity, detail);
        this.vram = 4;
    }

    public int getVRAM() {
        return vram;
    }

    @Override
    public String getSpecialFeature() {
        return vram + "GB VRAM";
    }

    @Override
    public boolean isHighPerformance() {
        return vram >= 12;
    }

    @Override
    public String toString() {
        return String.format("GPU [ID: %d, Name: %s, Price: RM%.2f, Quantity: %d, Detail: %s, VRAM: %dGB, High Performance: %s]",
                getProductId(), getName(), getPrice(), getQuantity(), getDetail(), vram, isHighPerformance() ? "Yes" : "No");
    }
}