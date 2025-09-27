package org.example.carrental.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class RentalRequest {
    private Long carId; // Должно быть заполнено
    private LocalDateTime startDate;
    private LocalDateTime endDate;

    // Добавьте конструкторы если их нет
    public RentalRequest() {}

    public RentalRequest(Long carId, LocalDateTime startDate, LocalDateTime endDate) {
        this.carId = carId;
        this.startDate = startDate;
        this.endDate = endDate;
    }
}