package gpusellingsystem;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public abstract class User {
    private int userId;
    private String username;
    private String password;
    private boolean isLoggedIn;
    private int failedLoginAttempts;
    private long lockoutTimestamp;
    private static Map<String, User> users = new HashMap<>();
    private static int nextUserId = 1;
    private static final String USERS_FILE_PATH = "user_data/users.txt";

    public User(int userId, String username, String password) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.isLoggedIn = false;
        this.failedLoginAttempts = 0;
        this.lockoutTimestamp = 0;
    }

    public static void loadUsersFromFile() {
        File file = new File(USERS_FILE_PATH);
        if (!file.exists()) {
            System.err.println("Users file not found: " + USERS_FILE_PATH);
            return;
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length != 5) {
                    System.err.println("Invalid user line format in users.txt: " + line);
                    continue;
                }
                try {
                    int userId = Integer.parseInt(parts[0]);
                    String username = parts[1];
                    String password = parts[2];
                    boolean isAdmin = Boolean.parseBoolean(parts[3]);
                    boolean isMember = Boolean.parseBoolean(parts[4]);
                    
                    User user;
                    if (isAdmin) {
                        user = new Admin(userId, username, password);
                    } else {
                        user = new Customer(userId, username, password, isMember);
                    }
                    users.put(username.toLowerCase(), user);
                    if (userId >= nextUserId) {
                        nextUserId = userId + 1;
                    }
                } catch (NumberFormatException e) {
                    System.err.println("Skipping invalid user line in users.txt: " + line);
                    continue;
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading users.txt: " + e.getMessage());
        }
    }

    public static void saveUsersToFile() {
        try {
            File dir = new File("user_data");
            if (!dir.exists()) {
                if (!dir.mkdirs()) {
                    return;
                }
            }

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(USERS_FILE_PATH))) {
                for (User user : users.values()) {
                    boolean isAdmin = user.isAdmin();
                    boolean isMember = user instanceof Customer && ((Customer) user).isMember();
                    writer.write(String.format("%d,%s,%s,%b,%b",
                            user.getUserId(), user.getUsername(), user.getPassword(), isAdmin, isMember));
                    writer.newLine();
                }
            }
        } catch (IOException e) {
            System.err.println("Error writing to users.txt: " + e.getMessage());
        }
    }

    public static Map<String, User> getUsers() {
        return users;
    }

    public static int getNextUserId() {
        return nextUserId;
    }

    public static void incrementNextUserId() {
        nextUserId++;
    }

    public int getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public boolean isLoggedIn() {
        return isLoggedIn;
    }

    public int getFailedLoginAttempts() {
        return failedLoginAttempts;
    }

    public boolean isLockedOut() {
        if (lockoutTimestamp == 0) {
            return false;
        }
        long currentTime = System.currentTimeMillis();
        if (currentTime >= lockoutTimestamp + 60_000) {
            lockoutTimestamp = 0;
            failedLoginAttempts = 0;
            return false;
        }
        return true;
    }

    public long getRemainingLockoutSeconds() {
        if (!isLockedOut()) {
            return 0;
        }
        long currentTime = System.currentTimeMillis();
        return (lockoutTimestamp + 60_000 - currentTime) / 1000;
    }

    public static boolean authenticateAdmin(String adminUsername, String adminPassword) {
        User admin = users.get(adminUsername.toLowerCase());
        if (admin == null || !admin.isAdmin()) {
            return false;
        }
        return admin.getPassword().equals(adminPassword);
    }

    public boolean login(String inputPassword) {
        if (isLockedOut()) {
            return false;
        }

        if (this.password.equals(inputPassword)) {
            this.isLoggedIn = true;
            this.failedLoginAttempts = 0;
            return true;
        } else {
            this.failedLoginAttempts++;
            if (this.failedLoginAttempts >= 3) {
                this.lockoutTimestamp = System.currentTimeMillis();
            }
            return false;
        }
    }

    public void resetPassword(String newPassword) {
        if (newPassword.isEmpty()) {
            throw new IllegalArgumentException("Password cannot be empty.");
        }
        if (newPassword.equals(this.password)) {
            throw new IllegalArgumentException("New password cannot be the same as the old password.");
        }
        this.password = newPassword;
        this.failedLoginAttempts = 0;
        this.lockoutTimestamp = 0;
        saveUsersToFile();
    }

    public void logout() {
        this.isLoggedIn = false;
    }

    public abstract boolean isAdmin();

    public double getDiscount() {
        return 0.0;
    }
}