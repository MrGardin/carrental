package org.example.carrental.repository;

import org.example.carrental.entity.Rental;
import org.example.carrental.entity.RentalStatus;
import org.example.carrental.entity.User;
import org.example.carrental.entity.Car;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;

public interface RentalRepository extends JpaRepository<Rental, Long> {

    // === СУЩЕСТВУЮЩИЕ МЕТОДЫ ===
    List<Rental> findByUser(User user);
    List<Rental> findByCar(Car car);
    List<Rental> findByStatus(RentalStatus status);
    List<Rental> findByStatusIn(List<RentalStatus> statuses);
    List<Rental> findByUserAndStatus(User user, RentalStatus status);

    @Query("SELECT COUNT(r) > 0 FROM Rental r WHERE r.car = :car AND r.status IN :activeStatuses")
    boolean existsActiveRentalForCar(@Param("car") Car car,
                                     @Param("activeStatuses") List<RentalStatus> activeStatuses);

    @Query("SELECT r FROM Rental r WHERE r.startDate <= :endDate AND r.endDate >= :startDate")
    List<Rental> findRentalsInPeriod(@Param("startDate") LocalDateTime startDate,
                                     @Param("endDate") LocalDateTime endDate);

    @Query("SELECT r FROM Rental r WHERE r.car = :car AND r.startDate > :now ORDER BY r.startDate")
    List<Rental> findFutureRentalsForCar(@Param("car") Car car, @Param("now") LocalDateTime now);

    @Query("SELECT r FROM Rental r WHERE r.status = 'ACTIVE' AND r.endDate >= :now")
    List<Rental> findCurrentlyActiveRentals(@Param("now") LocalDateTime now);

    // === НОВЫЕ МЕТОДЫ ДЛЯ МЕНЕДЖЕРА ===

    // Найти все аренды для автомобилей менеджера
    List<Rental> findByCar_Manager(User manager);

    // Найти аренды по менеджеру и статусу
    List<Rental> findByCar_ManagerAndStatus(User manager, RentalStatus status);

    // Найти аренды по менеджеру и списку статусов
    List<Rental> findByCar_ManagerAndStatusIn(User manager, List<RentalStatus> statuses);

    // Проверить наложение аренд с исключением текущей аренды
    @Query("SELECT COUNT(r) > 0 FROM Rental r WHERE " +
            "r.car.id = :carId AND " +
            "r.id != :excludeRentalId AND " +
            "r.status IN :activeStatuses AND " +
            "((r.startDate BETWEEN :startDate AND :endDate) OR " +
            "(r.endDate BETWEEN :startDate AND :endDate) OR " +
            "(r.startDate <= :startDate AND r.endDate >= :endDate))")
    boolean existsOverlappingRental(@Param("carId") Long carId,
                                    @Param("startDate") LocalDateTime startDate,
                                    @Param("endDate") LocalDateTime endDate,
                                    @Param("excludeRentalId") Long excludeRentalId,
                                    @Param("activeStatuses") List<RentalStatus> activeStatuses);

    // Найти аренды по менеджеру и периоду времени
    @Query("SELECT r FROM Rental r WHERE " +
            "r.car.manager = :manager AND " +
            "((r.startDate BETWEEN :startDate AND :endDate) OR " +
            "(r.endDate BETWEEN :startDate AND :endDate) OR " +
            "(r.startDate <= :startDate AND r.endDate >= :endDate))")
    List<Rental> findByManagerAndPeriod(@Param("manager") User manager,
                                        @Param("startDate") LocalDateTime startDate,
                                        @Param("endDate") LocalDateTime endDate);

    // Статистика по статусам для менеджера
    @Query("SELECT r.status, COUNT(r) FROM Rental r WHERE r.car.manager = :manager GROUP BY r.status")
    List<Object[]> getRentalStatsByManager(@Param("manager") User manager);

    // Найти ближайшие аренды для менеджера
    @Query("SELECT r FROM Rental r WHERE " +
            "r.car.manager = :manager AND " +
            "r.startDate >= :startDate AND " +
            "r.startDate <= :endDate " +
            "ORDER BY r.startDate")
    List<Rental> findUpcomingRentalsForManager(@Param("manager") User manager,
                                               @Param("startDate") LocalDateTime startDate,
                                               @Param("endDate") LocalDateTime endDate);

    // Найти просроченные аренды для менеджера
    @Query("SELECT r FROM Rental r WHERE " +
            "r.car.manager = :manager AND " +
            "r.status = 'ACTIVE' AND " +
            "r.endDate < :now")
    List<Rental> findOverdueRentalsForManager(@Param("manager") User manager,
                                              @Param("now") LocalDateTime now);

    // Найти аренды требующие внимания (ожидающие подтверждения + активные сегодня)
    @Query("SELECT r FROM Rental r WHERE " +
            "r.car.manager = :manager AND " +
            "(r.status = 'PENDING' OR " +
            "(r.status IN ('CONFIRMED', 'ACTIVE') AND r.startDate <= :today AND r.endDate >= :today)) " +
            "ORDER BY r.status, r.startDate")
    List<Rental> findRentalsRequiringAttention(@Param("manager") User manager,
                                               @Param("today") LocalDateTime today);

    // Получить общую выручку по завершенным арендам менеджера
    @Query("SELECT COALESCE(SUM(r.actualPrice), 0) FROM Rental r WHERE " +
            "r.car.manager = :manager AND " +
            "r.status = 'COMPLETED' AND " +
            "r.actualPrice IS NOT NULL")
    Double getTotalRevenueByManager(@Param("manager") User manager);

    // Найти аренды по клиенту для менеджера
    @Query("SELECT r FROM Rental r WHERE " +
            "r.car.manager = :manager AND " +
            "r.user.id = :userId")
    List<Rental> findByManagerAndUser(@Param("manager") User manager,
                                      @Param("userId") Long userId);

    // Найти аренды по автомобилю для менеджера
    @Query("SELECT r FROM Rental r WHERE " +
            "r.car.manager = :manager AND " +
            "r.car.id = :carId")
    List<Rental> findByManagerAndCar(@Param("manager") User manager,
                                     @Param("carId") Long carId);
}