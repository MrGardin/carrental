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
    @Query("SELECT c FROM Car c WHERE " +
            "(:brand IS NULL OR :brand = '' OR c.brand LIKE %:brand%) AND " +
            "(:minPrice IS NULL OR c.pricePerDay >= :minPrice) AND " +
            "(:maxPrice IS NULL OR c.pricePerDay <= :maxPrice) AND " +
            "(:bodyType IS NULL OR :bodyType = '' OR c.bodyType = :bodyType) AND " +
            "(:fuelType IS NULL OR :fuelType = '' OR c.fuelType = :fuelType) AND " +
            "(:transmission IS NULL OR :transmission = '' OR c.transmission = :transmission) AND " +
            "(:minYear IS NULL OR c.year >= :minYear) AND " +
            "(:maxYear IS NULL OR c.year <= :maxYear) AND " +
            "(:available IS NULL OR c.available = :available)")
    List<Car> findWithFilters(@Param("brand") String brand,
                              @Param("minPrice") Double minPrice,
                              @Param("maxPrice") Double maxPrice,
                              @Param("bodyType") String bodyType,
                              @Param("fuelType") String fuelType,
                              @Param("transmission") String transmission,
                              @Param("minYear") Integer minYear,
                              @Param("maxYear") Integer maxYear,
                              @Param("available") Boolean available);
    // Кастомный запрос: поиск по типу кузова и топливу
    @Query("SELECT c FROM Car c WHERE c.bodyType = :bodyType AND c.fuelType = :fuelType AND c.available = true")
    List<Car> findAvailableCarsByBodyTypeAndFuelType(@Param("bodyType") String bodyType,
                                                     @Param("fuelType") String fuelType);
}