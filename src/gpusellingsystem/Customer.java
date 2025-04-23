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
}

// 继承：Member继承Customer
class Member extends Customer {
    public Member(int userId, String username, String password) {
        super(userId, username, password);
    }

    // 多态：覆盖getDiscount方法
    @Override
    public double getDiscount() {
        return 0.10; // 会员10%折扣
    }
}

// 继承：NonMember继承Customer
class NonMember extends Customer {
    public NonMember(int userId, String username, String password) {
        super(userId, username, password);
    }

    // 多态：覆盖getDiscount方法
    @Override
    public double getDiscount() {
        return 0.0; // 非会员无折扣
    }
}
