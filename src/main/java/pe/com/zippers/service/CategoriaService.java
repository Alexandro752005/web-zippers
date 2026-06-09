package pe.com.zippers.service;

import org.springframework.data.domain.Page;
import pe.com.zippers.web.dto.CategoriaFormDTO;
import pe.com.zippers.web.dto.CategoriaListItemDTO;

public interface CategoriaService {

    Page<CategoriaListItemDTO> listar(String filtro, int page, int size, String sort, String dir);

    CategoriaFormDTO obtenerParaEditar(Long idCategoria);

    Long crear(CategoriaFormDTO dto);

    void editar(CategoriaFormDTO dto);

    /** Soft delete / cambio de estado con validación de dependencias. */
    void cambiarEstado(Long idCategoria, String nuevoEstado);
}