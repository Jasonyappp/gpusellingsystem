package gpusellingsystem;


public abstract class User {
    // 封装：私有属性通过公共方法访问
    private int userId;
    private String username;
    private String password;
    private boolean isLoggedIn;

    public User(int userId, String username, String password) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.isLoggedIn = false;
    }

    // 封装：提供公共方法访问私有属性
    public String getUsername() {
        return username;
    }

    public boolean isLoggedIn() {
        return isLoggedIn;
    }

    // 登录方法
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

    // 抽象：子类必须实现
    public abstract boolean isAdmin();

    // 多态：子类可以覆盖
    public double getDiscount() {
        return 0.0; // 默认无折扣
    }
}
