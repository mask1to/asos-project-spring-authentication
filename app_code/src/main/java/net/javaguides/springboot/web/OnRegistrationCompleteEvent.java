package net.javaguides.springboot.web;

import net.javaguides.springboot.model.TemporaryUser;
import org.springframework.context.ApplicationEvent;

import java.util.Locale;

public class OnRegistrationCompleteEvent extends ApplicationEvent {

    private String appUrl;
    private Locale locale;
    private TemporaryUser temporaryUser;

    public OnRegistrationCompleteEvent(
            TemporaryUser temporaryUser, Locale locale, String appUrl) {
        super(temporaryUser);

        this.temporaryUser = temporaryUser;
        this.locale = locale;
        this.appUrl = appUrl;
    }

    public String getAppUrl() {
        return appUrl;
    }

    public void setAppUrl(String appUrl) {
        this.appUrl = appUrl;
    }

    public Locale getLocale() {
        return locale;
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    public TemporaryUser getTemporaryUser() {
        return temporaryUser;
    }

    public void setTemporaryUser(TemporaryUser temporaryUser) {
        this.temporaryUser = temporaryUser;
    }
}
