package org.example.carrental.controller;

import jakarta.servlet.http.HttpSession;
import org.example.carrental.entity.Car;
import org.example.carrental.entity.User;
import org.example.carrental.service.CarService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.example.carrental.dto.CarFilterDTO;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.List;
import java.util.Map;

@Controller
public class HomeController {

    @Autowired
    private CarService carService;

    @GetMapping("/")
    public String homePage(Model model, HttpSession session) {
        model.addAttribute("cars", carService.getAvailableCars());

        // Добавляем пользователя в модель
        User user = (User) session.getAttribute("user");
        model.addAttribute("currentUser", user);

        return "index";
    }

    @GetMapping("/cars")
    public String allCarsPage(@ModelAttribute CarFilterDTO filters, // Добавьте эту аннотацию
                              HttpSession session,
                              Model model) {

        // Если фильтры не заданы, показываем все автомобили
        List<Car> cars;
        if (areFiltersEmpty(filters)) {
            cars = carService.getAllCars();
        } else {
            cars = carService.getCarsWithFilters(filters);
        }

        Map<String, List<String>> filterOptions = carService.getFilterOptions();

        model.addAttribute("cars", cars);
        model.addAttribute("filterOptions", filterOptions);
        model.addAttribute("filters", filters);

        User user = (User) session.getAttribute("user");
        model.addAttribute("currentUser", user);

        return "cars";
    }

    // Вспомогательный метод для проверки пустых фильтров
    private boolean areFiltersEmpty(CarFilterDTO filters) {
        return filters.getBrand() == null &&
                filters.getMinPrice() == null &&
                filters.getMaxPrice() == null &&
                filters.getBodyType() == null &&
                filters.getFuelType() == null &&
                filters.getTransmission() == null &&
                filters.getMinYear() == null &&
                filters.getMaxYear() == null &&
                filters.getAvailable() == null;
    }


    @GetMapping("/test-images")
    public String testImages() {
        return "test-images";
    }
}