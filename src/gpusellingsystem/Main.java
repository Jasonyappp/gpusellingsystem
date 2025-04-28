package gpusellingsystem;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.HashMap;
import java.util.Map;

public class Main {
    private static Inventory inventory = new Inventory();
    private static UserManager userManager = new UserManager();
    private static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        initializeUsers();
        boolean running = true;

        while (running) {
            System.out.println("\n=== Computer Retail Management System ===");
            System.out.println("1. Login");
            System.out.println("2. Register"); // 修改为 Register
            System.out.println("3. Exit");
            System.out.print("Enter your choice (1-3): ");

            int choice;
            try {
                choice = Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number (1-3).");
                continue;
            }

            switch (choice) {
                case 1:
                    User user = handleLogin();
                    if (user != null) {
                        if (user.isAdmin()) {
                            handleAdminMenu((Admin) user);
                        } else {
                            handleCustomerMenu((Customer) user, inventory);
                        }
                    }
                    break;
                case 2:
                    handleCreateAccount();
                    break;
                case 3:
                    running = false;
                    System.out.println("Thank you for using the system. Goodbye!");
                    break;
                default:
                    System.out.println("Invalid choice. Please enter 1, 2, or 3.");
            }
        }
        scanner.close();
    }

    private static void initializeUsers() {
        User.loadUsersFromFile();
        if (User.getUsers().isEmpty()) {
            User.getUsers().put("admin_leong", new Admin(User.getNextUserId(), "admin_leong", "adminpass"));
            User.incrementNextUserId();
            User.getUsers().put("admin_yap", new Admin(User.getNextUserId(), "admin_yap", "adminpass"));
            User.incrementNextUserId();
            User.saveUsersToFile();
        }
    }

    private static User handleLogin() {
        System.out.print("Enter username: ");
        String username = scanner.nextLine();
        User user = User.getUsers().get(username);
        if (user == null) {
            System.out.println("Login failed. Invalid username.");
            return null;
        }

        if (!(user instanceof Customer)) {
            System.out.print("Enter password: ");
            String password = scanner.nextLine();
            if (user.login(password)) {
                System.out.println("Logged in as " + username + ".");
                return user;
            } else {
                System.out.println("Login failed. Invalid password.");
                return null;
            }
        }

        System.out.print("Enter password: ");
        String password = scanner.nextLine();
        if (user.isLockedOut()) {
            long remainingSeconds = user.getRemainingLockoutSeconds();
            System.out.println("Account is locked. " + remainingSeconds + " seconds remaining.");
            System.out.print("Do you want to reset password? (y/n): ");
            String resetChoice = scanner.nextLine().toLowerCase();
            if (resetChoice.equals("y")) {
                System.out.print("Enter new password: ");
                String newPassword = scanner.nextLine();
                if (newPassword.isEmpty()) {
                    System.out.println("Password cannot be empty. Reset cancelled.");
                    return null;
                }
                System.out.print("Enter admin username: ");
                String adminUsername = scanner.nextLine();
                System.out.print("Enter admin password: ");
                String adminPassword = scanner.nextLine();
                if (User.authenticateAdmin(adminUsername, adminPassword)) {
                    user.resetPassword(newPassword);
                    System.out.println("Password reset successfully. Please log in again.");
                    return null;
                } else {
                    System.out.println("Admin authentication failed. Password reset cancelled.");
                    return null;
                }
            }
            return null;
        }

        if (user.login(password)) {
            System.out.println("Logged in as " + username + ".");
            return user;
        } else {
            System.out.println("Login failed. Invalid password. Attempts: " + user.getFailedLoginAttempts() + "/3");
            if (user.getFailedLoginAttempts() >= 3) {
                long remainingSeconds = user.getRemainingLockoutSeconds();
                System.out.println("Account is locked. " + remainingSeconds + " seconds remaining.");
                System.out.print("Do you want to reset password? (y/n): ");
                String resetChoice = scanner.nextLine().toLowerCase();
                if (resetChoice.equals("y")) {
                    System.out.print("Enter new password: ");
                    String newPassword = scanner.nextLine();
                    if (newPassword.isEmpty()) {
                        System.out.println("Password cannot be empty. Reset cancelled.");
                        return null;
                    }
                    System.out.print("Enter admin username: ");
                    String adminUsername = scanner.nextLine();
                    System.out.print("Enter admin password: ");
                    String adminPassword = scanner.nextLine();
                    if (User.authenticateAdmin(adminUsername, adminPassword)) {
                        user.resetPassword(newPassword);
                        System.out.println("Password reset successfully. Please log in again.");
                        return null;
                    } else {
                        System.out.println("Admin authentication failed. Password reset cancelled.");
                        return null;
                    }
                }
                return null;
            }
            return null;
        }
    }

    private static void handleCreateAccount() {
        System.out.print("Enter new username: ");
        String username = scanner.nextLine();
        if (username.isEmpty()) {
            System.out.println("Username cannot be empty. Registration cancelled.");
            return;
        }
        if (User.getUsers().containsKey(username)) {
            System.out.println("Username already exists. Please choose a different username.");
            return;
        }

        System.out.print("Enter new password: ");
        String password = scanner.nextLine();
        if (password.isEmpty()) {
            System.out.println("Password cannot be empty. Registration cancelled.");
            return;
        }
        if (password.length() < 5 || password.length() > 12) {
            System.out.println("Password must be between 5 and 12 characters. Registration cancelled.");
            return;
        }

        System.out.print("Do you want to join as a member? (y/n): ");
        String joinMember = scanner.nextLine().toLowerCase();

        User user;
        if (joinMember.equals("y")) {
            user = new Member(User.getNextUserId(), username, password);
        } else {
            user = new NonMember(User.getNextUserId(), username, password);
        }
        User.incrementNextUserId();
        User.getUsers().put(username, user);
        User.saveUsersToFile();
        System.out.println("Account created successfully for " + username + " with ID " + (User.getNextUserId() - 1));
    }

    private static void handleAdminMenu(Admin admin) {
        boolean inAdminMenu = true;
        while (inAdminMenu && admin.isLoggedIn()) {
            System.out.println("\n=== Admin Page ===");
            System.out.println("1. Manage Product");
            System.out.println("2. Manage Customers");
            System.out.println("3. Logout");
            System.out.print("Enter your choice (1-3): ");

            int choice;
            try {
                choice = Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
                continue;
            }

            switch (choice) {
                case 1: // Manage Product
                    boolean inProductManagementMenu = true;
                    while (inProductManagementMenu) {
                        System.out.println("\n=== Manage Product ===");
                        System.out.println("1. Add Product");
                        System.out.println("2. Remove Product");
                        System.out.println("3. Update Product");
                        System.out.println("4. Search Product");
                        System.out.println("5. View Product Listing");
                        System.out.println("6. Back to Admin Page");
                        System.out.print("Enter your choice (1-6): ");

                        int productChoice;
                        try {
                            productChoice = Integer.parseInt(scanner.nextLine());
                        } catch (NumberFormatException e) {
                            System.out.println("Invalid input. Please enter a number.");
                            continue;
                        }

                        switch (productChoice) {
                            case 1: // Add Product
                                System.out.print("Enter product type (GPU/CPU): ");
                                String type = scanner.nextLine();
                                System.out.print("Enter product name: ");
                                String name = scanner.nextLine();
                                System.out.print("Enter product price: ");
                                double price;
                                try {
                                    price = Double.parseDouble(scanner.nextLine());
                                } catch (NumberFormatException e) {
                                    System.out.println("Invalid price. Please enter a valid number.");
                                    continue;
                                }
                                System.out.print("Enter product quantity: ");
                                int quantity;
                                try {
                                    quantity = Integer.parseInt(scanner.nextLine());
                                } catch (NumberFormatException e) {
                                    System.out.println("Invalid quantity. Please enter a valid number.");
                                    continue;
                                }
                                System.out.print("Enter product detail: ");
                                String detail = scanner.nextLine();
                                if (inventory.addProduct(name, price, quantity, detail, type)) {
                                    System.out.println("Product added successfully.");
                                } else {
                                    System.out.println("Failed to add product. Check input values or type (must be GPU or CPU).");
                                }
                                break;
                            case 2: // Remove Product
                                System.out.print("Enter product ID to remove: ");
                                int removeId;
                                try {
                                    removeId = Integer.parseInt(scanner.nextLine());
                                } catch (NumberFormatException e) {
                                    System.out.println("Invalid product ID. Please enter a number.");
                                    continue;
                                }
                                if (inventory.removeProduct(removeId)) {
                                    System.out.println("Product with ID " + removeId + " removed successfully.");
                                } else {
                                    System.out.println("Product with ID " + removeId + " not found.");
                                }
                                break;
                            case 3: // Update Product
                                System.out.print("Enter product ID to update: ");
                                int updateId;
                                try {
                                    updateId = Integer.parseInt(scanner.nextLine());
                                } catch (NumberFormatException e) {
                                    System.out.println("Invalid product ID. Please enter a number.");
                                    continue;
                                }
                                Product productToUpdate = inventory.getProducts().get(updateId);
                                if (productToUpdate == null) {
                                    System.out.println("Product with ID " + updateId + " not found.");
                                    break;
                                }
                                System.out.println("Original name is '" + productToUpdate.getName() + "', enter new name (or press Enter to keep): ");
                                String newName = scanner.nextLine();
                                System.out.println("Original price is 'RM" + productToUpdate.getPrice() + "', enter new price (or -1 to keep): ");
                                double newPrice;
                                try {
                                    newPrice = Double.parseDouble(scanner.nextLine());
                                } catch (NumberFormatException e) {
                                    System.out.println("Invalid price. Using existing value.");
                                    newPrice = -1;
                                }
                                System.out.println("Original quantity is '" + productToUpdate.getQuantity() + "', enter new quantity (or -1 to keep): ");
                                int newQuantity;
                                try {
                                    newQuantity = Integer.parseInt(scanner.nextLine());
                                } catch (NumberFormatException e) {
                                    System.out.println("Invalid quantity. Using existing value.");
                                    newQuantity = -1;
                                }
                                System.out.println("Original detail is '" + productToUpdate.getDetail() + "', enter new detail (or press Enter to keep): ");
                                String newDetail = scanner.nextLine();
                                inventory.updateProduct(updateId, newName, newPrice, newQuantity, newDetail);
                                System.out.println("Product with ID " + updateId + " updated successfully.");
                                break;
                            case 4: // Search Product
                                System.out.print("Enter product name to search: ");
                                String searchName = scanner.nextLine();
                                Product product = inventory.searchProduct(searchName);
                                if (product != null) {
                                    System.out.println("Found: " + product);
                                } else {
                                    System.out.println("Product with name '" + searchName + "' not found.");
                                }
                                break;
                            case 5: // View Product Listing
                                System.out.println("\nProduct Listing:");
                                if (inventory.getProducts().isEmpty()) {
                                    System.out.println("No products available.");
                                } else {
                                    System.out.printf("%-8s%-20s%-15s%-15s%-30s%-10s%n", 
                                        "ID", "Name", "Price", "Quantity", "Detail", "Type");
                                    System.out.printf("%-8s%-20s%-15s%-15s%-30s%-10s%n", 
                                        "--", "--------------------", "---------------", "---------------", "------------------------------", "----------");
                                    for (Product p : inventory.getProducts().values()) {
                                        System.out.printf("%-8d%-20sRM%-14.1f%-15d%-30s%-10s%n", 
                                            p.getProductId(), p.getName(), p.getPrice(), p.getQuantity(), p.getDetail(), 
                                            p.getClass().getSimpleName());
                                    }
                                }
                                break;
                            case 6: // Back to Admin Page
                                inProductManagementMenu = false;
                                break;
                            default:
                                System.out.println("Invalid choice. Please enter 1, 2, 3, 4, 5, or 6.");
                        }
                    }
                    break;
                case 2: // Manage Customers
                    boolean inCustomerManagementMenu = true;
                    while (inCustomerManagementMenu) {
                        System.out.println("\n=== Manage Customers ===");
                        System.out.println("1. View All Customers");
                        System.out.println("2. Delete Customer");
                        System.out.println("3. Update Customer");
                        System.out.println("4. Search Customer");
                        System.out.println("5. Back to Admin Page");
                        System.out.print("Choose an option (1-5): ");

                        int customerManagementChoice;
                        try {
                            customerManagementChoice = Integer.parseInt(scanner.nextLine());
                        } catch (NumberFormatException e) {
                            System.out.println("Invalid input. Please enter a number.");
                            continue;
                        }

                        switch (customerManagementChoice) {
                            case 1: // 查看所有客户
                                List<Customer> customers = userManager.getAllCustomers();
                                if (customers.isEmpty()) {
                                    System.out.println("No customers found.");
                                } else {
                                    System.out.printf("%-8s%-20s%-20s%-15s%-10s%n", 
                                        "ID", "Username", "Password", "Type", "Discount");
                                    System.out.printf("%-8s%-20s%-20s%-15s%-10s%n", 
                                        "--", "--------------------", "--------------------", "---------------", "----------");
                                    for (Customer customer : customers) {
                                        String customerType = customer instanceof Member ? "Member" : "NonMember";
                                        System.out.printf("%-8d%-20s%-20s%-15s%-10.1f%%%n", 
                                            customer.getUserId(), customer.getUsername(), customer.getPassword(), 
                                            customerType, customer.getDiscount() * 100);
                                    }
                                }
                                break;
                            case 2: // 删除客户
                                System.out.print("Enter customer username to delete: ");
                                String deleteUsername = scanner.nextLine();
                                if (userManager.deleteCustomer(deleteUsername)) {
                                    System.out.println("Customer '" + deleteUsername + "' deleted successfully.");
                                } else {
                                    System.out.println("Customer '" + deleteUsername + "' not found or is an admin.");
                                }
                                break;
                            case 3: // 修改客户信息
                                System.out.print("Enter customer username to update: ");
                                String updateUsername = scanner.nextLine();
                                Customer customerToUpdate = userManager.searchCustomer(updateUsername);
                                if (customerToUpdate == null) {
                                    System.out.println("Customer '" + updateUsername + "' not found.");
                                    break;
                                }
                                System.out.println("Current username: " + customerToUpdate.getUsername() + ", enter new username (or press Enter to keep): ");
                                String newUsername = scanner.nextLine();
                                System.out.println("Enter new password (or press Enter to keep): ");
                                String newPassword = scanner.nextLine();
                                System.out.println("Current type: " + (customerToUpdate instanceof Member ? "Member" : "NonMember") + ", make member? (y/n): ");
                                String memberChoice = scanner.nextLine().toLowerCase();
                                boolean isMember = memberChoice.equals("y");
                                if (userManager.updateCustomer(updateUsername, newUsername, newPassword, isMember)) {
                                    System.out.println("Customer updated successfully.");
                                } else {
                                    System.out.println("Failed to update customer. New username may already exist.");
                                }
                                break;
                            case 4: // 搜索客户
                                System.out.print("Enter customer username to search: ");
                                String searchUsername = scanner.nextLine();
                                Customer foundCustomer = userManager.searchCustomer(searchUsername);
                                if (foundCustomer != null) {
                                    String customerType = foundCustomer instanceof Member ? "Member" : "NonMember";
                                    System.out.printf("Found: ID: %d, Username: %s, Password: %s, Type: %s, Discount: %.1f%%\n", 
                                        foundCustomer.getUserId(), foundCustomer.getUsername(), foundCustomer.getPassword(), 
                                        customerType, foundCustomer.getDiscount() * 100);
                                } else {
                                    System.out.println("Customer '" + searchUsername + "' not found.");
                                }
                                break;
                            case 5: // 返回管理员页面
                                inCustomerManagementMenu = false;
                                break;
                            default:
                                System.out.println("Invalid option! Please choose 1, 2, 3, 4, or 5.");
                        }
                    }
                    break;
                case 3: // Logout
                    admin.logout();
                    inAdminMenu = false;
                    break;
                default:
                    System.out.println("Invalid choice. Please enter 1, 2, or 3.");
            }
        }
    }

    private static void handleCustomerMenu(Customer customer, Inventory inventory) {
        Cart cart = new Cart(customer.getUsername());
        OrderHistory history = new OrderHistory(customer.getUsername());

        System.out.println("\n=== Welcome to GPU/CPU Shop ===");
        boolean inCustomerMenu = true;
        while (inCustomerMenu && customer.isLoggedIn()) {
            System.out.println("\n=== Main Menu ===");
            System.out.println("1. View Products");
            System.out.println("2. View Cart");
            System.out.println("3. Checkout");
            System.out.println("4. View Order History");
            System.out.println("5. Logout");
            System.out.print("Choose an option: ");
            int choice;
            try {
                choice = Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
                continue;
            }

            if (choice == 1) {
                boolean inViewProductsMenu = true;
                while (inViewProductsMenu) {
                    System.out.println("\n=== View Products ===");
                    System.out.println("1. View GPU Products");
                    System.out.println("2. View CPU Products");
                    System.out.println("3. Back to Main Menu");
                    System.out.print("Choose an option: ");
                    int viewChoice;
                    try {
                        viewChoice = Integer.parseInt(scanner.nextLine());
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid input. Please enter a number.");
                        continue;
                    }

                    if (viewChoice == 1 || viewChoice == 2) {
                        List<Product> filteredProducts = new ArrayList<>();
                        String productType = viewChoice == 1 ? "GPU" : "CPU";
                        for (Product p : Product.getAllProducts(inventory)) {
                            if ((viewChoice == 1 && p instanceof GPU) || (viewChoice == 2 && p instanceof CPU)) {
                                filteredProducts.add(p);
                            }
                        }

                        System.out.println("\n=== " + productType + " Products ===");
                        if (filteredProducts.isEmpty()) {
                            System.out.println("No " + productType + " products available!");
                        } else {
                            System.out.printf("%-8s%-20s%-15s%-15s%-30s%-15s%-15s%n", 
                                "ID", "Name", "Price", "Quantity", "Detail", "Type", "After Discount");
                            System.out.printf("%-8s%-20s%-15s%-15s%-30s%-15s%-15s%n", 
                                "--", "--------------------", "---------------", "---------------", "------------------------------", "---------------", "---------------");
                            for (Product p : filteredProducts) {
                                double discountedPrice = p.getPrice() * (1 - customer.getDiscount());
                                System.out.printf("%-8d%-20sRM%-14.1f%-15d%-30s%-15sRM%-14.1f%n", 
                                    p.getProductId(), p.getName(), p.getPrice(), p.getQuantity(), p.getDetail(), 
                                    p.getClass().getSimpleName(), discountedPrice);
                            }
                        }

                        System.out.println("\n=== Product Actions ===");
                        System.out.println("1. Add Item to Cart");
                        System.out.println("2. Back to View Products Menu");
                        System.out.print("Choose an action: ");
                        int productChoice;
                        try {
                            productChoice = Integer.parseInt(scanner.nextLine());
                        } catch (NumberFormatException e) {
                            System.out.println("Invalid input. Please enter a number.");
                            continue;
                        }

                        if (productChoice == 1) {
                            System.out.print("Enter product ID to add to cart: ");
                            int productId;
                            try {
                                productId = Integer.parseInt(scanner.nextLine());
                            } catch (NumberFormatException e) {
                                System.out.println("Invalid product ID. Please enter a number.");
                                continue;
                            }
                            Product product = Product.getProduct(productId, inventory);
                            if (product == null) {
                                System.out.println("Product not found! Please check the product ID.");
                                continue;
                            }
                            if ((viewChoice == 1 && !(product instanceof GPU)) || (viewChoice == 2 && !(product instanceof CPU))) {
                                System.out.println("Product ID does not match the selected type (" + productType + ")!");
                                continue;
                            }
                            System.out.println("Selected: " + product);
                            System.out.print("Enter quantity: ");
                            int quantity;
                            try {
                                quantity = Integer.parseInt(scanner.nextLine());
                            } catch (NumberFormatException e) {
                                System.out.println("Invalid quantity. Please enter a number.");
                                continue;
                            }
                            if (quantity <= 0) {
                                System.out.println("Quantity must be greater than 0!");
                                continue;
                            }
                            if (!cart.addItem(product, quantity, inventory)) {
                                System.out.println("Cannot add to cart: requested quantity exceeds stock (" + product.getQuantity() + ")!");
                                continue;
                            }
                            System.out.println("Successfully added to cart!");
                        } else if (productChoice == 2) {
                            continue;
                        } else {
                            System.out.println("Invalid action! Please choose a valid option.");
                        }
                    } else if (viewChoice == 3) {
                        inViewProductsMenu = false;
                    } else {
                        System.out.println("Invalid option! Please choose 1, 2, or 3.");
                    }
                }
            } else if (choice == 2) {
                System.out.println("\n=== Your Cart ===");
                if (cart.getItems().isEmpty()) {
                    System.out.println("Your cart is empty!");
                } else {
                    System.out.println(cart);
                }
                while (true) {
                    System.out.println("\n=== Cart Actions ===");
                    System.out.println("1. Edit Item Quantity (set to 0 to remove)");
                    System.out.println("2. Clear Cart");
                    System.out.println("3. Back to Main Menu");
                    System.out.print("Choose an action: ");
                    int cartChoice;
                    try {
                        cartChoice = Integer.parseInt(scanner.nextLine());
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid input. Please enter a number.");
                        continue;
                    }

                    if (cartChoice == 1) {
                        System.out.println("\nCurrent Cart:");
                        System.out.println(cart);
                        System.out.print("Enter product ID to edit quantity: ");
                        int productId;
                        try {
                            productId = Integer.parseInt(scanner.nextLine());
                        } catch (NumberFormatException e) {
                            System.out.println("Invalid product ID. Please enter a number.");
                            continue;
                        }
                        Product product = Product.getProduct(productId, inventory);
                        if (product == null || cart.getItems().stream().noneMatch(item -> item.getProduct().getProductId() == productId)) {
                            System.out.println("Product not found in cart!");
                            continue;
                        }
                        System.out.println("Product stock: " + product.getQuantity());
                        System.out.print("Enter new quantity (0 to remove): ");
                        int newQuantity;
                        try {
                            newQuantity = Integer.parseInt(scanner.nextLine());
                        } catch (NumberFormatException e) {
                            System.out.println("Invalid quantity. Please enter a number.");
                            continue;
                        }
                        if (!cart.editQuantity(productId, newQuantity, inventory)) {
                            System.out.println("Cannot update quantity: requested quantity exceeds stock (" + product.getQuantity() + ")!");
                            continue;
                        }
                        if (newQuantity <= 0) {
                            System.out.println("Item removed from cart.");
                        } else {
                            System.out.println("Quantity updated successfully.");
                        }
                    } else if (cartChoice == 2) {
                        List<CartItem> itemsToClear = new ArrayList<>(cart.getItems());
                        cart.clearCart();
                        for (CartItem item : itemsToClear) {
                            Product product = Product.getProduct(item.getProduct().getProductId(), inventory);
                            if (product != null) {
                                int newStock = product.getQuantity() + item.getQuantity();
                                inventory.updateProduct(product.getProductId(), null, -1, newStock, null);
                            }
                        }
                        System.out.println("Cart cleared successfully.");
                    } else if (cartChoice == 3) {
                        break;
                    } else {
                        System.out.println("Invalid action! Please choose a valid option.");
                    }
                }
            } else if (choice == 3) {
                if (cart.getItems().isEmpty()) {
                    System.out.println("Your cart is empty! Please add items before checking out.");
                    continue;
                }
                System.out.println("\n=== Order Summary ===");
                System.out.println("Username: " + customer.getUsername());
                System.out.println("Items in Cart:");
                System.out.println(cart);
                Order order;
                try {
                    order = new Order(cart, customer);
                } catch (RuntimeException e) {
                    System.out.println("Error creating order: " + e.getMessage());
                    continue;
                }
                System.out.println("Total (Before Discount): RM " + String.format("%.2f", order.getTotal()));
                System.out.println("Member Discount: " + String.format("%.1f%%", (1 - order.getDiscountedTotal() / order.getTotal()) * 100));
                System.out.println("Total (After Discount): RM " + String.format("%.2f", order.getDiscountedTotal()));

                System.out.print("Confirm order? (y/n): ");
                if (!scanner.nextLine().trim().toLowerCase().equals("y")) {
                    System.out.println("Order cancelled. Returning to menu.");
                    continue;
                }

                System.out.println("\n=== Payment Options ===");
                System.out.println("1. Proceed to Payment");
                System.out.println("2. Cancel Order");
                System.out.print("Choose an action (1-2): ");
                int orderChoice;
                try {
                    orderChoice = Integer.parseInt(scanner.nextLine());
                } catch (NumberFormatException e) {
                    System.out.println("Invalid input. Please enter a number.");
                    continue;
                }

                if (orderChoice == 1) {
                    boolean paymentCompleted = false;
                    while (!paymentCompleted) {
                        System.out.println("\n=== Payment Method ===");
                        System.out.println("1. Online Banking");
                        System.out.println("2. Cash on Delivery");
                        System.out.print("Choose a payment method (1-2): ");
                        int paymentChoice;
                        try {
                            paymentChoice = Integer.parseInt(scanner.nextLine());
                        } catch (NumberFormatException e) {
                            System.out.println("Invalid input. Please enter a number (1-2).");
                            continue;
                        }

                        String paymentMethod;
                        if (paymentChoice == 1) {
                            paymentMethod = "OnlineBanking";
                        } else if (paymentChoice == 2) {
                            paymentMethod = "CODPayment";
                        } else {
                            System.out.println("Invalid choice. Please select 1 or 2.");
                            continue;
                        }

                        double paymentAmount = order.getDiscountedTotal();
                        System.out.println("Payment amount: RM " + String.format("%.2f", paymentAmount));
                        System.out.print("Proceed with payment? (y/n): ");
                        if (!scanner.nextLine().trim().toLowerCase().equals("y")) {
                            System.out.println("Payment cancelled.");
                            continue;
                        }

                        PaymentMethod paymentProcessor;
                        Map<String, String> details = new HashMap<>();
                        PaymentMethod.PaymentResult result = null;

                        if (paymentMethod.equalsIgnoreCase("OnlineBanking")) {
                            paymentProcessor = new OnlineBankingPayment();
                            System.out.println("\n=== Select Bank ===");
                            System.out.println("1. Maybank");
                            System.out.println("2. CIMB");
                            System.out.println("3. Public Bank");
                            System.out.print("Choose a bank (1-3): ");
                            String input = scanner.nextLine().trim();
                            if(input.isEmpty()){
                            
                                System.out.println("Input cannot be emply. Please select a bank (1-3).");
                                continue;
                            }
                            int bankChoice;
                            try {
                                bankChoice = Integer.parseInt(input);
                            } catch (NumberFormatException e) {
                                System.out.println("Invalid input. Please enter a number (1-3).");
                                continue;
                            }

                            String bankName;
                            if (bankChoice == 1) {
                                bankName = "Maybank";
                            } else if (bankChoice == 2) {
                                bankName = "CIMB";
                            } else if (bankChoice == 3) {
                                bankName = "Public Bank";
                            } else {
                                System.out.println("Invalid choice. Please select 1, 2, or 3.");
                                continue;
                            }

                            System.out.print("Enter bank username: ");
                            String bankUsername = scanner.nextLine().trim();
                            System.out.print("Enter bank password: ");
                            String bankPassword = scanner.nextLine().trim();

                            details.put("bankName", bankName);
                            details.put("bankUsername", bankUsername);
                            details.put("bankPassword", bankPassword);
                            result = paymentProcessor.processPayment(order, paymentAmount, details);
                        } else if (paymentMethod.equalsIgnoreCase("CODPayment")) {
                            paymentProcessor = new CODPayment();
                            System.out.println("\n=== Cash on Delivery ===");
                            System.out.print("Enter delivery address: ");
                            String deliveryAddress = scanner.nextLine().trim();
                            System.out.print("Enter contact information (e.g., phone number): ");
                            String contactInfo = scanner.nextLine().trim();
                            details.put("deliveryAddress", deliveryAddress);
                            details.put("contactInfo", contactInfo);
                            result = paymentProcessor.processPayment(order, paymentAmount, details);
                        }

                        try {
                            if (result.isSuccess()) {
                                history.addOrder(order);
                                if (order.getDiscountedTotal() >= 5000 && userManager.upgradeToMember(customer.getUsername())) {
                                    System.out.println("Congratulations! Your order total exceeds RM5000 and you have been automatically upgraded to a member. You can enjoy a 10% member discount on your next purchase!");
                                }
                                System.out.println("\n=== Order Confirmation ===");
                                System.out.println(order);
                                System.out.println("\n=== Payment Result ===");
                                System.out.println(result.getMessage());
                                System.out.println("\n" + result.getInvoice().toFormattedString());
                                cart.clearCart();
                                paymentCompleted = true;
                            } else {
                                System.out.println("Payment failed: " + result.getMessage());
                                System.out.print("Retry payment? (y/n): ");
                                if (!scanner.nextLine().trim().toLowerCase().equals("y")) {
                                    System.out.println("Order retained in cart. You can retry later.");
                                    paymentCompleted = true;
                                }
                            }
                        } catch (RuntimeException e) {
                            System.out.println("Error processing payment or saving order: " + e.getMessage());
                            System.out.print("Retry payment? (y/n): ");
                            if (!scanner.nextLine().trim().toLowerCase().equals("y")) {
                                System.out.println("Order retained in cart. You can retry later.");
                                paymentCompleted = true;
                            }
                        }
                    }
                } else if (orderChoice == 2) {
                    List<CartItem> itemsToRestore = new ArrayList<>(cart.getItems());
                    try {
                        for (CartItem item : itemsToRestore) {
                            Product product = Product.getProduct(item.getProduct().getProductId(), inventory);
                            if (product != null) {
                                int newStock = product.getQuantity() + item.getQuantity();
                                inventory.updateProduct(product.getProductId(), null, -1, newStock, null);
                            }
                        }
                        System.out.println("Order cancelled. Items returned to inventory.");
                    } catch (RuntimeException e) {
                        System.out.println("Error cancelling order: " + e.getMessage());
                    }
                } else {
                    System.out.println("Invalid action! Please choose 1 or 2.");
                }
            } else if (choice == 4) {
                System.out.println("\n=== Order History ===");
                try {
                    String historyStr = history.toString();
                    if (historyStr.isEmpty()) {
                        System.out.println("You have no past orders.");
                    } else {
                        System.out.println(historyStr);
                    }
                } catch (RuntimeException e) {
                    System.out.println("Error loading order history: " + e.getMessage());
                }
            } else if (choice == 5) {
                customer.logout();
                inCustomerMenu = false;
            } else {
                System.out.println("Invalid option! Please choose a valid option.");
            }
        }
    }
}
