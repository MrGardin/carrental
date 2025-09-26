package org.example.carrental.repository;

import org.example.carrental.entity.Car;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.example.carrental.entity.User;

import java.util.List;
import java.util.Optional;

public interface CarRepository extends JpaRepository<Car, Long> {

    // Самые базовые методы для начала:

    // 1. Найти все доступные автомобили
    List<Car> findByAvailableTrue();

    // 2. Найти автомобили по марке
    List<Car> findByBrand(String brand);

    // 3. Найти автомобили по марке и модели
    List<Car> findByBrandAndModel(String brand, String model);

    // 4. Найти автомобили в ценовом диапазоне
    List<Car> findByPricePerDayBetween(Double minPrice, Double maxPrice);

    // 5. Найти автомобиль по VIN
    Optional<Car> findByVin(String vin);

    // Методы для менеджеров (по владельцу)
    List<Car> findByManager(User manager);
    List<Car> findByManagerId(Long managerId);

    // Поиск доступных машин по марке и цене
    List<Car> findByBrandAndAvailableTrueAndPricePerDayLessThanEqual(String brand, Double maxPrice);

    // Кастомный запрос: машины определенного менеджера
    @Query("SELECT c FROM Car c WHERE c.manager.id = :managerId AND c.available = true")
    List<Car> findAvailableCarsByManager(@Param("managerId") Long managerId);

    // Кастомный запрос: поиск по типу кузова и топливу
    @Query("SELECT c FROM Car c WHERE c.bodyType = :bodyType AND c.fuelType = :fuelType AND c.available = true")
    List<Car> findAvailableCarsByBodyTypeAndFuelType(@Param("bodyType") String bodyType,
                                                     @Param("fuelType") String fuelType);
}