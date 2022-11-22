package net.javaguides.springboot.web.dto;

public class UserEmailDto
{
    private String email;

    public UserEmailDto(){}

    public UserEmailDto(String email)
    {
        this.email = email;
    }

    public String getEmail()
    {
        return email;
    }

    public void setEmail(String email)
    {
        this.email = email;
    }
}
