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
    private int failedLoginAttempts; // 密码错误次数（不持久化）
    private long lockoutTimestamp; // 锁定时间戳（不持久化）
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

    // Static method to load users from file（保持原样）
    public static void loadUsersFromFile() {
        users.clear();
        File file = new File(USERS_FILE_PATH);
        if (!file.exists()) {
            return;
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length != 5) {
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
                        if (isMember) {
                            user = new Member(userId, username, password);
                        } else {
                            user = new NonMember(userId, username, password);
                        }
                    }
                    users.put(username, user);
                    if (userId >= nextUserId) {
                        nextUserId = userId + 1;
                    }
                } catch (NumberFormatException e) {
                    continue;
                }
            }
        } catch (IOException e) {
            // Silently handle IOException
        }
    }

    // Static method to save users to file（保持原样）
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

    // Method to increment nextUserId
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

    // 检查是否被锁定
    public boolean isLockedOut() {
        if (lockoutTimestamp == 0) {
            return false;
        }
        long currentTime = System.currentTimeMillis();
        if (currentTime >= lockoutTimestamp + 60_000) {
            lockoutTimestamp = 0; // 自动解锁
            failedLoginAttempts = 0;
            return false;
        }
        return true;
    }

    // 获取剩余锁定时间（秒）
    public long getRemainingLockoutSeconds() {
        if (!isLockedOut()) {
            return 0;
        }
        long currentTime = System.currentTimeMillis();
        return (lockoutTimestamp + 60_000 - currentTime) / 1000;
    }

    // 管理员认证
    public static boolean authenticateAdmin(String adminUsername, String adminPassword) {
        User admin = users.get(adminUsername);
        if (admin == null || !admin.isAdmin()) {
            return false;
        }
        return admin.getPassword().equals(adminPassword);
    }

    // 登录方法，包含错误计数和锁定逻辑
    public boolean login(String inputPassword) {
        if (isLockedOut()) {
            return false; // 锁定状态由Main.handleLogin处理
        }

        if (this.password.equals(inputPassword)) {
            this.isLoggedIn = true;
            this.failedLoginAttempts = 0; // 登录成功，重置错误计数
            return true;
        } else {
            this.failedLoginAttempts++;
            if (this.failedLoginAttempts >= 3) {
                this.lockoutTimestamp = System.currentTimeMillis(); // 设置锁定时间
            }
            return false;
        }
    }

    // 重置密码
    public void resetPassword(String newPassword) {
        this.password = newPassword;
        this.failedLoginAttempts = 0;
        this.lockoutTimestamp = 0;
        saveUsersToFile(); // 更新文件中的密码
    }

    public void logout() {
        this.isLoggedIn = false;
    }

    public abstract boolean isAdmin();

    public double getDiscount() {
        return 0.0;
    }
}