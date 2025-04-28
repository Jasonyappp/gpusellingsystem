/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package gpusellingsystem;

/**
 *
 * @author chong
 */
public class NonMember extends Customer {
    public NonMember(int userId, String username, String password) {
        super(userId, username, password);
    }

    @Override
    public boolean isMember() {
        return false;
    }
}
