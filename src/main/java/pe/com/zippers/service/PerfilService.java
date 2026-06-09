package pe.com.zippers.service;

import pe.com.zippers.web.dto.MatrizPermisoDTO;
import pe.com.zippers.web.dto.PerfilFormDTO;
import pe.com.zippers.web.dto.PerfilListItemDTO;

import java.util.List;

public interface PerfilService {

    List<PerfilListItemDTO> listar(String filtro);

    PerfilFormDTO obtenerParaEditar(Long idRol);

    /** Crea un perfil nuevo (siempre ACTIVO). Devuelve el id generado. */
    Long crear(PerfilFormDTO dto);

    /** Edita nombre/descripcion/estado de un perfil existente. */
    void editar(PerfilFormDTO dto);

    /** Cambia estado (toggle) respetando el bloqueo del perfil base. */
    void cambiarEstado(Long idRol, String nuevoEstado);

    /** Matriz de permisos del rol, con TODAS las opciones (checked si ya tiene permiso). */
    List<MatrizPermisoDTO> obtenerMatriz(Long idRol);

    /** Guarda la matriz: borra permisos del rol y reinserta según payload (transaccional). */
    void guardarMatriz(Long idRol, List<MatrizPermisoDTO> filas);
}