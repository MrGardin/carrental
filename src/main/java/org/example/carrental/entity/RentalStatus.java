package org.example.carrental.entity;

public enum RentalStatus {
    PENDING,        // Ожидает подтверждения
    CONFIRMED,      // Подтверждена менеджером
    ACTIVE,         // Активна (автомобиль выдан)
    COMPLETED,      // Завершена (автомобиль возвращен)
    CANCELLED,      // Отменена
    REJECTED        // Отклонена менеджером
}