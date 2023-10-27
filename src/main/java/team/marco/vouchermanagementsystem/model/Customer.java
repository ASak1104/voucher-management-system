package team.marco.vouchermanagementsystem.model;

import java.time.LocalDateTime;
import java.util.UUID;

import static java.text.MessageFormat.format;

public class Customer {
    private final UUID id;
    private final String name;
    private final String email;
    private final LocalDateTime createdAt;

    public Customer(UUID id, String name, String email, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.createdAt = createdAt;
    }

    public Customer(String name, String email) {
        this.id = UUID.randomUUID();
        this.email = email;
        this.name = name;
        this.createdAt = LocalDateTime.now();
    }

    public String getInfo() {
        return format("id: {0}, 고객명: {1} ", id, name);
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
