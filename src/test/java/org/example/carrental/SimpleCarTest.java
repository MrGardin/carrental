package org.example.carrental;

import org.example.carrental.entity.Car;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class SimpleCarTest {

    @Test
    public void testCarEntity() {
        System.out.println("=== ПРОСТОЙ ТЕСТ СУЩНОСТИ CAR ===");

        // Просто проверяем что сущность Car работает
        Car car = new Car("Toyota", "Camry", 2023, 45.0, "бензин", "автоматическая", "седан");
        car.setColor("Черный");
        car.setVin("TEST1234567890123");
        car.setHorsePower(180);
        car.setMileage(15000);

        // Проверяем геттеры (должны работать через Lombok)
        assertEquals("Toyota", car.getBrand());
        assertEquals("Camry", car.getModel());
        assertEquals(2023, car.getYear());
        assertEquals(45.0, car.getPricePerDay());
        assertTrue(car.getAvailable());
        assertEquals("Черный", car.getColor());
        assertEquals("TEST1234567890123", car.getVin());
        assertEquals(180, car.getHorsePower());
        assertEquals(15000, car.getMileage());

        // Проверяем кастомный метод
        assertEquals("Toyota Camry (2023)", car.getFullName());

        System.out.println("✅ Простой тест Car прошел успешно!");
        System.out.println("Полное имя: " + car.getFullName());
    }

    @Test
    public void testCarConstructor() {
        // Тестируем конструктор
        Car car = new Car();
        assertNull(car.getBrand()); // Должно быть null по умолчанию
        assertNull(car.getModel());
        assertTrue(car.getAvailable()); // available = true по умолчанию

        System.out.println("✅ Конструктор по умолчанию работает!");
    }
}