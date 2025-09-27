package org.example.carrental.service;

import jakarta.annotation.PostConstruct;
import org.example.carrental.entity.User;
import org.example.carrental.entity.UserRole;
import org.example.carrental.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // РЕГИСТРАЦИЯ КЛИЕНТА
    public User registerClient(String email, String password, String fullName, String phone, String driverLicense) {
        // 1. ПРОВЕРКА УНИКАЛЬНОСТИ EMAIL
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Пользователь с email " + email + " уже существует!");
        }

        // 2. ВАЛИДАЦИЯ ДАННЫХ КЛИЕНТА
        if (driverLicense == null || driverLicense.trim().isEmpty()) {
            throw new RuntimeException("Водительские права обязательны для клиента!");
        }

        // 3. СОЗДАНИЕ КЛИЕНТА (пароль без шифрования пока)
        User client = new User(email, password, fullName, phone, driverLicense);
        return userRepository.save(client);
    }

    // РЕГИСТРАЦИЯ МЕНЕДЖЕРА (ожидает одобрения админа)
    public User registerManager(String email, String password, String fullName, String phone) {
        // 1. ПРОВЕРКА УНИКАЛЬНОСТИ EMAIL
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Пользователь с email " + email + " уже существует!");
        }

        // 2. ФИКС: Используем правильный конструктор для менеджера
        User manager = new User(email, password, fullName, phone);
        manager.setRole(UserRole.MANAGER); // Явно устанавливаем роль
        return userRepository.save(manager);
    }

    // ОДОБРЕНИЕ МЕНЕДЖЕРА АДМИНОМ
    public User approveManager(Long managerId, User admin) {
        // 1. ПРОВЕРКА ЧТО ЭТО АДМИН
        if (!admin.isAdmin()) {
            throw new RuntimeException("Только администратор может одобрять менеджеров!");
        }

        User manager = getUserById(managerId);

        // 2. ПРОВЕРКА ЧТО ЭТО МЕНЕДЖЕР
        if (!manager.isManager()) {
            throw new RuntimeException("Можно одобрять только менеджеров!");
        }

        // 3. ОДОБРЕНИЕ
        // manager.setApproved(true);
        return userRepository.save(manager);
    }
    // В UserService добавить:
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
    // АУТЕНТИФИКАЦИЯ (простая проверка)
    public User authenticate(String email, String password) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("Пользователь не найден!");
        }

        User user = userOpt.get();

        // Простая проверка пароля (без шифрования)
        if (!password.equals(user.getPassword())) {
            throw new RuntimeException("Неверный пароль!");
        }

        return user;
    }

    // БАЗОВЫЕ МЕТОДЫ
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден с ID: " + id));
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден с email: " + email));
    }

    // ОБНОВЛЕНИЕ ПРОФИЛЯ
    public User updateProfile(Long userId, String fullName, String phone, String driverLicense) {
        User user = getUserById(userId);

        user.setFullName(fullName);
        user.setPhone(phone);

        // Только для клиентов
        if (user.isClient() && driverLicense != null) {
            user.setDriverLicense(driverLicense);
        }

        return userRepository.save(user);
    }

    // ПОИСК И ФИЛЬТРАЦИЯ
    public List<User> getAllClients() {
        return userRepository.findAllClients();
    }

    public List<User> getAllManagers() {
        return userRepository.findAllManagers();
    }

    public List<User> searchUsersByName(String name) {
        if (name == null || name.trim().length() < 2) {
            throw new RuntimeException("Поисковый запрос должен содержать минимум 2 символа!");
        }
        return userRepository.findByFullNameContainingIgnoreCase(name);
    }
    // В UserService добавить метод
    @PostConstruct
    public void createTestUsers() {
        if (userRepository.count() == 0) {
            // Админ
            User admin = new User("admin@carrental.ru", "admin123", "Администратор Системы", UserRole.ADMIN);
            userRepository.save(admin);

            // Менеджер
            User manager = new User("manager@carrental.ru", "manager123", "Иван Менеджеров", UserRole.MANAGER);
            manager.setApproved(true); // ОДОБРЯЕМ МЕНЕДЖЕРА
            userRepository.save(manager);
            // Клиент
            User client = new User("client@carrental.ru", "client123", "Петр Клиентов",
                    "+79991234567", "AB12345678");
            userRepository.save(client);

            System.out.println("Созданы тестовые пользователи");
        }
    }
    // Добавь этот геттер в UserService
    public UserRepository getUserRepository() {
        return userRepository;
    }
    // В UserService.java добавляем:
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден: " + email));
    }
}