package gpusellingsystem;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class UserManager {
    public List<Customer> getAllCustomers() {
        List<Customer> customers = new ArrayList<>();
        for (User user : User.getUsers().values()) {
            if (user instanceof Customer) {
                customers.add((Customer) user);
            }
        }
        return customers;
    }

    public boolean deleteCustomer(String username) {
        User user = User.getUsers().get(username.toLowerCase());
        if (user == null || user.isAdmin()) {
            return false;
        }
        User.getUsers().remove(username.toLowerCase());
        User.saveUsersToFile();
        return true;
    }

    public boolean updateCustomer(String oldUsername, String newUsername, String newPassword, boolean isMember) {
        User user = User.getUsers().get(oldUsername.toLowerCase());
        if (user == null || user.isAdmin()) {
            return false;
        }

        String finalUsername = oldUsername;
        if (newUsername != null && !newUsername.trim().isEmpty() && !newUsername.equalsIgnoreCase(oldUsername)) {
            if (User.getUsers().containsKey(newUsername.toLowerCase())) {
                return false;
            }
            finalUsername = newUsername;
        }

        String finalPassword = user.getPassword();
        if (newPassword != null && !newPassword.trim().isEmpty()) {
            finalPassword = newPassword;
        }

        User newUser;
        if (isMember) {
            newUser = new Member(user.getUserId(), finalUsername, finalPassword);
        } else {
            newUser = new NonMember(user.getUserId(), finalUsername, finalPassword);
        }

        if (!finalUsername.equalsIgnoreCase(oldUsername)) {
            User.getUsers().remove(oldUsername.toLowerCase());
        }
        User.getUsers().put(finalUsername.toLowerCase(), newUser);
        User.saveUsersToFile();
        return true;
    }

    public Customer searchCustomer(String username) {
        User user = User.getUsers().get(username.toLowerCase());
        if (user instanceof Customer) {
            return (Customer) user;
        }
        return null;
    }
    
    public boolean upgradeToMember(String username) {
        User user = User.getUsers().get(username.toLowerCase());
        if (user == null || user.isAdmin() || user instanceof Member) {
            return false;
        }
        User newUser = new Member(user.getUserId(), user.getUsername(), user.getPassword());
        User.getUsers().put(username.toLowerCase(), newUser);
        User.saveUsersToFile();
        return true;
    }
}