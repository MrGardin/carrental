package org.example.carrental.config;

import org.example.carrental.entity.Car;
import org.example.carrental.entity.User;
import org.example.carrental.service.CarService;
import org.example.carrental.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import jakarta.annotation.PostConstruct;

@Configuration
public class TestDataConfig {

    @Autowired
    private CarService carService;

    @Autowired
    private UserService userService;

    @PostConstruct
    public void initTestData() {
        try {
            System.out.println("=== СОЗДАЕМ ТЕСТОВЫЕ ДАННЫЕ ===");

            // Создаем тестового менеджера
            User manager = userService.registerManager(
                    "manager@rental.com",
                    "manager123",
                    "Иван Менеджеров",
                    "+79161234567"
            );
            System.out.println("✅ Менеджер создан: " + manager.getEmail());

            // Создаем тестовые автомобили (цены до 1000 руб/день)
            Car car1 = new Car("Toyota", "Camry", 2022, 800.0, "Бензин", "Автомат", "Седан");
            car1.setColor("Белый");
            car1.setVin("VIN12345678901234");
            carService.addCar(car1, manager);
            System.out.println("✅ Автомобиль создан: " + car1.getBrand() + " " + car1.getModel());

            Car car2 = new Car("BMW", "X5", 2023, 950.0, "Бензин", "Автомат", "Внедорожник"); // цена 950 вместо 5000
            car2.setColor("Черный");
            car2.setVin("VIN98765432109876");
            car2.setImageUrl("/images/cars/bmw-x5.jpg");
            carService.addCar(car2, manager);
            System.out.println("✅ Автомобиль создан: " + car2.getBrand() + " " + car2.getModel());

            Car car3 = new Car("Hyundai", "Solaris", 2021, 500.0, "Бензин", "Механика", "Седан");
            car3.setColor("Серый");
            car3.setVin("VIN55555555555555");
            carService.addCar(car3, manager);
            System.out.println("✅ Автомобиль создан: " + car3.getBrand() + " " + car3.getModel());

            System.out.println("=== ТЕСТОВЫЕ ДАННЫЕ СОЗДАНЫ ===");

        } catch (Exception e) {
            System.out.println("⚠️ Ошибка создания тестовых данных: " + e.getMessage());
            e.printStackTrace(); // добавим стектрейс для отладки
        }
    }
}