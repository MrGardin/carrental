package org.example.carrental.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false, length = 100)
    private String password;

    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    @Column(name = "phone", length = 20)
    private String phone;

    // Только для клиентов (у менеджера и админа будет null)
    @Column(name = "driver_license", unique = true, length = 20)
    private String driverLicense;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "approved", nullable = false)
    private Boolean approved = true; // для клиентов сразу true

    public User(String email, String password, String fullName, UserRole role) {
        this.email = email;
        this.password = password;
        this.fullName = fullName;
        this.role = role;
        // driverLicense = null, phone = null - админу не нужны
    }

    // В конструкторе клиента
    public User(String email, String password, String fullName, String phone, String driverLicense) {
        this.email = email;
        this.password = password;
        this.fullName = fullName;
        this.phone = phone;
        this.driverLicense = driverLicense;
        this.role = UserRole.CLIENT;
        this.approved = true; // клиенты одобрены сразу
    }

    // В конструкторе менеджера
    public User(String email, String password, String fullName, String phone) {
        this.email = email;
        this.password = password;
        this.fullName = fullName;
        this.phone = phone;
        this.role = UserRole.MANAGER;
        this.approved = false; // менеджеры ждут одобрения
    }

    public boolean isApproved() {
        return Boolean.TRUE.equals(approved);
    }

    public boolean isAdmin() {
        return UserRole.ADMIN.equals(role);
    }

    public boolean isManager() {
        return UserRole.MANAGER.equals(role);
    }

    public boolean isClient() {
        return UserRole.CLIENT.equals(role);
    }

    // Геттеры для Thymeleaf
    public Boolean getManager() { return isManager(); }
    public Boolean getAdmin() { return isAdmin(); }
    public Boolean getClient() { return isClient(); }
}