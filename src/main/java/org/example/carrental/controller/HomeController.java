package org.example.carrental.controller;

import jakarta.servlet.http.HttpSession;
import org.example.carrental.entity.User;
import org.example.carrental.service.CarService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

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
    public String allCarsPage(Model model, HttpSession session) {
        model.addAttribute("cars", carService.getAllCars());

        // Добавляем пользователя в модель
        User user = (User) session.getAttribute("user");
        model.addAttribute("currentUser", user);

        return "cars";
    }



    @GetMapping("/test-images")
    public String testImages() {
        return "test-images";
    }
}