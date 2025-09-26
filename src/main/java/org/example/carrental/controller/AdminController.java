package org.example.carrental.controller;

import org.example.carrental.entity.Car;
import org.example.carrental.entity.User;
import org.example.carrental.service.CarService;
import org.example.carrental.service.FileStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.servlet.http.HttpSession;
import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private CarService carService;

    @Autowired
    private FileStorageService fileStorageService;

    @GetMapping("/cars")
    public String adminCarsPage(HttpSession session, Model model) {
        User currentUser = (User) session.getAttribute("user");

        if (currentUser == null || (!currentUser.isManager() && !currentUser.isAdmin())) {
            return "redirect:/login";
        }

        List<Car> allCars = carService.getAllCars();

        // Ручной подсчет статистики
        long availableCount = allCars.stream().filter(Car::getAvailable).count();
        long occupiedCount = allCars.stream().filter(c -> !c.getAvailable()).count();
        long myCarsCount = allCars.stream()
                .filter(c -> c.getManager() != null && c.getManager().getId().equals(currentUser.getId()))
                .count();

        model.addAttribute("currentUser", currentUser);
        model.addAttribute("cars", allCars);
        model.addAttribute("availableCount", availableCount);
        model.addAttribute("occupiedCount", occupiedCount);
        model.addAttribute("myCarsCount", myCarsCount);

        return "admin-cars";
    }

    @GetMapping("/cars/add")
    public String addCarForm(HttpSession session, Model model) {
        User currentUser = (User) session.getAttribute("user");

        if (currentUser == null || (!currentUser.isManager() && !currentUser.isAdmin())) {
            return "redirect:/login";
        }

        model.addAttribute("currentUser", currentUser);
        model.addAttribute("car", new Car());
        return "add-car";
    }

    // ИСПРАВЛЕННАЯ ВЕРСИЯ - ДОБАВЛЕН ПАРАМЕТР ДЛЯ ФАЙЛА
    @PostMapping("/cars/add")
    public String addCar(@RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                         @RequestParam String brand,
                         @RequestParam String model,
                         @RequestParam Integer year,
                         @RequestParam String color,
                         @RequestParam Double pricePerDay,
                         @RequestParam String fuelType,
                         @RequestParam String transmission,
                         @RequestParam String bodyType,
                         @RequestParam String vin,
                         HttpSession session,
                         RedirectAttributes redirectAttributes) {

        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null || (!currentUser.isManager() && !currentUser.isAdmin())) {
            return "redirect:/login";
        }

        try {
            // РУЧНАЯ ОБРАБОТКА КОДИРОВКИ UTF-8
            String decodedBrand = new String(brand.getBytes("ISO-8859-1"), "UTF-8");
            String decodedModel = new String(model.getBytes("ISO-8859-1"), "UTF-8");
            String decodedColor = new String(color.getBytes("ISO-8859-1"), "UTF-8");
            String decodedFuelType = new String(fuelType.getBytes("ISO-8859-1"), "UTF-8");
            String decodedTransmission = new String(transmission.getBytes("ISO-8859-1"), "UTF-8");
            String decodedBodyType = new String(bodyType.getBytes("ISO-8859-1"), "UTF-8");
            String decodedVin = new String(vin.getBytes("ISO-8859-1"), "UTF-8");

            // Создаем объект Car вручную с правильной кодировкой
            Car car = new Car();
            car.setBrand(decodedBrand);
            car.setModel(decodedModel);
            car.setYear(year);
            car.setColor(decodedColor);
            car.setPricePerDay(pricePerDay);
            car.setFuelType(decodedFuelType);
            car.setTransmission(decodedTransmission);
            car.setBodyType(decodedBodyType);
            car.setVin(decodedVin);
            car.setAvailable(true);

            // ОБРАБОТКА ЗАГРУЗКИ ФОТО
            if (imageFile != null && !imageFile.isEmpty()) {
                try {
                    String filePath = fileStorageService.storeFile(imageFile);
                    car.setImageUrl(filePath);
                    System.out.println("Фото успешно загружено: " + filePath);
                } catch (Exception e) {
                    System.out.println("Ошибка загрузки фото: " + e.getMessage());
                    car.setImageUrl("/images/cars/default-car.jpg");
                }
            } else {
                car.setImageUrl("/images/cars/default-car.jpg");
            }

            // Устанавливаем менеджера
            car.setManager(currentUser);

            // Сохраняем автомобиль
            Car savedCar = carService.addCar(car, currentUser);
            redirectAttributes.addFlashAttribute("success", "Автомобиль успешно добавлен!");
            return "redirect:/admin/cars";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Ошибка при добавлении: " + e.getMessage());
            return "redirect:/admin/cars/add";
        }
    }
}