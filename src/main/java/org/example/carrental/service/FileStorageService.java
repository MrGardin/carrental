package org.example.carrental.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class FileStorageService {

    // Папка где будем хранить загруженные фото
    private final Path fileStorageLocation;

    public FileStorageService() {
        // Указываем папку для загрузок (создастся автоматически)
        this.fileStorageLocation = Paths.get("uploads/images/cars")
                .toAbsolutePath().normalize();

        try {
            // Создаем папку если ее нет
            Files.createDirectories(this.fileStorageLocation);
            System.out.println("Папка для загрузок создана: " + this.fileStorageLocation);
        } catch (Exception ex) {
            throw new RuntimeException("Не удалось создать директорию для загрузки файлов", ex);
        }
    }

    // Метод для сохранения файла
    public String storeFile(MultipartFile file) {
        try {
            // Проверяем что файл не пустой
            if (file.isEmpty()) {
                throw new RuntimeException("Файл пустой");
            }

            // Проверяем что это изображение
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                throw new RuntimeException("Файл должен быть изображением");
            }

            // Генерируем уникальное имя файла чтобы избежать конфликтов
            String originalFileName = file.getOriginalFilename();
            String fileExtension = "";

            if (originalFileName != null && originalFileName.contains(".")) {
                fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
            }

            String fileName = UUID.randomUUID().toString() + fileExtension;

            // Сохраняем файл на диск
            Path targetLocation = this.fileStorageLocation.resolve(fileName);
            Files.copy(file.getInputStream(), targetLocation);

            System.out.println("Файл сохранен: " + targetLocation);

            // Возвращаем путь для доступа через браузер
            return "/uploads/images/cars/" + fileName;

        } catch (IOException ex) {
            throw new RuntimeException("Не удалось сохранить файл: " + ex.getMessage(), ex);
        }
    }
}