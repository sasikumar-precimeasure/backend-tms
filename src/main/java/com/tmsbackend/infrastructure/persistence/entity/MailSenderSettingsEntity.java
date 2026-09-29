package com.tmsbackend.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

// Singleton row (id always 1) - one app-wide sender identity, mirrors the
// legacy senderEmail_Settings single-row table.
@Entity
@Table(name = "mail_sender_settings")
public class MailSenderSettingsEntity {
    @Id
    private Integer id = 1;

    @Column(name = "sender_name", nullable = false)
    private String senderName = "";

    @Column(name = "sender_email", nullable = false)
    private String senderEmail = "";

    @Column(name = "smtp_host", nullable = false)
    private String smtpHost = "";

    @Column(name = "smtp_port", nullable = false)
    private int smtpPort = 587;

    @Column(name = "password_encrypted", nullable = false)
    private String passwordEncrypted = "";

    @Column(name = "enable_ssl", nullable = false)
    private boolean enableSsl = true;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getSenderEmail() {
        return senderEmail;
    }

    public void setSenderEmail(String senderEmail) {
        this.senderEmail = senderEmail;
    }

    public String getSmtpHost() {
        return smtpHost;
    }

    public void setSmtpHost(String smtpHost) {
        this.smtpHost = smtpHost;
    }

    public int getSmtpPort() {
        return smtpPort;
    }

    public void setSmtpPort(int smtpPort) {
        this.smtpPort = smtpPort;
    }

    public String getPasswordEncrypted() {
        return passwordEncrypted;
    }

    public void setPasswordEncrypted(String passwordEncrypted) {
        this.passwordEncrypted = passwordEncrypted;
    }

    public boolean isEnableSsl() {
        return enableSsl;
    }

    public void setEnableSsl(boolean enableSsl) {
        this.enableSsl = enableSsl;
    }
}
