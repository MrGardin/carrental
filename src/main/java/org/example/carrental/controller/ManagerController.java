package org.example.carrental.controller;

import org.example.carrental.entity.Car;
import org.example.carrental.entity.Rental;
import org.example.carrental.entity.User;
import org.example.carrental.service.CarService;
import org.example.carrental.service.FileStorageService;
import org.example.carrental.service.RentalService;
import org.example.carrental.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.servlet.http.HttpSession;
import java.util.List;

@Controller
@RequestMapping("/manager")
public class ManagerController {

    @Autowired
    private CarService carService;

    @Autowired
    private RentalService rentalService;

    @Autowired
    private UserService userService;

    @Autowired
    private FileStorageService fileStorageService;

    // Главная страница менеджера - его автомобили и аренды
    @GetMapping("/dashboard")
    public String managerDashboard(HttpSession session, Model model) {
        User currentUser = (User) session.getAttribute("user");

        if (currentUser == null || !currentUser.isManager()) {
            return "redirect:/login";
        }

        // Автомобили менеджера
        List<Car> myCars = carService.getCarsByManager(currentUser);

        // Аренды для автомобилей менеджера
        List<Rental> pendingRentals = rentalService.getPendingRentalsByManager(currentUser.getId());
        List<Rental> activeRentals = rentalService.getActiveRentalsByManager(currentUser.getId());

        model.addAttribute("currentUser", currentUser);
        model.addAttribute("cars", myCars);
        model.addAttribute("pendingRentals", pendingRentals);
        model.addAttribute("activeRentals", activeRentals);
        model.addAttribute("pendingCount", pendingRentals.size());
        model.addAttribute("activeCount", activeRentals.size());

        return "manager-dashboard";
    }

    // Страница управления арендами
    @GetMapping("/rentals")
    public String managerRentalsPage(HttpSession session, Model model) {
        User currentUser = (User) session.getAttribute("user");

        if (currentUser == null || !currentUser.isManager()) {
            return "redirect:/login";
        }

        List<Rental> pendingRentals = rentalService.getPendingRentalsByManager(currentUser.getId());
        List<Rental> activeRentals = rentalService.getActiveRentalsByManager(currentUser.getId());
        List<Rental> allRentals = rentalService.getAllRentalsByManager(currentUser.getId());

        model.addAttribute("currentUser", currentUser);
        model.addAttribute("pendingRentals", pendingRentals);
        model.addAttribute("activeRentals", activeRentals);
        model.addAttribute("allRentals", allRentals);

        return "manager-rentals";
    }

    // Одобрение аренды
    @PostMapping("/rentals/{rentalId}/approve")
    public String approveRental(@PathVariable Long rentalId,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {
        User currentUser = (User) session.getAttribute("user");

        if (currentUser == null || !currentUser.isManager()) {
            return "redirect:/login";
        }

        try {
            Rental rental = rentalService.approveRental(rentalId, currentUser.getId());
            redirectAttributes.addFlashAttribute("success",
                    "Аренда автомобиля " + rental.getCar().getBrand() + " " + rental.getCar().getModel() + " одобрена!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Ошибка при одобрении: " + e.getMessage());
        }

        return "redirect:/manager/rentals";
    }

    // Отклонение аренды
    @PostMapping("/rentals/{rentalId}/reject")
    public String rejectRental(@PathVariable Long rentalId,
                               @RequestParam(required = false) String reason,
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {
        User currentUser = (User) session.getAttribute("user");

        if (currentUser == null || !currentUser.isManager()) {
            return "redirect:/login";
        }

        try {
            Rental rental = rentalService.rejectRental(rentalId, currentUser.getId(), reason);
            redirectAttributes.addFlashAttribute("success",
                    "Аренда автомобиля " + rental.getCar().getBrand() + " " + rental.getCar().getModel() + " отклонена.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Ошибка при отклонении: " + e.getMessage());
        }

        return "redirect:/manager/rentals";
    }

    // Завершение аренды
    @PostMapping("/rentals/{rentalId}/complete")
    public String completeRental(@PathVariable Long rentalId,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {
        User currentUser = (User) session.getAttribute("user");

        if (currentUser == null || !currentUser.isManager()) {
            return "redirect:/login";
        }

        try {
            Rental rental = rentalService.completeRental(rentalId, currentUser.getId());
            redirectAttributes.addFlashAttribute("success",
                    "Аренда автомобиля " + rental.getCar().getBrand() + " " + rental.getCar().getModel() + " завершена.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Ошибка при завершении: " + e.getMessage());
        }

        return "redirect:/manager/rentals";
    }

    // Детали аренды для менеджера
    @GetMapping("/rentals/{rentalId}")
    public String rentalDetails(@PathVariable Long rentalId,
                                HttpSession session,
                                Model model) {
        User currentUser = (User) session.getAttribute("user");

        if (currentUser == null || !currentUser.isManager()) {
            return "redirect:/login";
        }

        try {
            Rental rental = rentalService.getRentalById(rentalId);

            // Проверяем что аренда относится к автомобилю менеджера
            if (!rental.getCar().getManager().getId().equals(currentUser.getId())) {
                return "redirect:/manager/rentals?error=access_denied";
            }

            model.addAttribute("currentUser", currentUser);
            model.addAttribute("rental", rental);
            return "manager-rental-details";
        } catch (Exception e) {
            return "redirect:/manager/rentals?error=rental_not_found";
        }
    }

    // Остальные методы для управления автомобилями (оставляем без изменений)
    @GetMapping("/cars")
    public String managerCarsPage(HttpSession session, Model model) {
        User currentUser = (User) session.getAttribute("user");

        if (currentUser == null || !currentUser.isManager()) {
            return "redirect:/login";
        }

        List<Car> myCars = carService.getCarsByManager(currentUser);
        long availableCount = myCars.stream().filter(Car::getAvailable).count();
        long occupiedCount = myCars.stream().filter(c -> !c.getAvailable()).count();

        model.addAttribute("currentUser", currentUser);
        model.addAttribute("cars", myCars);
        model.addAttribute("availableCount", availableCount);
        model.addAttribute("occupiedCount", occupiedCount);
        model.addAttribute("totalCount", myCars.size());

        return "manager-cars";
    }

    @GetMapping("/cars/add")
    public String addCarForm(HttpSession session, Model model) {
        User currentUser = (User) session.getAttribute("user");

        if (currentUser == null || !currentUser.isManager()) {
            return "redirect:/login";
        }

        model.addAttribute("currentUser", currentUser);
        model.addAttribute("car", new Car());
        return "add-car";
    }

    @GetMapping("/cars/upload-photo/{id}")
    public String uploadPhotoForm(@PathVariable Long id, HttpSession session, Model model) {
        User currentUser = (User) session.getAttribute("user");

        if (currentUser == null || !currentUser.isManager()) {
            return "redirect:/login";
        }

        Car car = carService.getCarById(id);

        if (!car.getManager().getId().equals(currentUser.getId())) {
            return "redirect:/manager/cars";
        }

        model.addAttribute("currentUser", currentUser);
        model.addAttribute("car", car);
        return "upload-photo";
    }

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
        if (currentUser == null || !currentUser.isManager()) {
            return "redirect:/login";
        }

        try {
            String decodedBrand = new String(brand.getBytes("ISO-8859-1"), "UTF-8");
            String decodedModel = new String(model.getBytes("ISO-8859-1"), "UTF-8");
            String decodedColor = new String(color.getBytes("ISO-8859-1"), "UTF-8");
            String decodedFuelType = new String(fuelType.getBytes("ISO-8859-1"), "UTF-8");
            String decodedTransmission = new String(transmission.getBytes("ISO-8859-1"), "UTF-8");
            String decodedBodyType = new String(bodyType.getBytes("ISO-8859-1"), "UTF-8");
            String decodedVin = new String(vin.getBytes("ISO-8859-1"), "UTF-8");

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

            if (imageFile != null && !imageFile.isEmpty()) {
                try {
                    String filePath = fileStorageService.storeFile(imageFile);
                    car.setImageUrl(filePath);
                } catch (Exception e) {
                    car.setImageUrl("/images/cars/default-car.jpg");
                }
            } else {
                car.setImageUrl("/images/cars/default-car.jpg");
            }

            car.setManager(currentUser);
            Car savedCar = carService.addCar(car, currentUser);
            redirectAttributes.addFlashAttribute("success", "Автомобиль успешно добавлен!");
            return "redirect:/manager/cars";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Ошибка при добавлении: " + e.getMessage());
            return "redirect:/manager/cars/add";
        }
    }
}