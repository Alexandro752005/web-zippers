package pe.com.zippers.service;

import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;
import pe.com.zippers.domain.Producto;
import pe.com.zippers.domain.ProductoFoto;
import pe.com.zippers.domain.ProductoVariante;
import pe.com.zippers.web.dto.ProductoFormDTO;
import pe.com.zippers.web.dto.ProductoListItemDTO;
import pe.com.zippers.web.dto.ProductoResumenDTO;

import java.util.List;

public interface ProductoService {

    ProductoResumenDTO resumen();

    Page<ProductoListItemDTO> listar(String q, String estado, String marca,
                                     String temporada, int page, int size,
                                     String sort, String dir);

    ProductoFormDTO obtenerParaEditar(Long idProducto);

    Producto obtenerDetalle(Long idProducto);
    List<ProductoVariante> variantesDe(Long idProducto);
    List<ProductoFoto> fotosDe(Long idProducto);

    Long crear(ProductoFormDTO dto);
    void editar(ProductoFormDTO dto);
    void cambiarEstado(Long idProducto, String nuevoEstado);

    void agregarFoto(Long idProducto, MultipartFile file, boolean principal);
}