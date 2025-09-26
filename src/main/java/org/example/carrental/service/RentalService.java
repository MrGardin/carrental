package org.example.carrental.service;

import org.example.carrental.entity.*;
import org.example.carrental.repository.RentalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.List;
import java.util.Arrays;

@Service
public class RentalService {

    private final RentalRepository rentalRepository;
    private final CarService carService;
    private final UserService userService;

    // Статусы, которые считаются "активными" (автомобиль занят)
    private final List<RentalStatus> ACTIVE_STATUSES = Arrays.asList(
            RentalStatus.CONFIRMED,
            RentalStatus.ACTIVE
    );

    @Autowired
    public RentalService(RentalRepository rentalRepository, CarService carService, UserService userService) {
        this.rentalRepository = rentalRepository;
        this.carService = carService;
        this.userService = userService;
    }

    // СОЗДАНИЕ ЗАПРОСА НА АРЕНДУ
    public Rental createRentalRequest(Long carId, Long userId, LocalDateTime startDate, LocalDateTime endDate) {
        // 1. ПРОВЕРКА ДАННЫХ
        if (startDate.isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Дата начала не может быть в прошлом!");
        }

        if (endDate.isBefore(startDate)) {
            throw new RuntimeException("Дата окончания должна быть после даты начала!");
        }

        // 2. ПОЛУЧЕНИЕ СУЩНОСТЕЙ
        Car car = carService.getCarById(carId);
        User user = userService.getUserById(userId);

        // 3. ПРОВЕРКА ЧТО ПОЛЬЗОВАТЕЛЬ - КЛИЕНТ
        if (!user.isClient()) {
            throw new RuntimeException("Только клиенты могут арендовать автомобили!");
        }

        // 4. ПРОВЕРКА ДОСТУПНОСТИ АВТОМОБИЛЯ
        if (!isCarAvailable(car, startDate, endDate)) {
            throw new RuntimeException("Автомобиль недоступен в выбранные даты!");
        }

        // 5. РАСЧЕТ СТОИМОСТИ
        BigDecimal totalPrice = calculatePrice(car, startDate, endDate);

        // 6. СОЗДАНИЕ АРЕНДЫ
        Rental rental = new Rental(user, car, startDate, endDate, totalPrice);
        return rentalRepository.save(rental);
    }

    // ПОДТВЕРЖДЕНИЕ АРЕНДЫ МЕНЕДЖЕРОМ
    public Rental confirmRental(Long rentalId, User manager) {
        // 1. ПРОВЕРКА ПРАВ
        if (!manager.isManager() && !manager.isAdmin()) {
            throw new RuntimeException("Только менеджеры и администраторы могут подтверждать аренды!");
        }

        Rental rental = getRentalById(rentalId);

        // 2. ПРОВЕРКА СТАТУСА
        if (rental.getStatus() != RentalStatus.PENDING) {
            throw new RuntimeException("Можно подтверждать только аренды со статусом PENDING!");
        }

        // 3. ПРОВЕРКА ДОСТУПНОСТИ АВТОМОБИЛЯ
        if (!isCarAvailable(rental.getCar(), rental.getStartDate(), rental.getEndDate())) {
            throw new RuntimeException("Автомобиль больше недоступен в выбранные даты!");
        }

        // 4. ПОДТВЕРЖДЕНИЕ
        rental.setStatus(RentalStatus.CONFIRMED);
        return rentalRepository.save(rental);
    }

    // НАЧАЛО АРЕНДЫ (выдача автомобиля)
    public Rental startRental(Long rentalId) {
        Rental rental = getRentalById(rentalId);

        // 1. ПРОВЕРКА СТАТУСА
        if (rental.getStatus() != RentalStatus.CONFIRMED) {
            throw new RuntimeException("Можно начинать только подтвержденные аренды!");
        }

        // 2. ПРОВЕРКА ДАТЫ
        if (rental.getStartDate().isAfter(LocalDateTime.now())) {
            throw new RuntimeException("Еще не время начинать аренду!");
        }

        // 3. НАЧАЛО АРЕНДЫ
        rental.setStatus(RentalStatus.ACTIVE);
        return rentalRepository.save(rental);
    }

    // ЗАВЕРШЕНИЕ АРЕНДЫ (возврат автомобиля)
    public Rental completeRental(Long rentalId) {
        Rental rental = getRentalById(rentalId);

        // 1. ПРОВЕРКА СТАТУСА
        if (rental.getStatus() != RentalStatus.ACTIVE) {
            throw new RuntimeException("Можно завершать только активные аренды!");
        }

        // 2. ЗАВЕРШЕНИЕ
        rental.setStatus(RentalStatus.COMPLETED);
        return rentalRepository.save(rental);
    }

    // ОТМЕНА АРЕНДЫ
    public Rental cancelRental(Long rentalId, User user) {
        Rental rental = getRentalById(rentalId);

        // 1. ПРОВЕРКА ПРАВ
        boolean isOwner = rental.getUser().getId().equals(user.getId());
        boolean isManagerOrAdmin = user.isManager() || user.isAdmin();

        if (!isOwner && !isManagerOrAdmin) {
            throw new RuntimeException("Недостаточно прав для отмены аренды!");
        }

        // 2. ПРОВЕРКА СТАТУСА
        if (rental.getStatus() == RentalStatus.COMPLETED || rental.getStatus() == RentalStatus.CANCELLED) {
            throw new RuntimeException("Нельзя отменить завершенную или уже отмененную аренду!");
        }

        // 3. ОТМЕНА
        rental.setStatus(RentalStatus.CANCELLED);
        return rentalRepository.save(rental);
    }

    // ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ
    public Rental getRentalById(Long id) {
        return rentalRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Аренда не найдена с ID: " + id));
    }

    public List<Rental> getUserRentals(Long userId) {
        User user = userService.getUserById(userId);
        return rentalRepository.findByUser(user);
    }

    public List<Rental> getCarRentals(Long carId) {
        Car car = carService.getCarById(carId);
        return rentalRepository.findByCar(car);
    }

    public List<Rental> getRentalsByStatus(RentalStatus status) {
        return rentalRepository.findByStatus(status);
    }

    // ПРОВЕРКА ДОСТУПНОСТИ АВТОМОБИЛЯ
    private boolean isCarAvailable(Car car, LocalDateTime startDate, LocalDateTime endDate) {
        // 1. ПРОВЕРКА БАЗОВОЙ ДОСТУПНОСТИ
        if (!car.getAvailable()) {
            return false;
        }

        // 2. ПРОВЕРКА НАЛОЖЕНИЯ АРЕНД
        return !rentalRepository.existsActiveRentalForCar(car, ACTIVE_STATUSES);
    }

    // РАСЧЕТ СТОИМОСТИ
    private BigDecimal calculatePrice(Car car, LocalDateTime start, LocalDateTime end) {
        long days = java.time.Duration.between(start, end).toDays();
        if (days < 1) days = 1; // минимум 1 день

        return BigDecimal.valueOf(car.getPricePerDay() * days);
    }
}