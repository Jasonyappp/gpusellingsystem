/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package gpusellingsystem;

public class Member extends Customer {
    public Member(int userId, String username, String password) {
        super(userId, username, password);
    }
    
    @Override
    public double getDiscount() {
        return 0.1; // 10% 会员折扣
    }

    @Override
    public boolean isMember() {
        return true;
    }
    
    
}
//test