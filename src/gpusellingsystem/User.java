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
    private static Map<String, User> users = new HashMap<>();
    private static int nextUserId = 1;
    private static final String USERS_FILE_PATH = "user_data/users.txt";

    public User(int userId, String username, String password) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.isLoggedIn = false;
    }

    // Static method to load users from file
    public static void loadUsersFromFile() {
        users.clear(); // Clear current users to avoid duplicates
        File file = new File(USERS_FILE_PATH);
        if (!file.exists()) {
            return; // File doesn't exist, will initialize with default users
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length != 5) {
                    continue; // Skip malformed lines
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
                        if (isMember) {
                            user = new Member(userId, username, password);
                        } else {
                            user = new NonMember(userId, username, password);
                        }
                    }
                    users.put(username, user);
                    // Update nextUserId to be higher than the largest userId
                    if (userId >= nextUserId) {
                        nextUserId = userId + 1;
                    }
                } catch (NumberFormatException e) {
                    continue; // Skip lines with parsing errors
                }
            }
        } catch (IOException e) {
            // Silently handle IOException
        }
    }

    // Static method to save users to file
    public static void saveUsersToFile() {
        try {
            // Create user_data folder if it doesn't exist
            File dir = new File("user_data");
            if (!dir.exists()) {
                if (!dir.mkdirs()) {
                    return; // Silently handle directory creation failure
                }
            }

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(USERS_FILE_PATH))) {
                for (User user : users.values()) {
                    boolean isAdmin = user.isAdmin();
                    boolean isMember = user instanceof Member;
                    writer.write(String.format("%d,%s,%s,%b,%b",
                            user.getUserId(), user.getUsername(), user.getPassword(), isAdmin, isMember));
                    writer.newLine();
                }
            }
        } catch (IOException e) {
            // Silently handle IOException
        }
    }

    // Getter for the users map
    public static Map<String, User> getUsers() {
        return users;
    }

    // Getter for nextUserId
    public static int getNextUserId() {
        return nextUserId;
    }

    // Method to increment nextUserId (used when creating a new user)
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

    public boolean login(String inputPassword) {
        if (this.password.equals(inputPassword)) {
            this.isLoggedIn = true;
            return true;
        }
        return false;
    }

    public void logout() {
        this.isLoggedIn = false;
    }

    public abstract boolean isAdmin();

    public double getDiscount() {
        return 0.0; // 默认无折扣
    }
}