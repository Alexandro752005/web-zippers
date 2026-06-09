package pe.com.zippers.service;

import pe.com.zippers.web.dto.MenuItemDTO;
import java.util.List;

/** Construye el menú lateral según los permisos (permiso_ver) del rol. */
public interface MenuService {
    List<MenuItemDTO> menuPara(Long idRol);
}