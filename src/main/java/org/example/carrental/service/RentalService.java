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

    // === МЕТОДЫ ДЛЯ МЕНЕДЖЕРА ===

    // Получить все аренды для автомобилей менеджера
    public List<Rental> getAllRentalsByManager(Long managerId) {
        User manager = userService.getUserById(managerId);
        return rentalRepository.findByCar_Manager(manager);
    }

    // Получить ожидающие аренды для менеджера
    public List<Rental> getPendingRentalsByManager(Long managerId) {
        User manager = userService.getUserById(managerId);
        return rentalRepository.findByCar_ManagerAndStatus(manager, RentalStatus.PENDING);
    }

    // Получить активные аренды для менеджера
    public List<Rental> getActiveRentalsByManager(Long managerId) {
        User manager = userService.getUserById(managerId);
        return rentalRepository.findByCar_ManagerAndStatusIn(manager, ACTIVE_STATUSES);
    }

    // Получить завершенные аренды для менеджера
    public List<Rental> getCompletedRentalsByManager(Long managerId) {
        User manager = userService.getUserById(managerId);
        return rentalRepository.findByCar_ManagerAndStatus(manager, RentalStatus.COMPLETED);
    }

    // Одобрить аренду (менеджер)
    public Rental approveRental(Long rentalId, Long managerId) {
        User manager = userService.getUserById(managerId);
        Rental rental = getRentalById(rentalId);

        // Проверяем что менеджер имеет право на эту аренду
        if (!rental.getCar().getManager().getId().equals(managerId)) {
            throw new RuntimeException("У вас нет прав для управления этой арендой!");
        }

        // Проверяем статус
        if (rental.getStatus() != RentalStatus.PENDING) {
            throw new RuntimeException("Можно одобрять только аренды со статусом 'Ожидает подтверждения'!");
        }

        // Проверяем доступность автомобиля
        if (!isCarAvailableForRental(rental.getCar(), rental.getStartDate(), rental.getEndDate(), rentalId)) {
            throw new RuntimeException("Автомобиль недоступен в выбранные даты!");
        }

        rental.setStatus(RentalStatus.CONFIRMED);
        return rentalRepository.save(rental);
    }

    // Отклонить аренду (менеджер)
    public Rental rejectRental(Long rentalId, Long managerId, String reason) {
        User manager = userService.getUserById(managerId);
        Rental rental = getRentalById(rentalId);

        // Проверяем что менеджер имеет право на эту аренду
        if (!rental.getCar().getManager().getId().equals(managerId)) {
            throw new RuntimeException("У вас нет прав для управления этой арендой!");
        }

        // Проверяем статус
        if (rental.getStatus() != RentalStatus.PENDING) {
            throw new RuntimeException("Можно отклонять только аренды со статусом 'Ожидает подтверждения'!");
        }

        rental.setStatus(RentalStatus.REJECTED);
        if (reason != null && !reason.trim().isEmpty()) {
            rental.setRejectionReason(reason);
        }

        return rentalRepository.save(rental);
    }

    // Завершить аренду (менеджер)
    public Rental completeRental(Long rentalId, Long managerId) {
        User manager = userService.getUserById(managerId);
        Rental rental = getRentalById(rentalId);

        // Проверяем что менеджер имеет право на эту аренду
        if (!rental.getCar().getManager().getId().equals(managerId)) {
            throw new RuntimeException("У вас нет прав для управления этой арендой!");
        }

        // Проверяем статус
        if (rental.getStatus() != RentalStatus.ACTIVE && rental.getStatus() != RentalStatus.CONFIRMED) {
            throw new RuntimeException("Можно завершать только активные или подтвержденные аренды!");
        }

        rental.setStatus(RentalStatus.COMPLETED);
        rental.setActualEndDate(LocalDateTime.now());

        // Расчет фактической стоимости
        if (rental.getActualEndDate() != null && rental.getStartDate() != null) {
            long actualDays = java.time.Duration.between(rental.getStartDate(), rental.getActualEndDate()).toDays();
            if (actualDays < 1) actualDays = 1;
            BigDecimal actualPrice = BigDecimal.valueOf(rental.getCar().getPricePerDay() * actualDays);
            rental.setActualPrice(actualPrice);
        }

        return rentalRepository.save(rental);
    }

    // === СУЩЕСТВУЮЩИЕ МЕТОДЫ (оставляем без изменений) ===

    public Rental createRentalRequest(Long carId, Long userId, LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate.isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Дата начала не может быть в прошлом!");
        }

        if (endDate.isBefore(startDate)) {
            throw new RuntimeException("Дата окончания должна быть после даты начала!");
        }

        Car car = carService.getCarById(carId);
        User user = userService.getUserById(userId);

        if (!user.isClient()) {
            throw new RuntimeException("Только клиенты могут арендовать автомобили!");
        }

        if (!isCarAvailableForRental(car, startDate, endDate, null)) {
            throw new RuntimeException("Автомобиль недоступен в выбранные даты!");
        }

        BigDecimal totalPrice = calculatePrice(car, startDate, endDate);
        Rental rental = new Rental(user, car, startDate, endDate, totalPrice);
        return rentalRepository.save(rental);
    }

    public Rental confirmRental(Long rentalId, User manager) {
        if (!manager.isManager() && !manager.isAdmin()) {
            throw new RuntimeException("Только менеджеры и администраторы могут подтверждать аренды!");
        }

        Rental rental = getRentalById(rentalId);

        if (rental.getStatus() != RentalStatus.PENDING) {
            throw new RuntimeException("Можно подтверждать только аренды со статусом PENDING!");
        }

        if (!isCarAvailableForRental(rental.getCar(), rental.getStartDate(), rental.getEndDate(), rentalId)) {
            throw new RuntimeException("Автомобиль больше недоступен в выбранные даты!");
        }

        rental.setStatus(RentalStatus.CONFIRMED);
        return rentalRepository.save(rental);
    }

    public Rental startRental(Long rentalId) {
        Rental rental = getRentalById(rentalId);

        if (rental.getStatus() != RentalStatus.CONFIRMED) {
            throw new RuntimeException("Можно начинать только подтвержденные аренды!");
        }

        if (rental.getStartDate().isAfter(LocalDateTime.now())) {
            throw new RuntimeException("Еще не время начинать аренду!");
        }

        rental.setStatus(RentalStatus.ACTIVE);
        return rentalRepository.save(rental);
    }

    public Rental completeRental(Long rentalId) {
        Rental rental = getRentalById(rentalId);

        if (rental.getStatus() != RentalStatus.ACTIVE) {
            throw new RuntimeException("Можно завершать только активные аренды!");
        }

        rental.setStatus(RentalStatus.COMPLETED);
        return rentalRepository.save(rental);
    }

    public Rental cancelRental(Long rentalId, User user) {
        Rental rental = getRentalById(rentalId);

        boolean isOwner = rental.getUser().getId().equals(user.getId());
        boolean isManagerOrAdmin = user.isManager() || user.isAdmin();

        if (!isOwner && !isManagerOrAdmin) {
            throw new RuntimeException("Недостаточно прав для отмены аренды!");
        }

        if (rental.getStatus() == RentalStatus.COMPLETED || rental.getStatus() == RentalStatus.CANCELLED) {
            throw new RuntimeException("Нельзя отменить завершенную или уже отмененную аренду!");
        }

        rental.setStatus(RentalStatus.CANCELLED);
        return rentalRepository.save(rental);
    }

    // === ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ===

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

    // Обновленная проверка доступности с учетом текущей аренды
    private boolean isCarAvailableForRental(Car car, LocalDateTime startDate, LocalDateTime endDate, Long currentRentalId) {
        if (!car.getAvailable()) {
            return false;
        }

        // Проверяем наложение аренд, исключая текущую аренду (для редактирования)
        return !rentalRepository.existsOverlappingRental(car.getId(), startDate, endDate, currentRentalId, ACTIVE_STATUSES);
    }

    private BigDecimal calculatePrice(Car car, LocalDateTime start, LocalDateTime end) {
        long days = java.time.Duration.between(start, end).toDays();
        if (days < 1) days = 1;
        return BigDecimal.valueOf(car.getPricePerDay() * days);
    }

    // Статистика для менеджера
    public ManagerRentalStats getManagerRentalStats(Long managerId) {
        User manager = userService.getUserById(managerId);
        List<Rental> allRentals = rentalRepository.findByCar_Manager(manager);

        long pendingCount = allRentals.stream().filter(r -> r.getStatus() == RentalStatus.PENDING).count();
        long activeCount = allRentals.stream().filter(r -> ACTIVE_STATUSES.contains(r.getStatus())).count();
        long completedCount = allRentals.stream().filter(r -> r.getStatus() == RentalStatus.COMPLETED).count();
        BigDecimal totalRevenue = allRentals.stream()
                .filter(r -> r.getStatus() == RentalStatus.COMPLETED && r.getActualPrice() != null)
                .map(Rental::getActualPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new ManagerRentalStats(pendingCount, activeCount, completedCount, totalRevenue, allRentals.size());
    }

    // DTO для статистики менеджера
    public static class ManagerRentalStats {
        public final long pendingCount;
        public final long activeCount;
        public final long completedCount;
        public final BigDecimal totalRevenue;
        public final long totalCount;

        public ManagerRentalStats(long pendingCount, long activeCount, long completedCount,
                                  BigDecimal totalRevenue, long totalCount) {
            this.pendingCount = pendingCount;
            this.activeCount = activeCount;
            this.completedCount = completedCount;
            this.totalRevenue = totalRevenue;
            this.totalCount = totalCount;
        }
    }
}