package gpusellingsystem;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
    private static Inventory inventory = new Inventory();
    private static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        initializeUsers();
        boolean running = true;

        while (running) {
            System.out.println("\n=== Computer Retail Management System ===");
            System.out.println("1. Login");
            System.out.println("2. Create Account");
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
        User.loadUsersFromFile(); // Load users from file
        // If no users were loaded, initialize default admin users
        if (User.getUsers().isEmpty()) {
            User.getUsers().put("admin_leong", new Admin(User.getNextUserId(), "admin_leong", "adminpass"));
            User.incrementNextUserId();
            User.getUsers().put("admin_yap", new Admin(User.getNextUserId(), "admin_yap", "adminpass"));
            User.incrementNextUserId();
            User.saveUsersToFile(); // Save default users to file, creating user_data folder if needed
        }
    }

    private static User handleLogin() {
        System.out.print("Enter username: ");
        String username = scanner.nextLine();
        System.out.print("Enter password: ");
        String password = scanner.nextLine();

        User user = User.getUsers().get(username);
        if (user == null || !user.login(password)) {
            System.out.println("Login failed. Invalid username or password.");
            return null;
        }

        System.out.println("Logged in as " + username + ".");
        return user;
    }

    private static void handleCreateAccount() {
        System.out.print("Enter new username: ");
        String username = scanner.nextLine();
        if (User.getUsers().containsKey(username)) {
            System.out.println("Username already exists. Please choose a different username.");
            return;
        }

        System.out.print("Enter new password: ");
        String password = scanner.nextLine();
        System.out.print("Do you want to join member? (y/n): ");
        String joinMember = scanner.nextLine().toLowerCase();

        User user;
        if (joinMember.equals("y")) {
            user = new Member(User.getNextUserId(), username, password);
        } else {
            user = new NonMember(User.getNextUserId(), username, password);
        }
        User.incrementNextUserId();
        User.getUsers().put(username, user);
        User.saveUsersToFile(); // Save users to file, ensuring user_data folder exists
        System.out.println("Account created successfully for " + username + " with ID " + (User.getNextUserId() - 1));
    }

    private static void handleAdminMenu(Admin admin) {
        boolean inAdminMenu = true;
        while (inAdminMenu && admin.isLoggedIn()) {
            System.out.println("\n=== Inventory Management System ===");
            System.out.println("1. Add Product");
            System.out.println("2. Remove Product");
            System.out.println("3. Update Product");
            System.out.println("4. Search Product");
            System.out.println("5. View Product Listing");
            System.out.println("6. Logout");
            System.out.print("Enter your choice (1-6): ");

            int choice;
            try {
                choice = Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
                continue;
            }

            switch (choice) {
                case 1:
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
                    if (inventory.addProduct(name, price, quantity, detail)) {
                        System.out.println("Product added successfully.");
                    } else {
                        System.out.println("Failed to add product. Check input values.");
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
                case 4:
                    System.out.print("Enter product name to search: ");
                    String searchName = scanner.nextLine();
                    Product product = inventory.searchProduct(searchName);
                    if (product != null) {
                        System.out.println("Found: " + product);
                    } else {
                        System.out.println("Product with name '" + searchName + "' not found.");
                    }
                    break;
                case 5:
                    System.out.println("\nProduct Listing:");
                    if (inventory.getProducts().isEmpty()) {
                        System.out.println("No products available.");
                    } else {
                        System.out.printf("%-8s%-20s%-15s%-15s%-30s%n", "ID", "Name", "Price", "Quantity", "Detail");
                        System.out.printf("%-8s%-20s%-15s%-15s%-30s%n", "--", "--------------------", "---------------", "---------------", "------------------------------");
                        for (Product p : inventory.getProducts().values()) {
                            System.out.printf("%-8d%-20sRM%-14.1f%-15d%-30s%n", 
                                p.getProductId(), p.getName(), p.getPrice(), p.getQuantity(), p.getDetail());
                        }
                    }
                    break;
                case 6:
                    admin.logout();
                    inAdminMenu = false;
                    break;
                default:
                    System.out.println("Invalid choice. Please enter 1, 2, 3, 4, 5, or 6.");
            }
        }
    }

    private static void handleCustomerMenu(Customer customer, Inventory inventory) {
        Cart cart = new Cart(customer.getUsername());
        OrderHistory history = new OrderHistory();

        System.out.println("=====================================");
        System.out.println("Welcome to GPU/CPU Shop!");
        System.out.println("=====================================");

        boolean inCustomerMenu = true;
        while (inCustomerMenu && customer.isLoggedIn()) {
            System.out.println("\n=== Main Menu ===");
            System.out.println("1. View Products");
            System.out.println("2. View Cart");
            System.out.println("3. Checkout");
            System.out.println("4. View Order History");
            System.out.println("5. Logout");
            System.out.print("Please choose an option: ");
            int choice;
            try {
                choice = Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
                continue;
            }

            if (choice == 1) {
                while (true) {
                    System.out.println("\n=== Available Products ===");
                    List<Product> products = Product.getAllProducts(inventory);
                    if (products.isEmpty()) {
                        System.out.println("No products available!");
                        break;
                    }
                    System.out.printf("%-8s%-20s%-15s%-15s%-30s%-15s%n", 
                        "ID", "Name", "Price", "Quantity", "Detail", "After Discount");
                    System.out.printf("%-8s%-20s%-15s%-15s%-30s%-15s%n", 
                        "--", "--------------------", "---------------", "---------------", "------------------------------", "---------------");
                    for (Product p : products) {
                        double discountedPrice = p.getPrice() * (1 - customer.getDiscount());
                        System.out.printf("%-8d%-20sRM%-14.1f%-15d%-30sRM%-14.1f%n", 
                            p.getProductId(), p.getName(), p.getPrice(), p.getQuantity(), p.getDetail(), discountedPrice);
                    }
                    System.out.println("\n=== Product Actions ===");
                    System.out.println("1. Add Item to Cart");
                    System.out.println("2. Back to Main Menu");
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
                        break;
                    } else {
                        System.out.println("Invalid action! Please choose a valid option.");
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
                        // Store items before clearing to restore stock
                        List<CartItem> itemsToClear = new ArrayList<>(cart.getItems());
                        cart.clearCart();
                        // Restore stock for each item in the cart
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
                // Display the order page with username and cart contents
                System.out.println("\n=== Order Page ===");
                System.out.println("Username: " + customer.getUsername());
                System.out.println("Products in Cart:");
                System.out.println(cart);
                
                // Prompt user to proceed or cancel
                System.out.println("\n=== Order Actions ===");
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
                    // Proceed to payment
                    Order order = new Order(cart, customer);
                    history.addOrder(order);
                    System.out.println("\n=== Order Confirmation ===");
                    System.out.println(order);
                    // Clear the cart (stock was already deducted when items were added to cart)
                    cart.clearCart();
                } else if (orderChoice == 2) {
                    // Cancel the order and restore stock
                    List<CartItem> itemsToRestore = new ArrayList<>(cart.getItems());
                    for (CartItem item : itemsToRestore) {
                        Product product = Product.getProduct(item.getProduct().getProductId(), inventory);
                        if (product != null) {
                            int newStock = product.getQuantity() + item.getQuantity();
                            inventory.updateProduct(product.getProductId(), null, -1, newStock, null);
                        }
                    }
                    System.out.println("Order cancelled. Items have been returned to inventory.");
                    // Cart remains intact, no need to clear it
                } else {
                    System.out.println("Invalid action! Please choose 1 or 2.");
                }
            } else if (choice == 4) {
                System.out.println("\n=== Order History ===");
                if (history.toString().isEmpty()) {
                    System.out.println("You have no past orders.");
                } else {
                    System.out.println(history);
                }
            } else if (choice == 5) {
                customer.logout();
                inCustomerMenu = false;
                break;
            } else {
                System.out.println("Invalid option! Please choose a valid option！");
                
                //!
            }
        }
    }
}