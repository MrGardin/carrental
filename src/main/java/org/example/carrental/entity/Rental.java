package org.example.carrental.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.math.BigDecimal;

@Entity
@Table(name = "rentals")
@Data
@NoArgsConstructor
public class Rental {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Кто арендует
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Какой автомобиль
    @ManyToOne
    @JoinColumn(name = "car_id", nullable = false)
    private Car car;

    // Даты аренды
    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDateTime endDate;

    // Стоимость
    @Column(name = "total_price", nullable = false)
    private BigDecimal totalPrice;

    // Статус аренды
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RentalStatus status = RentalStatus.PENDING;

    // Дата создания
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    // Простой конструктор
    public Rental(User user, Car car, LocalDateTime startDate, LocalDateTime endDate, BigDecimal totalPrice) {
        this.user = user;
        this.car = car;
        this.startDate = startDate;
        this.endDate = endDate;
        this.totalPrice = totalPrice;
    }
}