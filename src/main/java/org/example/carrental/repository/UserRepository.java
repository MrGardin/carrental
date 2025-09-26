package org.example.carrental.repository;

import org.example.carrental.entity.User;
import org.example.carrental.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    // Найти пользователя по email
    Optional<User> findByEmail(String email);

    // Проверить существует ли пользователь с таким email
    boolean existsByEmail(String email);

    // Найти всех пользователей по роли
    List<User> findByRole(UserRole role);

    // Найти всех клиентов
    default List<User> findAllClients() {
        return findByRole(UserRole.CLIENT);
    }

    // Найти всех менеджеров
    default List<User> findAllManagers() {
        return findByRole(UserRole.MANAGER);
    }

    // Найти всех админов
    default List<User> findAllAdmins() {
        return findByRole(UserRole.ADMIN);
    }

    // Найти пользователей по имени (поиск)
    List<User> findByFullNameContainingIgnoreCase(String name);
}