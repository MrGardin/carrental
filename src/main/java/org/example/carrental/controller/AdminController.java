package org.example.carrental.controller;

import org.example.carrental.entity.Car;
import org.example.carrental.entity.User;
import org.example.carrental.service.CarService;
import org.example.carrental.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.servlet.http.HttpSession;
import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private CarService carService;

    @Autowired
    private UserService userService;

    // АДМИНСКАЯ СТАТИСТИКА - просмотр ВСЕХ данных системы
    @GetMapping("/dashboard")
    public String adminDashboard(HttpSession session, Model model) {
        User currentUser = (User) session.getAttribute("user");

        if (currentUser == null || !currentUser.isAdmin()) {
            return "redirect:/login";
        }

        List<Car> allCars = carService.getAllCars();
        List<User> allUsers = userService.getAllUsers(); // нужно добавить этот метод в UserService
        List<User> managers = userService.getAllManagers();
        List<User> clients = userService.getAllClients();

        // Общая статистика системы
        long totalCars = allCars.size();
        long availableCars = allCars.stream().filter(Car::getAvailable).count();
        long totalUsers = allUsers.size();
        long pendingManagers = managers.stream().filter(m -> !m.isApproved()).count();

        model.addAttribute("currentUser", currentUser);
        model.addAttribute("totalCars", totalCars);
        model.addAttribute("availableCars", availableCars);
        model.addAttribute("totalUsers", totalUsers);
        model.addAttribute("pendingManagers", pendingManagers);
        model.addAttribute("managersCount", managers.size());
        model.addAttribute("clientsCount", clients.size());

        return "admin-dashboard";
    }

    // УПРАВЛЕНИЕ ПОЛЬЗОВАТЕЛЯМИ - главная функция админа
    @GetMapping("/users")
    public String manageUsers(HttpSession session, Model model) {
        User currentUser = (User) session.getAttribute("user");

        if (currentUser == null || !currentUser.isAdmin()) {
            return "redirect:/login";
        }

        List<User> allUsers = userService.getAllUsers();
        List<User> pendingManagers = userService.getAllManagers().stream()
                .filter(m -> !m.isApproved())
                .toList();

        model.addAttribute("currentUser", currentUser);
        model.addAttribute("users", allUsers);
        model.addAttribute("pendingManagers", pendingManagers);

        return "admin-users";
    }

    // Одобрение менеджера
    @PostMapping("/approve-manager/{managerId}")
    public String approveManager(@PathVariable Long managerId,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {
        User currentUser = (User) session.getAttribute("user");

        if (currentUser == null || !currentUser.isAdmin()) {
            return "redirect:/login";
        }

        try {
            User approvedManager = userService.approveManager(managerId, currentUser);
            redirectAttributes.addFlashAttribute("success",
                    "Менеджер " + approvedManager.getFullName() + " одобрен!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/admin/users";
    }

    // ПРОСМОТР ВСЕХ АВТОМОБИЛЕЙ (только чтение для админа)
    @GetMapping("/cars")
    public String viewAllCars(HttpSession session, Model model) {
        User currentUser = (User) session.getAttribute("user");

        if (currentUser == null || !currentUser.isAdmin()) {
            return "redirect:/login";
        }

        List<Car> allCars = carService.getAllCars();
        long availableCount = allCars.stream().filter(Car::getAvailable).count();
        long occupiedCount = allCars.stream().filter(c -> !c.getAvailable()).count();

        model.addAttribute("currentUser", currentUser);
        model.addAttribute("cars", allCars);
        model.addAttribute("availableCount", availableCount);
        model.addAttribute("occupiedCount", occupiedCount);
        model.addAttribute("totalCount", allCars.size());

        return "admin-cars-view"; // шаблон только для просмотра
    }
}