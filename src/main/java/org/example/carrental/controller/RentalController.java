package org.example.carrental.controller;

import org.example.carrental.dto.RentalRequest;
import org.example.carrental.entity.Car;
import org.example.carrental.entity.Rental;
import org.example.carrental.entity.User;
import org.example.carrental.service.CarService;
import org.example.carrental.service.RentalService;
import org.example.carrental.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import java.time.LocalDateTime;

@Controller
@RequestMapping("/rentals")
public class RentalController {

    @Autowired
    private RentalService rentalService;

    @Autowired
    private CarService carService;

    @Autowired
    private UserService userService;

    // ЕДИНСТВЕННЫЙ метод для страницы бронирования
    @GetMapping("/book/{carId}")
    public String bookCarPage(@PathVariable Long carId,
                              HttpSession session,
                              Model model,
                              @RequestParam(value = "error", required = false) String error) {
        try {
            System.out.println("=== DEBUG БРОНИРОВАНИЕ ===");
            System.out.println("1. Car ID: " + carId);

            // Получаем пользователя из сессии
            User user = (User) session.getAttribute("user");
            System.out.println("2. User from session: " + (user != null ? user.getEmail() : "null"));

            // Проверяем аутентификацию
            if (user == null) {
                System.out.println("3. User not authenticated - redirecting to login");
                return "redirect:/login?booking=true&carId=" + carId;
            }

            // Шаг 1: Ищем автомобиль
            Car car = carService.getCarById(carId);
            if (car == null) {
                return "redirect:/cars?error=Car+not+found";
            }
            System.out.println("4. Car found: " + car.getBrand() + " " + car.getModel());

            // Шаг 2: Обновляем данные пользователя из базы
            User currentUser = userService.findByEmail(user.getEmail());
            System.out.println("5. User found: " + currentUser.getFullName() + " (Role: " + currentUser.getRole() + ")");

            // Шаг 3: Добавляем в модель
            model.addAttribute("car", car);
            model.addAttribute("user", currentUser);
            model.addAttribute("rentalRequest", new RentalRequest());

            // Добавляем ошибку, если есть (из параметра или flash атрибута)
            if (error != null) {
                model.addAttribute("error", error);
            }

            System.out.println("6. SUCCESS - Redirecting to book-car.html");
            return "book-car";

        } catch (Exception e) {
            System.out.println("=== DEBUG ERROR ===");
            System.out.println("Error message: " + e.getMessage());
            e.printStackTrace();
            return "redirect:/cars?error=Booking+error";
        }
    }

    // Вариант 1: Простой редирект без сложных параметров
    @PostMapping("/book")
    public String bookCar(@ModelAttribute RentalRequest request,
                          HttpSession session,
                          Model model) {
        try {
            // Получаем пользователя из сессии
            User user = (User) session.getAttribute("user");
            if (user == null) {
                return "redirect:/login?booking=true&carId=" + request.getCarId();
            }

            // Обновляем данные пользователя из базы
            User currentUser = userService.findByEmail(user.getEmail());

            Rental rental = rentalService.createRentalRequest(
                    request.getCarId(),
                    currentUser.getId(),
                    request.getStartDate(),
                    request.getEndDate()
            );

            return "redirect:/rentals/confirmation/" + rental.getId();
        } catch (Exception e) {
            // Простой редирект с английским текстом ошибки
            return "redirect:/rentals/book/" + request.getCarId() + "?error=booking_error";
        }
    }

    // Вариант 2: Возврат на ту же страницу без редиректа (РЕКОМЕНДУЕМЫЙ)
    @PostMapping("/book2")
    public String bookCarAlternative(@ModelAttribute RentalRequest request,
                                     HttpSession session,
                                     Model model) {
        try {
            // Получаем пользователя из сессии
            User user = (User) session.getAttribute("user");
            if (user == null) {
                return "redirect:/login?booking=true&carId=" + request.getCarId();
            }

            // Обновляем данные пользователя из базы
            User currentUser = userService.findByEmail(user.getEmail());

            Rental rental = rentalService.createRentalRequest(
                    request.getCarId(),
                    currentUser.getId(),
                    request.getStartDate(),
                    request.getEndDate()
            );

            return "redirect:/rentals/confirmation/" + rental.getId();

        } catch (Exception e) {
            // Вместо редиректа возвращаем ту же страницу с ошибкой
            User user = (User) session.getAttribute("user");
            if (user != null) {
                User currentUser = userService.findByEmail(user.getEmail());
                Car car = carService.getCarById(request.getCarId());

                model.addAttribute("car", car);
                model.addAttribute("user", currentUser);
                model.addAttribute("rentalRequest", request); // Сохраняем введенные данные
                model.addAttribute("error", e.getMessage()); // Русский текст ошибки
            }

            return "book-car";
        }
    }

    // Вариант 3: С правильным использованием RedirectAttributes
    @PostMapping("/book3")
    public String bookCarWithAttributes(@ModelAttribute RentalRequest request,
                                        HttpSession session,
                                        RedirectAttributes redirectAttributes) {
        try {
            // Получаем пользователя из сессии
            User user = (User) session.getAttribute("user");
            if (user == null) {
                return "redirect:/login?booking=true&carId=" + request.getCarId();
            }

            // Обновляем данные пользователя из базы
            User currentUser = userService.findByEmail(user.getEmail());

            Rental rental = rentalService.createRentalRequest(
                    request.getCarId(),
                    currentUser.getId(),
                    request.getStartDate(),
                    request.getEndDate()
            );

            return "redirect:/rentals/confirmation/" + rental.getId();

        } catch (Exception e) {
            // Правильное использование RedirectAttributes
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            // Простой редирект без шаблонных переменных
            return "redirect:/rentals/book/" + request.getCarId();
        }
    }

    // Страница подтверждения бронирования
    @GetMapping("/confirmation/{rentalId}")
    public String confirmationPage(@PathVariable Long rentalId,
                                   HttpSession session,
                                   Model model) {
        try {
            User user = (User) session.getAttribute("user");
            if (user == null) {
                return "redirect:/login";
            }

            Rental rental = rentalService.getRentalById(rentalId);
            User currentUser = userService.findByEmail(user.getEmail());

            if (!rental.getUser().getId().equals(currentUser.getId())) {
                model.addAttribute("error", "Доступ запрещен");
                return "redirect:/cars";
            }

            model.addAttribute("rental", rental);
            return "rental-confirmation";
        } catch (Exception e) {
            return "redirect:/cars?error=Rental+not+found";
        }
    }

    // Страница моих аренд
    @GetMapping("/my-rentals")
    public String myRentals(HttpSession session, Model model) {
        try {
            User user = (User) session.getAttribute("user");
            if (user == null) {
                return "redirect:/login";
            }

            User currentUser = userService.findByEmail(user.getEmail());
            var rentals = rentalService.getUserRentals(currentUser.getId());

            model.addAttribute("rentals", rentals);
            return "my-rentals";
        } catch (Exception e) {
            model.addAttribute("error", "Ошибка загрузки аренд");
            return "my-rentals";
        }
    }
}