package net.javaguides.springboot.model;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;

@Entity
@Table(name="tokens")
public class VerificationToken {
    private static final int EXPIRATION = 60 * 24;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    private String token;

    @OneToOne
    @JoinColumn(name = "tempUser_id", nullable = false)
    private TemporaryUser temporaryUser;

    private Date expiryDate;

    public VerificationToken() {
    }

    public VerificationToken(String token, TemporaryUser temporaryUser) {
        this.token = token;
        this.temporaryUser = temporaryUser;
        this.expiryDate = calculateExpiryDate(15);
    }

    private Date calculateExpiryDate(int expiryTimeInMinutes) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Timestamp(cal.getTime().getTime()));
        cal.add(Calendar.MINUTE, expiryTimeInMinutes);
        return new Date(cal.getTime().getTime());
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }


    public Date getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(Date expiryDate) {
        this.expiryDate = expiryDate;
    }

    public TemporaryUser getTemporaryUser() {
        return temporaryUser;
    }

    public void setTemporaryUser(TemporaryUser temporaryUser) {
        this.temporaryUser = temporaryUser;
    }

    public void setId(int id) {
        this.id = id;
    }
}
