package org.example.carrental.controller;

import org.example.carrental.entity.Car;
import org.example.carrental.service.CarService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/cars")
public class CarController {

    private final CarService carService;

    @Autowired
    public CarController(CarService carService) {
        this.carService = carService;
    }

    // 1. Получить все автомобили
    @GetMapping
    public List<Car> getAllCars() {
        return carService.getAllCars();
    }

    // 2. Получить доступные автомобили
    @GetMapping("/available")
    public List<Car> getAvailableCars() {
        return carService.getAvailableCars();
    }

    // 3. Получить автомобиль по ID - ИСПРАВЛЕНО
    @GetMapping("/{id}")
    public Car getCarById(@PathVariable("id") Long id) {
        return carService.getCarById(id);
    }

    // 4. Поиск по марке - ИСПРАВЛЕНО
    @GetMapping("/search/brand/{brand}")
    public List<Car> getCarsByBrand(@PathVariable("brand") String brand) {
        return carService.getCarsByBrand(brand);
    }

    // 5. Поиск по диапазону цен
    @GetMapping("/search/price")
    public List<Car> getCarsByPriceRange(@RequestParam Double minPrice,
                                         @RequestParam Double maxPrice) {
        return carService.getCarsByPriceRange(minPrice, maxPrice);
    }

    // 6. Аренда автомобиля - ИСПРАВЛЕНО
    @PostMapping("/{id}/rent")
    public ResponseEntity<String> rentCar(@PathVariable("id") Long id) {
        try {
            carService.rentCar(id);
            return ResponseEntity.ok("Автомобиль успешно арендован");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}