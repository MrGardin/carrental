package org.example.carrental.dto;

import lombok.Data;

@Data
public class CarFilterDTO {
    private String brand;
    private Double minPrice;
    private Double maxPrice;
    private String bodyType;
    private String fuelType;
    private String transmission;
    private Integer minYear;
    private Integer maxYear;
    private Boolean available;

    public CarFilterDTO() {}
}