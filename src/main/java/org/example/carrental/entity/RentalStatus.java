package org.example.carrental.entity;

public enum RentalStatus {
    PENDING,        // Ожидает подтверждения
    CONFIRMED,      // Подтверждена
    ACTIVE,         // В процессе
    COMPLETED,      // Завершена
    CANCELLED       // Отменена
}