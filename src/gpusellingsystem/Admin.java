package gpusellingsystem;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */




/**
 *
 * @author leong
 */
// 继承：Admin继承User
public class Admin extends User {
    public Admin(int userId, String username, String password) {
        super(userId, username, password);
    }

    // 实现抽象方法
    @Override
    public boolean isAdmin() {
        return true;
    }
}
