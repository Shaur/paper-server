package org.home.paper.server.model;

public class Credentials {

    private String email;

    private String password;

    private String salt;

    public Credentials(String email, String password, String salt) {
        this.email = email;
        this.password = password;
        this.salt = salt;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }
}
