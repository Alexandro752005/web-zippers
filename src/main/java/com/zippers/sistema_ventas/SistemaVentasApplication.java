package com.zippers.sistema_ventas;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootApplication
public class SistemaVentasApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(SistemaVentasApplication.class, args);
    }
    // Este bloque de código se ejecutará automáticamente al encender el proyecto
    @Bean
    public CommandLineRunner generarPassword() {
        return args -> {
            String miHash = new BCryptPasswordEncoder().encode("admin123");
            System.out.println("\n=======================================================");
            System.out.println("COPIA ESTE HASH EXACTO EN TU MYSQL PARA admin123:");
            System.out.println(miHash);
            System.out.println("=======================================================\n");
        };
    }
}