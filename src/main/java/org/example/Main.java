package org.example;

import org.example.carrental.config.DatabaseConfig;
import org.example.carrental.config.WebConfig;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;
import org.apache.catalina.Context;
import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.Wrapper;

import jakarta.servlet.MultipartConfigElement;

public class Main {
    public static void main(String[] args) throws Exception {
        // Создаем веб-контекст Spring
        AnnotationConfigWebApplicationContext context = new AnnotationConfigWebApplicationContext();
        context.register(DatabaseConfig.class, WebConfig.class);

        // Создаем и настраиваем Tomcat
        Tomcat tomcat = new Tomcat();
        tomcat.setPort(8080);
        tomcat.getConnector(); // Активируем коннектор

        // Создаем контекст Tomcat
        String contextPath = "";
        String docBase = new java.io.File(".").getAbsolutePath();
        Context tomcatContext = tomcat.addContext(contextPath, docBase);

        // Создаем и регистрируем DispatcherServlet
        DispatcherServlet dispatcherServlet = new DispatcherServlet(context);

        // ВАЖНО: Получаем Wrapper для настройки MultipartConfig
        Wrapper dispatcherWrapper = Tomcat.addServlet(tomcatContext, "dispatcher", dispatcherServlet);

        // НАСТРАИВАЕМ MULTIPART CONFIG - это ключевое!
        dispatcherWrapper.setMultipartConfigElement(new MultipartConfigElement(
                "", // временная директория
                10 * 1024 * 1024, // maxFileSize - 10MB
                50 * 1024 * 1024, // maxRequestSize - 50MB
                1 * 1024 * 1024 // fileSizeThreshold - 1MB
        ));

        tomcatContext.addServletMappingDecoded("/*", "dispatcher");

        // Запускаем сервер
        tomcat.start();
        System.out.println("🚗 CarRental Web Application запущена на http://localhost:8080");
        System.out.println("📋 API доступно по адресам:");
        System.out.println("   GET  http://localhost:8080/api/cars");
        System.out.println("   GET  http://localhost:8080/api/cars/available");
        System.out.println("✅ Загрузка файлов настроена!");

        tomcat.getServer().await();
    }
}