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

        // 确定最终用户名
        String finalUsername = oldUsername;
        if (newUsername != null && !newUsername.trim().isEmpty() && !newUsername.equals(oldUsername)) {
            if (User.getUsers().containsKey(newUsername)) {
                return false; // 新用户名已存在
            }
            finalUsername = newUsername;
        }

        // 更新密码
        String finalPassword = user.getPassword();
        if (newPassword != null && !newPassword.trim().isEmpty()) {
            finalPassword = newPassword;
        }

        // 创建新用户对象，基于会员状态
        User newUser;
        if (isMember) {
            newUser = new Member(user.getUserId(), finalUsername, finalPassword);
        } else {
            newUser = new NonMember(user.getUserId(), finalUsername, finalPassword);
        }

        // 更新用户映射
        if (!finalUsername.equals(oldUsername)) {
            User.getUsers().remove(oldUsername);
        }
        User.getUsers().put(finalUsername, newUser);
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
    
    public boolean upgradeToMember(String username){
        User user = User.getUsers().get(username);
        if (user == null || user.isAdmin() || user instanceof Member) {
            return false;
        }
        User newUser = new Member(user.getUserId(), user.getUsername(), user.getPassword());
        User.getUsers().put(username, newUser);
        User.saveUsersToFile();
        return true;
    }
}