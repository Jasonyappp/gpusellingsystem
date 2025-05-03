package gpusellingsystem;

public class Admin extends User {
    public Admin(int userId, String username, String password) {
        super(userId, username, password);
    }

    @Override
    public boolean isAdmin() {
        return true;
    }

    public String generateSystemReport(UserManager userManager, ProductManager inventory) {
        int totalInventoryQuantity = inventory.getProducts().values().stream()
                .mapToInt(Product::getQuantity)
                .sum();
        int totalUsers = userManager.getAllCustomers().size() + (int) User.getUsers().values().stream().filter(User::isAdmin).count();
        double totalInventoryValue = inventory.getProducts().values().stream()
                .mapToDouble(p -> p.getPrice() * p.getQuantity())
                .sum();
        StringBuilder report = new StringBuilder();
        report.append("=== System Report ===\n");
        report.append(String.format("Total Inventory Quantity: %d\n", totalInventoryQuantity));
        report.append(String.format("Total Users: %d\n", totalUsers));
        report.append(String.format("Total Inventory Value: RM %.2f\n", totalInventoryValue));
        return report.toString();
    }
}