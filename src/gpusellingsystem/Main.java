package gpusellingsystem;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.HashMap;
import java.util.Map;

public class Main {
    private static ProductManager inventory = new ProductManager();
    private static UserManager userManager = new UserManager();
    private static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        initializeUsers();
        boolean running = true;

        while (running) {
            System.out.println("\n=== Computer Retail Management System ===");
            System.out.println("1. Login");
            System.out.println("2. Register");
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
        String username = scanner.nextLine().trim();
        User user = User.getUsers().get(username.toLowerCase());
        if (user == null) {
            System.out.println("Login failed. Invalid username.");
            return null;
        }

        if (!(user instanceof Customer)) {
            System.out.print("Enter password: ");
            String password = scanner.nextLine().trim();
            if (user.login(password)) {
                System.out.println("Logged in as " + username + ".");
                return user;
            } else {
                System.out.println("Login failed. Invalid password.");
                return null;
            }
        }

        System.out.print("Enter password: ");
        String password = scanner.nextLine().trim();
        if (user.isLockedOut()) {
            long remainingSeconds = user.getRemainingLockoutSeconds();
            System.out.println("Account is locked. " + remainingSeconds + " seconds remaining.");
            System.out.println("Please visit our physical store to reset your password with admin verification.");
            System.out.print("Do you want to proceed with admin-verified password reset? (y/n): ");
            String resetChoice = scanner.nextLine().toLowerCase().trim();
            if (resetChoice.equals("y")) {
                if (handleAdminVerifiedPasswordReset(user)) {
                    System.out.println("Password reset successfully. Please log in again.");
                } else {
                    System.out.println("Password reset failed. Please try again or contact support.");
                }
                return null;
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
                System.out.println("Please visit our physical store to reset your password with admin verification.");
                System.out.print("Do you want to proceed with admin-verified password reset? (y/n): ");
                String resetChoice = scanner.nextLine().toLowerCase().trim();
                if (resetChoice.equals("y")) {
                    if (handleAdminVerifiedPasswordReset(user)) {
                        System.out.println("Password reset successfully. Please log in again.");
                    } else {
                        System.out.println("Password reset failed. Please try again or contact support.");
                    }
                    return null;
                }
                return null;
            }
            return null;
        }
    }

    private static boolean handleAdminVerifiedPasswordReset(User user) {
        System.out.println("\n=== Admin Verification for Password Reset ===");
        System.out.print("Enter admin username: ");
        String adminUsername = scanner.nextLine().trim();
        System.out.print("Enter admin password: ");
        String adminPassword = scanner.nextLine().trim();

        if (User.authenticateAdmin(adminUsername, adminPassword)) {
            System.out.print("Enter new password for user " + user.getUsername() + ": ");
            String newPassword = scanner.nextLine().trim();
            if (newPassword.isEmpty()) {
                System.out.println("Password cannot be empty.");
                return false;
            }
            if (newPassword.length() < 5 || newPassword.length() > 12) {
                System.out.println("Password must be between 5 and 12 characters.");
                return false;
            }
            try {
                user.resetPassword(newPassword);
                System.out.println("Admin verification successful. Password reset completed.");
                return true;
            } catch (IllegalArgumentException e) {
                System.out.println("Error: " + e.getMessage());
                return false;
            }
        } else {
            System.out.println("Admin verification failed. Invalid admin username or password.");
            return false;
        }
    }

    private static void handleCreateAccount() {
        System.out.print("Enter new username: ");
        String username = scanner.nextLine().trim();
        if (username.isEmpty()) {
            System.out.println("Username cannot be empty. Registration cancelled.");
            return;
        }
        if (User.getUsers().containsKey(username.toLowerCase())) {
            System.out.println("Username already exists. Please choose a different username.");
            return;
        }

        System.out.print("Enter new password: ");
        String password = scanner.nextLine().trim();
        if (password.isEmpty()) {
            System.out.println("Password cannot be empty. Registration cancelled.");
            return;
        }
        if (password.length() < 5 || password.length() > 12) {
            System.out.println("Password must be between 5 and 12 characters. Registration cancelled.");
            return;
        }
        
        User user = new NonMember(User.getNextUserId(), username, password);
        User.incrementNextUserId();
        User.getUsers().put(username.toLowerCase(), user);
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
                case 1 -> {
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
                            case 1:
                                System.out.print("Enter product type (GPU/CPU): ");
                                String type = scanner.nextLine().toUpperCase().trim();
                                if (!type.equals("GPU") && !type.equals("CPU")) {
                                    System.out.println("Invalid product type. Please enter GPU or CPU.");
                                    break;
                                }
                                System.out.print("Enter product name: ");
                                String name = scanner.nextLine().trim();
                                System.out.print("Enter product price: ");
                                double price;
                                try {
                                    price = Double.parseDouble(scanner.nextLine());
                                    if (price < 0) {
                                        System.out.println("Invalid price. Negative values are not allowed.");
                                        break;
                                    }
                                } catch (NumberFormatException e) {
                                    System.out.println("Invalid price. Please enter a valid number.");
                                    break;
                                }
                                System.out.print("Enter product quantity: ");
                                int quantity;
                                try {
                                    quantity = Integer.parseInt(scanner.nextLine());
                                    if (quantity < 0) {
                                        System.out.println("Invalid quantity. Negative values are not allowed.");
                                        break;
                                    }
                                } catch (NumberFormatException e) {
                                    System.out.println("Invalid quantity. Please enter a valid number.");
                                    break;
                                }
                                System.out.print("Enter product detail: ");
                                String detail = scanner.nextLine();
                                if (inventory.addProduct(name, price, quantity, detail, type)) {
                                    System.out.println("Product added successfully.");
                                } else {
                                    System.out.println("Invalid product name. This product has already exist.");
                                }
                                break;
                            case 2:
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
                            case 3:
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
                                System.out.println("Original price is 'RM" + productToUpdate.getPrice() + "', enter new price (or Enter to keep): ");
                                double newPrice = -1;
                                String priceInput = scanner.nextLine();
                                if (!priceInput.isEmpty()) {
                                    try {
                                        newPrice = Double.parseDouble(priceInput);
                                        if (newPrice < 0) {
                                            System.out.println("Invalid price. Negative values are not allowed.");
                                            newPrice = -1;
                                        }
                                    } catch (NumberFormatException e) {
                                        System.out.println("Invalid price. Using existing value.");
                                        newPrice = -1;
                                    }
                                }
                                System.out.println("Original quantity is '" + productToUpdate.getQuantity() + "', enter new quantity (or Enter to keep): ");
                                int newQuantity = -1;
                                String quantityInput = scanner.nextLine();
                                if (!quantityInput.isEmpty()) {
                                    try {
                                        newQuantity = Integer.parseInt(quantityInput);
                                        if (newQuantity < 0 && newQuantity != -1) {
                                            System.out.println("Invalid quantity. Negative values are not allowed.");
                                            newQuantity = -1;
                                        }
                                    } catch (NumberFormatException e) {
                                        System.out.println("Invalid quantity. Using existing value.");
                                        newQuantity = -1;
                                    }
                                }
                                System.out.println("Original detail is '" + productToUpdate.getDetail() + "', enter new detail (or press Enter to keep): ");
                                String newDetail = scanner.nextLine();
                                inventory.updateProduct(updateId, newName, newPrice, newQuantity, newDetail);
                                System.out.println("Product with ID " + updateId + " updated successfully.");
                                break;
                            case 4:
                                System.out.print("Enter product ID to search: ");
                                int searchId;
                                try {
                                    searchId = Integer.parseInt(scanner.nextLine());
                                } catch (NumberFormatException e) {
                                    System.out.println("Invalid product ID. Please enter a valid number.");
                                    break;
                                }
                                Product product = inventory.searchProductById(searchId);
                                if (product != null) {
                                    System.out.println("Found: " + product);
                                } else {
                                    System.out.println("Product with ID " + searchId + " not found.");
                                }
                                break;
                            case 5:
                                System.out.println("\nProduct Listing:");
                                if (inventory.getProducts().isEmpty()) {
                                    System.out.println("No products available.");
                                } else {
                                    System.out.printf("%-8s%-30s%-15s%-12s%-50s%-10s%n",
                                            "ID", "Name", "Price", "Quantity", "Detail", "Type");
                                    System.out.printf("%-8s%-30s%-15s%-12s%-50s%-10s%n",
                                            "--------", "------------------------------", "---------------", "------------", "--------------------------------------------------", "----------");
                                    for (Product p : inventory.getProducts().values()) {
                                        System.out.printf("%-8d%-30sRM%-13.2f%-12d%-50s%-10s%n",
                                                p.getProductId(), p.getName(), p.getPrice(), p.getQuantity(), p.getDetail(),
                                                p.getClass().getSimpleName());
                                    }
                                }
                                break;
                            case 6:
                                inProductManagementMenu = false;
                                break;
                            default:
                                System.out.println("Invalid choice. Please enter 1, 2, 3, 4, 5, or 6.");
                        }
                    }
                }
                case 2 -> {
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
                            case 1:
                                List<Customer> customers = userManager.getAllCustomers();
                                if (customers.isEmpty()) {
                                    System.out.println("No customers found.");
                                } else {
                                    System.out.printf("%-8s%-25s%-15s%-12s%n",
                                            "ID", "Username", "Type", "Discount");
                                    System.out.printf("%-8s%-25s%-15s%-12s%n",
                                            "--------", "-------------------------", "---------------", "------------");
                                    for (Customer customer : customers) {
                                        String customerType = customer instanceof Member ? "Member" : "NonMember";
                                        System.out.printf("%-8d%-25s%-15s%.1f%%%n",
                                                customer.getUserId(), customer.getUsername(),
                                                customerType, customer.getDiscount() * 100);
                                    }
                                }
                                break;
                            case 2:
                                System.out.print("Enter customer username to delete: ");
                                String deleteUsername = scanner.nextLine();
                                if (userManager.deleteCustomer(deleteUsername)) {
                                    System.out.println("Customer '" + deleteUsername + "' deleted successfully.");
                                } else {
                                    System.out.println("Customer '" + deleteUsername + "' not found or is an admin.");
                                }
                                break;
                            case 3:
                                System.out.print("Enter customer username to update: ");
                                String updateUsername = scanner.nextLine();
                                Customer customerToUpdate = userManager.searchCustomer(updateUsername);
                                if (customerToUpdate == null) {
                                    System.out.println("Customer '" + updateUsername + "' not found.");
                                    break;
                                }
                                System.out.println("Current username: " + customerToUpdate.getUsername() + ", enter new username (or press Enter to keep): ");
                                String newUsername = scanner.nextLine();
                                
                                boolean wasMember = customerToUpdate instanceof Member;
                                boolean isMember = wasMember;
                                boolean membershipChanged = false;

                                while (true) {
                                    System.out.println("Current type: " + (wasMember ? "Member" : "NonMember") + ", make member? (y/n): ");
                                    String memberChoice = scanner.nextLine().trim().toLowerCase();
                                    if (memberChoice.equals("y")) {
                                        isMember = true;
                                        membershipChanged = (isMember != wasMember);
                                        break;
                                    } else if (memberChoice.equals("n")) {
                                        isMember = false;
                                        membershipChanged = (isMember != wasMember);
                                        break;
                                    } else {
                                        System.out.println("Invalid input. Please enter 'y' or 'n'.");
                                    }
                                }

                                boolean usernameChanged = !newUsername.isEmpty() && !newUsername.equals(customerToUpdate.getUsername());
                                if (usernameChanged || membershipChanged) {
                                    userManager.updateCustomer(updateUsername, newUsername, "", isMember);
                                    System.out.println("Customer updated successfully.");
                                } else {
                                    System.out.println("No changes made to customer profile.");
                                }
                                break;
                            case 4:
                                System.out.print("Enter customer username to search: ");
                                String searchUsername = scanner.nextLine();
                                Customer foundCustomer = userManager.searchCustomer(searchUsername);
                                if (foundCustomer != null) {
                                    String customerType = foundCustomer instanceof Member ? "Member" : "NonMember";
                                    System.out.printf("Found: ID: %d, Username: %s, Type: %s, Discount: %.1f%%\n",
                                            foundCustomer.getUserId(), foundCustomer.getUsername(),
                                            customerType, foundCustomer.getDiscount() * 100);
                                } else {
                                    System.out.println("Customer '" + searchUsername + "' not found.");
                                }
                                break;
                            case 5:
                                inCustomerManagementMenu = false;
                                break;
                            default:
                                System.out.println("Invalid option! Please choose 1, 2, 3, 4, or 5.");
                        }
                    }
                }
                case 3 -> {
                    admin.logout();
                    inAdminMenu = false;
                }
                default -> System.out.println("Invalid choice. Please enter 1, 2, or 3.");
            }
        }
    }

    private static void handleCustomerMenu(Customer customer, ProductManager inventory) {
        Cart cart = new Cart(customer.getUserId());
        OrderHistory history = new OrderHistory(customer.getUserId());

        System.out.println("\n=== Welcome to GPU/CPU Shop ===");
        boolean inCustomerMenu = true;
        mainMenu: while (inCustomerMenu && customer.isLoggedIn()) {
            System.out.println("\n=== Main Menu ===");
            System.out.println("1. View Products");
            System.out.println("2. View Cart");
            System.out.println("3. Checkout");
            System.out.println("4. Profile");
            System.out.println("5. Logout");
            System.out.print("Choose an option: ");
            int choice;
            try {
                choice = Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
                continue;
            }

            switch (choice) {
                case 1:
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
                                System.out.printf("%-8s%-30s%-15s%-12s%-50s%-15s%n", 
                                    "ID", "Name", "Price", "Quantity", "Detail", "Type");
                                System.out.printf("%-8s%-30s%-15s%-12s%-50s%-15s%n", 
                                    "--------", "------------------------------", "---------------", "------------", "--------------------------------------------------", "---------------");
                                for (Product p : filteredProducts) {
                                    System.out.printf("%-8d%-30sRM%-13.2f%-12d%-50s%-15s%n", 
                                        p.getProductId(), p.getName(), p.getPrice(), p.getQuantity(), p.getDetail(), 
                                        p.getClass().getSimpleName());
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
                    break;
                case 2:
                    if (cart.getItems().isEmpty()) {
                        System.out.println("Your cart is empty!");
                        break;
                    }
                    System.out.println(cart);
                    while (true) {
                        System.out.println("\n=== Cart Actions ===");
                        System.out.println("1. Edit Item Quantity (set to 0 to remove)");
                        System.out.println("2. Clear Cart");
                        System.out.println("3. Back to Main Menu");
                        System.out.print ("Choose an action: ");
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
                            break;
                        } else if (cartChoice == 3) {
                            break;
                        } else {
                            System.out.println("Invalid action! Please choose a valid option.");
                        }
                    }
                    break;
                case 3:
                    if (cart.getItems().isEmpty()) {
                        System.out.println("Your cart is empty! Please add items before checking out.");
                        continue;
                    }
                    System.out.println("\n=== Order Confirmation ===");
                    Order order;
                    try {
                        order = new Order(cart, customer);
                    } catch (RuntimeException e) {
                        System.out.println("Error creating order: " + e.getMessage());
                        continue;
                    }
                    System.out.println(order);

                    while (true) {
                        System.out.print("Confirm order? (y/n): ");
                        String confirmInput = scanner.nextLine().trim().toLowerCase();
                        if (confirmInput.equals("y")) {
                            break;
                        } else if (confirmInput.equals("n")) {
                            order.setPaymentStatus("Cancelled");
                            history.addOrder(order);
                            System.out.println("Order cancelled. Returning to menu.");
                            continue mainMenu;
                        } else {
                            System.out.println("Invalid input. Please enter 'y' or 'n'.");
                        }
                    }

                    paymentOptions: while (true) {
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
                                System.out.println("2. Pay on Delivery");
                                System.out.println("3. Back to Payment Options");
                                System.out.print("Choose a payment method (1-3): ");
                                int paymentChoice;
                                try {
                                    paymentChoice = Integer.parseInt(scanner.nextLine());
                                } catch (NumberFormatException e) {
                                    System.out.println("Invalid input. Please enter a number (1-3).");
                                    continue;
                                }

                                if (paymentChoice == 3) {
                                    continue paymentOptions;
                                }

                                String paymentMethod;
                                if (paymentChoice == 1) {
                                    paymentMethod = "Online Banking";
                                } else if (paymentChoice == 2) {
                                    paymentMethod = "Pay on Delivery";
                                } else {
                                    System.out.println("Invalid choice. Please select 1, 2, or 3.");
                                    continue;
                                }

                                double paymentAmount = order.getTotal();
                                System.out.println("Payment amount: RM " + String.format("%.2f", paymentAmount));
                                while (true) {
                                    System.out.print("Proceed with payment? (y/n): ");
                                    String paymentConfirmInput = scanner.nextLine().trim().toLowerCase();
                                    if (paymentConfirmInput.equals("y")) {
                                        break;
                                    } else if (paymentConfirmInput.equals("n")) {
                                        continue paymentOptions;
                                    } else {
                                        System.out.println("Invalid input. Please enter 'y' or 'n'.");
                                    }
                                }

                                PaymentMethod paymentProcessor;
                                Map<String, String> details = new HashMap<>();
                                PaymentMethod.PaymentResult result = null;

                                if (paymentMethod.equalsIgnoreCase("Online Banking")) {
                                    paymentProcessor = new OnlineBankingPayment();
                                    boolean validBankDetails = false;
                                    while (!validBankDetails) {
                                        System.out.println("\n=== Select Bank ===");
                                        System.out.println("1. Maybank");
                                        System.out.println("2. CIMB");
                                        System.out.println("3. Public Bank");
                                        System.out.println("4. Back to Payment Options");
                                        System.out.print("Choose a bank (1-4): ");
                                        String input = scanner.nextLine().trim();
                                        if (input.isEmpty()) {
                                            System.out.println("Input cannot be empty. Please select an option (1-4).");
                                            continue;
                                        }
                                        int bankChoice;
                                        try {
                                            bankChoice = Integer.parseInt(input);
                                        } catch (NumberFormatException e) {
                                            System.out.println("Invalid input. Please enter a number (1-4).");
                                            continue;
                                        }

                                        if (bankChoice == 4) {
                                            continue paymentOptions;
                                        }

                                        String bankName;
                                        if (bankChoice == 1) {
                                            bankName = "Maybank";
                                        } else if (bankChoice == 2) {
                                            bankName = "CIMB";
                                        } else if (bankChoice == 3) {
                                            bankName = "Public Bank";
                                        } else {
                                            System.out.println("Invalid choice. Please select 1, 2, 3 or 4.");
                                            continue;
                                        }

                                        System.out.print("Enter bank username: ");
                                        String bankUsername = scanner.nextLine().trim();
                                        if (bankUsername.isEmpty()) {
                                            System.out.println("Bank username cannot be empty! Returning to bank selection.");
                                            continue;
                                        }

                                        System.out.print("Enter bank password: ");
                                        String bankPassword = scanner.nextLine().trim();
                                        if (bankPassword.isEmpty()) {
                                            System.out.println("Bank password cannot be empty! Returning to bank selection.");
                                            continue;
                                        }

                                        details.put("bankName", bankName);
                                        details.put("bankUsername", bankUsername);
                                        details.put("bankPassword", bankPassword);
                                        result = paymentProcessor.processPayment(order, customer, paymentAmount, details);

                                        try {
                                            if (result.isSuccess()) {
                                                order.setPaymentStatus("Completed");
                                                order.setPaymentMethod("online_banking");
                                                order.setBankName(details.get("bankName"));
                                                order.setBankUsername(details.get("bankUsername"));
                                                order.setBankDiscount(((OnlineBankingPayment.PaymentResult) result).getBankDiscount());
                                                history.addOrder(order);
                                                if (order.getTotal() >= 5000 && userManager.upgradeToMember(customer.getUsername())) {
                                                    System.out.println("Congratulations! Your order total exceeds RM5000 and you have been automatically upgraded to a member. You can enjoy a 10% member discount on your next purchase!");
                                                    User updatedUser = User.getUsers().get(customer.getUsername().toLowerCase());
                                                    if (updatedUser instanceof Customer) {
                                                        customer = (Customer) updatedUser;
                                                    }
                                                }
                                                System.out.println("\n" + result.getInvoice().toFormattedString());
                                                cart.clearCart();
                                                paymentCompleted = true;
                                                validBankDetails = true;
                                                break paymentOptions;
                                            } else {
                                                System.out.println("Payment failed: " + result.getMessage());
                                                System.out.println("Please try again.");
                                            }
                                        } catch (RuntimeException e) {
                                            System.out.println("Error processing payment or saving order: " + e.getMessage());
                                            System.out.println("Please try again.");
                                        }
                                    }
                                } else if (paymentMethod.equalsIgnoreCase("Pay on Delivery")) {
                                    paymentProcessor = new PODPayment();
                                    boolean podPaymentCompleted = false;
                                    while (!podPaymentCompleted) {
                                        System.out.println("\n=== Pay on Delivery ===");
                                        System.out.print("Enter delivery address: ");
                                        String deliveryAddress = scanner.nextLine().trim();
                                        System.out.print("Enter contact information (10 to 15 digit phone number): ");
                                        String contactInfo = scanner.nextLine().trim();
                                        details.put("deliveryAddress", deliveryAddress);
                                        details.put("contactInfo", contactInfo);
                                        result = paymentProcessor.processPayment(order, customer, paymentAmount, details);

                                        if (result.isSuccess()) {
                                            order.setPaymentStatus("Pending");
                                            order.setPaymentMethod("pod_payment");
                                            order.setDeliveryAddress(deliveryAddress);
                                            order.setContactInfo(contactInfo);
                                            history.addOrder(order);
                                            System.out.println("\n" + result.getInvoice().toFormattedString());
                                            cart.clearCart();
                                            podPaymentCompleted = true;
                                            paymentCompleted = true;
                                            break paymentOptions;
                                        } else {
                                            System.out.println("Payment failed: " + result.getMessage());
                                        }
                                    }
                                }
                            }
                        } else if (orderChoice == 2) {
                            order.setPaymentStatus("Cancelled");
                            history.addOrder(order);
                            System.out.println("Order cancelled. Items remain in your cart.");
                            break;
                        } else {
                            System.out.println("Invalid action! Please choose 1 or 2.");
                        }
                    }
                    break;
                case 4:
                    boolean inProfileMenu = true;
                    while (inProfileMenu) {
                        System.out.println("\n=== Profile ===");
                        System.out.println("User ID: " + customer.getUserId());
                        System.out.println("Username: " + customer.getUsername());
                        System.out.println("Membership Status: " + (customer instanceof Member ? "Member" : "NonMember"));
                        System.out.println("1. Change Password");
                        System.out.println("2. View Order History");
                        System.out.println("3. Back to Main Menu");
                        System.out.print("Choose an option: ");
                        int profileChoice;
                        try {
                            profileChoice = Integer.parseInt(scanner.nextLine());
                        } catch (NumberFormatException e) {
                            System.out.println("Invalid input. Please enter a number.");
                            continue;
                        }

                        switch (profileChoice) {
                            case 1:
                                System.out.print("Enter new password: ");
                                String newPassword = scanner.nextLine();
                                if (newPassword.isEmpty()) {
                                    System.out.println("Password cannot be empty. Change cancelled.");
                                    continue;
                                }
                                if (newPassword.length() < 5 || newPassword.length() > 12) {
                                    System.out.println("Password must be between 5 and 12 characters. Change cancelled.");
                                    continue;
                                }
                                try {
                                    customer.resetPassword(newPassword);
                                    System.out.println("Password changed successfully.");
                                } catch (IllegalArgumentException e) {
                                    System.out.println("Error: " + e.getMessage());
                                }
                                break;
                            case 2:
                                System.out.println("\n=== Order History ===");
                                try {
                                    String historyStr = history.toString();
                                    System.out.println(historyStr);
                                    if (history.getOrderCount() > 0) {
                                        System.out.print("Enter the order number to view details (1-" + history.getOrderCount() + ", or 0 to go back): ");
                                        int orderIndex;
                                        try {
                                            orderIndex = Integer.parseInt(scanner.nextLine());
                                        } catch (NumberFormatException e) {
                                            System.out.println("Invalid input. Please enter a number.");
                                            continue;
                                        }
                                        if (orderIndex == 0) {
                                            break;
                                        }
                                        Order selectedOrder = history.getOrderByIndex(orderIndex);
                                        if (selectedOrder == null) {
                                            System.out.println("Invalid order number. Please choose a number between 1 and " + history.getOrderCount() + ".");
                                            continue;
                                        }
                                        if (selectedOrder.getPaymentStatus().equals("Cancelled")) {
                                            System.out.println("Order ID " + selectedOrder.getOrderId() + " has been cancelled.");
                                        } else {
                                            Receipt receipt;
                                            if (selectedOrder.getPaymentStatus().equals("Completed")) {
                                                receipt = new Receipt(selectedOrder, customer, selectedOrder.getPaymentMethod(),
                                                                     true, selectedOrder.getBankName(), selectedOrder.getBankUsername(),
                                                                     null, null, null, selectedOrder.getBankDiscount());
                                            } else {
                                                receipt = new Receipt(selectedOrder, customer, "pod_payment", false, null, null,
                                                                     selectedOrder.getDeliveryAddress(), selectedOrder.getContactInfo(),
                                                                     LocalDate.now().plusDays(3));
                                            }
                                            System.out.println("\n" + receipt.toFormattedString());
                                        }
                                    }
                                } catch (RuntimeException e) {
                                    System.out.println("Error loading order history: " + e.getMessage());
                                }
                                break;
                            case 3:
                                inProfileMenu = false;
                                break;
                            default:
                                System.out.println("Invalid option! Please choose 1, 2, or 3.");
                        }
                    }
                    break;
                case 5:
                    customer.logout();
                    inCustomerMenu = false;
                    break;
                default:
                    System.out.println("Invalid option! Please choose a valid option.");
            }
        }
    }
}