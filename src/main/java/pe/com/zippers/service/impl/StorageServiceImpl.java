package pe.com.zippers.service.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import pe.com.zippers.common.exception.BusinessException;
import pe.com.zippers.service.StorageService;

import java.io.IOException;
import java.nio.file.*;
import java.util.Set;
import java.util.UUID;

@Service
public class StorageServiceImpl implements StorageService {

    private static final long MAX_BYTES = 14L * 1024 * 1024; // 14 MB
    private static final Set<String> TIPOS = Set.of(
            "image/jpeg", "image/png", "image/webp");

    @Value("${zippers.upload-dir:uploads/productos}")
    private String uploadDir;

    @Override
    public String guardarImagenProducto(MultipartFile file, Long idProducto) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("La imagen está vacía");
        }
        if (file.getSize() > MAX_BYTES) {
            throw new BusinessException("La imagen supera el máximo permitido de 14 MB");
        }
        String contentType = file.getContentType();
        if (contentType == null || !TIPOS.contains(contentType)) {
            throw new BusinessException("Formato no permitido. Use JPG, PNG o WEBP");
        }
        try {
            Path dir = Paths.get(uploadDir).toAbsolutePath().normalize();
            Files.createDirectories(dir);
            String ext = switch (contentType) {
                case "image/png" -> ".png";
                case "image/webp" -> ".webp";
                default -> ".jpg";
            };
            String nombre = "p" + idProducto + "_" + UUID.randomUUID() + ext;
            Path destino = dir.resolve(nombre);
            file.transferTo(destino.toFile());
            // URL pública servida por WebConfig (resource handler)
            return "/uploads/productos/" + nombre;
        } catch (IOException e) {
            throw new BusinessException("No se pudo guardar la imagen: " + e.getMessage());
        }
    }
}