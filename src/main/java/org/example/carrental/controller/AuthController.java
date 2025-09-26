package org.example.carrental.controller;

import org.example.carrental.entity.User;
import org.example.carrental.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import jakarta.servlet.http.HttpSession;

@Controller
public class AuthController {

    @Autowired
    private UserService userService;

    // Страница входа
    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    // Обработка входа
    @PostMapping("/login")
    public String login(@RequestParam String email,
                        @RequestParam String password,
                        HttpSession session,
                        Model model) {
        try {
            User user = userService.authenticate(email, password);
            session.setAttribute("user", user);
            return "redirect:/";
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            return "login";
        }
    }

    // Выход
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.removeAttribute("user");
        return "redirect:/";
    }

    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }

    @PostMapping(value = "/register", produces = "text/html;charset=UTF-8")
    public String register(@RequestParam String email,
                           @RequestParam String password,
                           @RequestParam String fullName,
                           @RequestParam String phone,
                           @RequestParam String role,
                           @RequestParam(required = false) String driverLicense,
                           HttpSession session,
                           Model model) {
        try {
            // Исправляем кодировку русских символов
            String correctedName = new String(fullName.getBytes("ISO-8859-1"), "UTF-8");
            String correctedPhone = new String(phone.getBytes("ISO-8859-1"), "UTF-8");
            String correctedDriverLicense = driverLicense != null ?
                    new String(driverLicense.getBytes("ISO-8859-1"), "UTF-8") : null;

            User newUser;
            if ("CLIENT".equals(role)) {
                if (correctedDriverLicense == null || correctedDriverLicense.trim().isEmpty()) {
                    throw new RuntimeException("Водительские права обязательны для клиента!");
                }
                newUser = userService.registerClient(email, password, correctedName, correctedPhone, correctedDriverLicense);
            } else {
                newUser = userService.registerManager(email, password, correctedName, correctedPhone);
            }

            session.setAttribute("user", newUser);
            return "redirect:/";

        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            return "register";
        } catch (Exception e) {
            model.addAttribute("error", "Ошибка кодировки: " + e.getMessage());
            return "register";
        }
    }
}