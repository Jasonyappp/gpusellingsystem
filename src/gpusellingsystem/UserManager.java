package gpusellingsystem;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class UserManager {
    // 获取所有客户（排除管理员）
    public List<Customer> getAllCustomers() {
        List<Customer> customers = new ArrayList<>();
        for (User user : User.getUsers().values()) {
            if (user instanceof Customer) { // 只添加 Customer 类型的用户
                customers.add((Customer) user);
            }
        }
        return customers;
    }

    // 删除客户
    public boolean deleteCustomer(String username) {
        User user = User.getUsers().get(username);
        if (user == null || user.isAdmin()) { // 不允许删除不存在的用户或管理员
            return false;
        }
        User.getUsers().remove(username);
        User.saveUsersToFile(); // 更新 user_data/users.txt
        return true;
    }

    // 修改客户信息
    public boolean updateCustomer(String oldUsername, String newUsername, String newPassword, boolean isMember) {
        User user = User.getUsers().get(oldUsername);
        if (user == null || user.isAdmin()) { // 不允许修改不存在的用户或管理员
            return false;
        }
        if (newUsername != null && !newUsername.trim().isEmpty() && !newUsername.equals(oldUsername)) {
            if (User.getUsers().containsKey(newUsername)) {
                return false; // 新用户名已存在
            }
            User.getUsers().remove(oldUsername); // 删除旧用户名
            user = newUsername.equals(user.getUsername()) ? user : (isMember ? new Member(user.getUserId(), newUsername, user.getPassword()) : new NonMember(user.getUserId(), newUsername, user.getPassword()));
            User.getUsers().put(newUsername, user);
        }
        if (newPassword != null && !newPassword.trim().isEmpty()) {
            user.resetPassword(newPassword);
        }
        if (user instanceof Member && !isMember) {
            // 从 Member 降级为 NonMember
            User newUser = new NonMember(user.getUserId(), user.getUsername(), user.getPassword());
            User.getUsers().put(user.getUsername(), newUser);
        } else if (user instanceof NonMember && isMember) {
            // 从 NonMember 升级为 Member
            User newUser = new Member(user.getUserId(), user.getUsername(), user.getPassword());
            User.getUsers().put(user.getUsername(), newUser);
        }
        User.saveUsersToFile(); // 更新 user_data/users.txt
        return true;
    }

    // 搜索客户
    public Customer searchCustomer(String username) {
        User user = User.getUsers().get(username);
        if (user instanceof Customer) {
            return (Customer) user;
        }
        return null;
    }
}