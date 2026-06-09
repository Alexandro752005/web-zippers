package pe.com.zippers.service;

import pe.com.zippers.domain.Rol;
import pe.com.zippers.web.dto.UsuarioCreateDTO;
import pe.com.zippers.web.dto.UsuarioListItemDTO;
import pe.com.zippers.web.dto.UsuarioUpdateDTO;
import java.util.List;

public interface UsuarioService {

    List<UsuarioListItemDTO> listar(String q);

    UsuarioUpdateDTO obtenerParaEditar(Long idUsuario);

    /** Crea usuario. Devuelve id creado. Audita INSERT. */
    Long crear(UsuarioCreateDTO dto, Long ejecutorId);

    /** Actualiza usuario (password opcional). Audita UPDATE. */
    void actualizar(UsuarioUpdateDTO dto, Long ejecutorId);

    /** Soft delete: estado=INACTIVO. Audita UPDATE. */
    void desactivar(Long idUsuario, Long ejecutorId);

    List<Rol> rolesSeleccionables();
}