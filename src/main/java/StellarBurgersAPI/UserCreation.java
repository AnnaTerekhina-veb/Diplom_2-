
package StellarBurgersAPI;

public class UserCreation {
    private String email;
    private String password;
    private String name;

    public UserCreation(String email, String password, String name) {
        this.email = email;
        this.password = password;
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}