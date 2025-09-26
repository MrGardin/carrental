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

@Controller
@RequestMapping("/admin/cars")
public class PhotoUploadController {

    @Autowired
    private CarService carService;

    @Autowired
    private FileStorageService fileStorageService;

    // Форма для загрузки фото
    @GetMapping("/upload-photo/{carId}")
    public String uploadPhotoForm(@PathVariable Long carId,
                                  HttpSession session,
                                  Model model) {
        User currentUser = (User) session.getAttribute("user");

        if (currentUser == null || (!currentUser.isManager() && !currentUser.isAdmin())) {
            return "redirect:/login";
        }

        Car car = carService.getCarById(carId);
        model.addAttribute("car", car);
        model.addAttribute("currentUser", currentUser);
        return "upload-photo";
    }

    // Обработка загрузки фото
    @PostMapping("/upload-photo/{carId}")
    public String uploadPhoto(@PathVariable Long carId,
                              @RequestParam("imageFile") MultipartFile imageFile,
                              HttpSession session,
                              RedirectAttributes redirectAttributes) {

        User currentUser = (User) session.getAttribute("user");

        try {
            if (currentUser == null || (!currentUser.isManager() && !currentUser.isAdmin())) {
                throw new RuntimeException("Доступ запрещен");
            }

            Car car = carService.getCarById(carId);

            if (imageFile != null && !imageFile.isEmpty()) {
                String filePath = fileStorageService.storeFile(imageFile);
                car.setImageUrl(filePath);
                carService.updateCarPhoto(carId, filePath);

                redirectAttributes.addFlashAttribute("success", "Фото успешно загружено!");
            } else {
                redirectAttributes.addFlashAttribute("error", "Файл не выбран");
            }

            return "redirect:/admin/cars";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Ошибка загрузки: " + e.getMessage());
            return "redirect:/admin/cars/upload-photo/" + carId;
        }
    }
}