package org.example.carrental.controller;

import org.example.carrental.entity.Car;
import org.example.carrental.service.CarService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class CarPageController {

    @Autowired
    private CarService carService;

    // Детальная страница автомобиля
    @GetMapping("/cars/{id}")
    public String carDetails(@PathVariable Long id, Model model) {
        Car car = carService.getCarById(id);
        model.addAttribute("car", car);
        return "car-details"; // создадим этот шаблон
    }
}