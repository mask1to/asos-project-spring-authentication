package net.javaguides.springboot.web.dto;

public class SecretCode {
    private String secret_code;
    private String email;
    public SecretCode(String secret_code, String email) {
        this.secret_code = secret_code;
        this.email = email;
    }
    public SecretCode(String secret_code) {
        this.secret_code = secret_code;
    }
    public SecretCode(){}

    public String getSecret_code() {
        return secret_code;
    }
    public void setSecret_code(String secret_code) {
        this.secret_code = secret_code;
    }
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

}
