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

    // Найти все аренды пользователя
    List<Rental> findByUser(User user);

    // Найти все аренды автомобиля
    List<Rental> findByCar(Car car);

    // Найти аренды по статусу
    List<Rental> findByStatus(RentalStatus status);

    // Найти активные аренды (подтвержденные и активные)
    List<Rental> findByStatusIn(List<RentalStatus> statuses);

    // Найти аренды пользователя по статусу
    List<Rental> findByUserAndStatus(User user, RentalStatus status);

    // Проверить есть ли активные аренды у автомобиля
    @Query("SELECT COUNT(r) > 0 FROM Rental r WHERE r.car = :car AND r.status IN :activeStatuses")
    boolean existsActiveRentalForCar(@Param("car") Car car,
                                     @Param("activeStatuses") List<RentalStatus> activeStatuses);

    // Найти аренды в определенном периоде времени
    @Query("SELECT r FROM Rental r WHERE r.startDate <= :endDate AND r.endDate >= :startDate")
    List<Rental> findRentalsInPeriod(@Param("startDate") LocalDateTime startDate,
                                     @Param("endDate") LocalDateTime endDate);

    // Найти будущие аренды автомобиля
    @Query("SELECT r FROM Rental r WHERE r.car = :car AND r.startDate > :now ORDER BY r.startDate")
    List<Rental> findFutureRentalsForCar(@Param("car") Car car, @Param("now") LocalDateTime now);

    // Найти текущие активные аренды
    @Query("SELECT r FROM Rental r WHERE r.status = 'ACTIVE' AND r.endDate >= :now")
    List<Rental> findCurrentlyActiveRentals(@Param("now") LocalDateTime now);
}