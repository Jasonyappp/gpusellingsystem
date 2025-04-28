/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package gpusellingsystem;

/**
 *
 * @author leong
 */

// 继承：Customer继承User
public abstract class Customer extends User {
    public Customer(int userId, String username, String password) {
        super(userId, username, password);
    }

    @Override
    public boolean isAdmin() {
        return false;
    }
    public abstract boolean isMember();
}