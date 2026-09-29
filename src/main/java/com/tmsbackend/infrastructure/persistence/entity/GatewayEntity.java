package com.tmsbackend.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "gateways")
public class GatewayEntity {
    @Id
    private String id;

    @Column(name = "transformer_id", nullable = false)
    private String transformerId;

    @Column(nullable = false)
    private String name;

    @Column(name = "client_id", nullable = false)
    private int clientId;

    @Column(name = "ip_address", nullable = false)
    private String ipAddress;

    @Column(nullable = false)
    private int port;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTransformerId() {
        return transformerId;
    }

    public void setTransformerId(String transformerId) {
        this.transformerId = transformerId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getClientId() {
        return clientId;
    }

    public void setClientId(int clientId) {
        this.clientId = clientId;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
}
