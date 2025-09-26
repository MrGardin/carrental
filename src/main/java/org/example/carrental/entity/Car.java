package org.example.carrental.entity;

import jakarta.persistence.*; //JPA
import lombok.NoArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "cars")
@Data
@NoArgsConstructor
@AllArgsConstructor

public class Car {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String brand;           // Марка: Toyota, BMW, etc.

    @Column(nullable = false, length = 50)
    private String model;           // Модель: Camry, X5, etc.

    @Column(name = "manufacture_year")
    private Integer year;           // Год выпуска

    @Column(length = 30)
    private String color;           // Цвет

    @Column(name = "price_per_day", nullable = false)
    private Double pricePerDay;     // Цена за день аренды

    @Column(nullable = false)
    private Boolean available = true; // Доступен для аренды

    @Column(unique = true, length = 17)
    private String vin;             // VIN номер (уникальный)

    @Column(name = "fuel_type")
    private String fuelType;        // Тип топлива: бензин, дизель, электро, гибрид

    @Column(name = "transmission_type")
    private String transmission;    // Трансмиссия: автоматическая, механическая, робот, вариатор

    @Column(name = "body_type")
    private String bodyType;        // Тип кузова: седан, универсал, хэтчбек, внедорожник, купе, кабриолет

    @Column(name = "horse_power")
    private Integer horsePower;     // Мощность в л.с.

    @Column(name = "mileage")
    private Integer mileage;        // Пробег в км

    @Column(name = "engine_capacity")
    private Double engineCapacity;  // Объем двигателя в литрах (1.6, 2.0, 3.0)

    @Column(name = "image_url")
    private String imageUrl;        // URL фотографии автомобиля

    @Column(name = "description", length = 1000)
    private String description;     // Описание автомобиля

    @ManyToOne
    @JoinColumn(name = "manager_id")
    private User manager;

    // Конструктор для удобного создания
    public Car(String brand, String model, Integer year, Double pricePerDay,
               String fuelType, String transmission, String bodyType) {
        this.brand = brand;
        this.model = model;
        this.year = year;
        this.pricePerDay = pricePerDay;
        this.fuelType = fuelType;
        this.transmission = transmission;
        this.bodyType = bodyType;
        this.available = true;
    }

    // Метод для отображения полного названия
    public String getFullName() {
        return brand + " " + model + " (" + year + ")";
    }
}
