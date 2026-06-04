package com.zippers.sistema_ventas.service;

import com.zippers.sistema_ventas.entity.*;
import com.zippers.sistema_ventas.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.Objects;

@Service
public class ProductoService {

    private final ProductoRepository productoRepository;
    private final ProductoFotoRepository fotoRepository;

    private static final String UPLOAD_DIR = "uploads/productos/";

    public ProductoService(ProductoRepository productoRepository,
                           ProductoFotoRepository fotoRepository) {
        this.productoRepository = productoRepository;
        this.fotoRepository = fotoRepository;
    }

    @Transactional
    public Producto guardarProducto(Producto producto) {
        Objects.requireNonNull(producto, "El producto no puede ser nulo");
        return productoRepository.save(producto);
    }

    public long contarFotos(Integer idProducto) {
        Objects.requireNonNull(idProducto, "ID de producto requerido");
        return fotoRepository.countByProductoIdProducto(idProducto);
    }

    @Transactional
    public ProductoFoto subirFoto(Integer idProducto, MultipartFile archivo) throws IOException {
        Objects.requireNonNull(idProducto, "ID de producto requerido");
        Objects.requireNonNull(archivo, "Archivo requerido");
        if (contarFotos(idProducto) >= 5) {
            throw new IllegalStateException("Máximo 5 fotos por producto.");
        }
        String nombreArchivo = System.currentTimeMillis() + "_" + archivo.getOriginalFilename();
        Path ruta = Paths.get(UPLOAD_DIR, nombreArchivo);
        Files.createDirectories(ruta.getParent());
        Files.copy(archivo.getInputStream(), ruta, StandardCopyOption.REPLACE_EXISTING);

        Producto producto = productoRepository.findById(idProducto).orElseThrow();
        ProductoFoto foto = new ProductoFoto();
        foto.setProducto(producto);
        foto.setUrl_foto(nombreArchivo);
        foto.setEsPortada(false);
        return fotoRepository.save(foto);
    }

    @Transactional
    public void marcarPortada(Integer idFoto) {
        Objects.requireNonNull(idFoto, "ID de foto requerido");
        ProductoFoto foto = fotoRepository.findById(idFoto).orElseThrow();
        Integer idProd = foto.getProducto().getId_producto();
        fotoRepository.desmarcarPortadas(idProd);
        foto.setEsPortada(true);
        fotoRepository.save(foto);
    }

    @Transactional
    public void eliminarFoto(Integer idFoto) {
        Objects.requireNonNull(idFoto, "ID de foto requerido");
        fotoRepository.deleteById(idFoto);
    }
}