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
    @Column(name = "total_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalPrice;

    // Статус аренды
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RentalStatus status = RentalStatus.PENDING;

    // Дата создания
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    // === НОВЫЕ ПОЛЯ ДЛЯ ФУНКЦИОНАЛА МЕНЕДЖЕРА ===

    // Фактическая дата окончания (может отличаться от плановой)
    @Column(name = "actual_end_date")
    private LocalDateTime actualEndDate;

    // Фактическая стоимость (может отличаться от плановой)
    @Column(name = "actual_price", precision = 10, scale = 2)
    private BigDecimal actualPrice;

    // Причина отклонения аренды
    @Column(name = "rejection_reason", length = 500)
    private String rejectionReason;

    // Дата подтверждения менеджером
    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    // Дата начала фактической аренды
    @Column(name = "actual_start_date")
    private LocalDateTime actualStartDate;

    // Комментарий менеджера
    @Column(name = "manager_notes", length = 1000)
    private String managerNotes;

    // Простой конструктор
    public Rental(User user, Car car, LocalDateTime startDate, LocalDateTime endDate, BigDecimal totalPrice) {
        this.user = user;
        this.car = car;
        this.startDate = startDate;
        this.endDate = endDate;
        this.totalPrice = totalPrice;
    }

    // === ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ===

    // Получить продолжительность аренды в днях
    public long getPlannedDurationDays() {
        return java.time.Duration.between(startDate, endDate).toDays();
    }

    // Получить фактическую продолжительность в днях
    public long getActualDurationDays() {
        if (actualStartDate != null && actualEndDate != null) {
            return java.time.Duration.between(actualStartDate, actualEndDate).toDays();
        }
        return getPlannedDurationDays();
    }

    // Проверить является ли аренда активной
    public boolean isActive() {
        return status == RentalStatus.ACTIVE || status == RentalStatus.CONFIRMED;
    }

    // Проверить является ли аренда завершенной
    public boolean isCompleted() {
        return status == RentalStatus.COMPLETED || status == RentalStatus.CANCELLED;
    }

    // Проверить требует ли аренда внимания менеджера
    public boolean requiresAttention() {
        return status == RentalStatus.PENDING ||
                (isActive() && startDate.isBefore(LocalDateTime.now()));
    }
}