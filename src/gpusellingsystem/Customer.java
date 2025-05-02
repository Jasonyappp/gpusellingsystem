package gpusellingsystem;

public class Customer extends User {
    private boolean isMember;

    public Customer(int userId, String username, String password, boolean isMember) {
        super(userId, username, password);
        this.isMember = isMember;
    }

    public boolean isMember() {
        return isMember;
    }

    public void setMember(boolean isMember) {
        this.isMember = isMember;
    }

    @Override
    public boolean isAdmin() {
        return false;
    }

    @Override
    public double getDiscount() {
        return isMember ? 0.1 : 0.0;
    }

    @Override
    public String toString() {
        return String.format("Customer [ID: %d, Username: %s, Member: %s, Discount: %.1f%%]",
                getUserId(), getUsername(), isMember ? "Yes" : "No", getDiscount() * 100);
    }
}