package org.example.carrental.entity;

public enum UserRole {
    CLIENT,   // Арендует машины
    MANAGER,  // Добавляет свои машины, видит свои аренды
    ADMIN     // Управляет всей системой
}