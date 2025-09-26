package org.example.carrental;

import org.example.carrental.config.DatabaseConfig;
import org.example.carrental.entity.Car;
import org.example.carrental.repository.CarRepository;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class CarRepositoryTest {

    @Test
    public void testCompleteCarWorkflow() {
        System.out.println("=== НАЧАЛО ТЕСТА: Полный цикл работы с автомобилями ===");

        // 1. Создаем Spring контекст с нашей конфигурацией
        try (var context = new AnnotationConfigApplicationContext(DatabaseConfig.class)) {

            // 2. Получаем репозиторий из контекста
            CarRepository carRepository = context.getBean(CarRepository.class);
            System.out.println("✅ Репозиторий получен из Spring контекста");

            // 3. ТЕСТ 1: Проверяем что база пустая
            List<Car> initialCars = carRepository.findAll();
            assertEquals(0, initialCars.size());
            System.out.println("✅ База данных пустая: " + initialCars.size() + " автомобилей");

            // 4. ТЕСТ 2: Создаем и сохраняем автомобиль
            Car toyota = new Car("Toyota", "Camry", 2023, 45.0, "бензин", "автоматическая", "седан");
            toyota.setColor("Черный");
            toyota.setVin("ABC12345678901234");
            toyota.setHorsePower(180);
            toyota.setMileage(15000);

            Car savedToyota = carRepository.save(toyota);
            assertNotNull(savedToyota.getId());
            System.out.println("✅ Toyota сохранена с ID: " + savedToyota.getId());

            // 5. ТЕСТ 3: Создаем второй автомобиль
            Car bmw = new Car("BMW", "X5", 2024, 85.0, "бензин", "автоматическая", "внедорожник");
            bmw.setColor("Белый");
            bmw.setVin("DEF98765432109876");
            bmw.setHorsePower(250);
            bmw.setMileage(5000);

            Car savedBmw = carRepository.save(bmw);
            System.out.println("✅ BMW сохранен с ID: " + savedBmw.getId());

            // 6. ТЕСТ 4: Проверяем что оба автомобиля в базе
            List<Car> allCars = carRepository.findAll();
            assertEquals(2, allCars.size());
            System.out.println("✅ В базе теперь " + allCars.size() + " автомобиля");

            // 7. ТЕСТ 5: Ищем автомобили по марке
            List<Car> toyotaCars = carRepository.findByBrand("Toyota");
            assertEquals(1, toyotaCars.size());
            assertEquals("Camry", toyotaCars.get(0).getModel());
            System.out.println("✅ Найдено Toyota: " + toyotaCars.size());

            // 8. ТЕСТ 6: Ищем по марке и модели
            List<Car> camryCars = carRepository.findByBrandAndModel("Toyota", "Camry");
            assertEquals(1, camryCars.size());
            System.out.println("✅ Найдена Toyota Camry");

            // 9. ТЕСТ 7: Ищем доступные автомобили
            List<Car> availableCars = carRepository.findByAvailableTrue();
            assertEquals(2, availableCars.size());
            System.out.println("✅ Доступных автомобилей: " + availableCars.size());

            // 10. ТЕСТ 8: Ищем по VIN
            Optional<Car> carByVin = carRepository.findByVin("ABC12345678901234");
            assertTrue(carByVin.isPresent());
            assertEquals("Toyota", carByVin.get().getBrand());
            System.out.println("✅ Найден автомобиль по VIN");

            // 11. ТЕСТ 9: Ищем по ценовому диапазону
            List<Car> affordableCars = carRepository.findByPricePerDayBetween(40.0, 50.0);
            assertEquals(1, affordableCars.size());
            assertEquals("Toyota", affordableCars.get(0).getBrand());
            System.out.println("✅ Найден автомобиль в ценовом диапазоне 40-50");

            // 12. ТЕСТ 10: Проверяем метод getFullName()
            String fullName = savedToyota.getFullName();
            assertEquals("Toyota Camry (2023)", fullName);
            System.out.println("✅ Полное имя автомобиля: " + fullName);

            System.out.println("=== ТЕСТ УСПЕШНО ЗАВЕРШЕН ===");

        } catch (Exception e) {
            fail("Тест упал с ошибкой: " + e.getMessage());
            e.printStackTrace();
        }
    }
}