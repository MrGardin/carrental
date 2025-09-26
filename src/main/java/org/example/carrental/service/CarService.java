package org.example.carrental.service;

import org.example.carrental.entity.Car;
import org.example.carrental.entity.User;
import org.example.carrental.repository.CarRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class CarService {

    private final CarRepository carRepository;

    @Autowired
    public CarService(CarRepository carRepository) {
        this.carRepository = carRepository;
    }

    public Car addCar(Car car, User manager) {
        // 1. ПРОВЕРКА ПРАВ ДОСТУПА (бизнес-правило)
        if (!manager.isManager() && !manager.isAdmin()) {
            throw new RuntimeException("Только менеджеры и администраторы могут добавлять автомобили!");
        }

        // 2. ПРОВЕРКА УНИКАЛЬНОСТИ VIN (бизнес-правило)
        Optional<Car> existingCar = carRepository.findByVin(car.getVin());
        if (existingCar.isPresent()) {
            throw new RuntimeException("Автомобиль с VIN " + car.getVin() + " уже существует!");
        }

        // 3. ПРАВИЛА ДЛЯ АВТОМОБИЛЕЙ МЕНЕДЖЕРА (бизнес-правило)
        if (car.getPricePerDay() > 1000) {
            throw new RuntimeException("Слишком высокая цена для аренды! Максимум 1000 руб/день");
        }

        // 4. УСТАНОВКА ВЛАДЕЛЬЦА (бизнес-логика)
        car.setManager(manager);
        car.setAvailable(true);

        return carRepository.save(car);
    }
/*
    public Car rentCar(Long carId, User client) {
        // 1. КЛИЕНТЫ НЕ МОГУТ АРЕНДОВАТЬ (бизнес-правило)
        if (!client.isClient()) {
            throw new RuntimeException("Только клиенты могут арендовать автомобили!");
        }

        Car car = getCarById(carId);

        // 2. ПРОВЕРКА ДОСТУПНОСТИ (бизнес-правило)
        if (!car.getAvailable()) {
            throw new RuntimeException("Автомобиль уже арендован!");
        }

        // 3. ВОЗРАСТНЫЕ ОГРАНИЧЕНИЯ (бизнес-правило)
        if (car.getYear() != null && car.getYear() < 2010) {
            throw new RuntimeException("Нельзя арендовать автомобили старше 2010 года!");
        }

        // 4. ИЗМЕНЕНИЕ СТАТУСА (бизнес-логика)
        car.setAvailable(false);
        return carRepository.save(car);
    }

 */

    // Метод для поиска автомобиля по ID
    public Car getCarById(Long id) {
        return carRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Автомобиль не найден с ID: " + id));
    }

    // Остальные методы бизнес-логики
    public List<Car> getAvailableCars() {
        return carRepository.findByAvailableTrue();
    }

    public List<Car> getCarsByBrand(String brand) {
        if (brand == null || brand.trim().isEmpty()) {
            throw new RuntimeException("Марка автомобиля не может быть пустой!");
        }
        return carRepository.findByBrand(brand);
    }

    public List<Car> getCarsByPriceRange(Double minPrice, Double maxPrice) {
        if (minPrice < 0 || maxPrice < 0) {
            throw new RuntimeException("Цена не может быть отрицательной!");
        }
        if (minPrice > maxPrice) {
            throw new RuntimeException("Минимальная цена не может быть больше максимальной!");
        }
        return carRepository.findByPricePerDayBetween(minPrice, maxPrice);
    }

    public List<Car> getCarsByManager(User manager) {
        return carRepository.findByManager(manager);
    }

    public Car updateCarAvailability(Long carId, Boolean available) {
        Car car = getCarById(carId);
        car.setAvailable(available);
        return carRepository.save(car);
    }
    // В CarService добавить:
    public List<Car> getAllCars() {
        return carRepository.findAll();
    }

    public void rentCar(Long carId) {
        Car car = carRepository.findById(carId)
                .orElseThrow(() -> new RuntimeException("Автомобиль не найден"));

        if (!car.getAvailable()) {
            throw new RuntimeException("Автомобиль уже арендован");
        }

        car.setAvailable(false);
        carRepository.save(car);

        // Здесь позже добавим создание записи в Rental
        System.out.println("Автомобиль " + car.getBrand() + " " + car.getModel() + " арендован");
    }
    public Car updateCarPhoto(Long carId, String imageUrl) {
        Car car = getCarById(carId);
        car.setImageUrl(imageUrl);
        return carRepository.save(car);
    }
}