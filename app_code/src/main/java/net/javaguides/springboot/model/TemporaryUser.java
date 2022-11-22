package net.javaguides.springboot.model;

import javax.persistence.*;

@Entity
@Table(name="temp_users", uniqueConstraints={@UniqueConstraint(columnNames={"email"})})
public class TemporaryUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "email")
    private String email;

    @Column(name = "enabled")
    private boolean enabled;

    @OneToOne(cascade = CascadeType.ALL,mappedBy="temporaryUser")
    private VerificationToken verificationToken;

    public TemporaryUser() {

    }

    public TemporaryUser(int id, String email, boolean enabled) {
        this.id = id;
        this.email = email;
        this.enabled = false;
    }

    public TemporaryUser(String email) {
        super();
        this.email = email;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public String toString()
    {
        return "TemporaryUser{" +
                "id=" + id +
                ", email='" + email + '\'' +
                ", enabled=" + enabled +
                '}';
    }
}
