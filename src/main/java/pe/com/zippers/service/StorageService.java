package pe.com.zippers.service;

import org.springframework.web.multipart.MultipartFile;

public interface StorageService {
    /** Guarda la imagen y devuelve la URL pública (/uploads/productos/xxx.jpg). */
    String guardarImagenProducto(MultipartFile file, Long idProducto);
}