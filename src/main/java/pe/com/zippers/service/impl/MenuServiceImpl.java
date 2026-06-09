package pe.com.zippers.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.com.zippers.common.Constants;
import pe.com.zippers.domain.Opcion;
import pe.com.zippers.domain.Permiso;
import pe.com.zippers.repository.OpcionRepository;
import pe.com.zippers.repository.PermisoRepository;
import pe.com.zippers.service.MenuService;
import pe.com.zippers.web.dto.MenuItemDTO;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Construye el menú con jerarquía padre/hijo:
 *  - Solo opciones ACTIVAS y visible_menu=1.
 *  - Solo opciones con permiso_ver=true para el rol.
 *  - Un padre se muestra si él o alguno de sus hijos es visible.
 */
@Service
@RequiredArgsConstructor
public class MenuServiceImpl implements MenuService {

    private final OpcionRepository opcionRepository;
    private final PermisoRepository permisoRepository;

    @Override
    @Transactional(readOnly = true)
    public List<MenuItemDTO> menuPara(Long idRol) {

        // Opciones con permiso_ver = true (y permiso activo) para el rol.
        Set<Long> opcionesVisibles = permisoRepository.findActivosByRol(idRol).stream()
                .filter(p -> Boolean.TRUE.equals(p.getPermisoVer()))
                .map(Permiso::getIdOpcion)
                .collect(Collectors.toSet());

        // Opciones candidatas: activas, visibles en menú y con permiso de ver.
        List<Opcion> candidatas = opcionRepository.findAll().stream()
                .filter(o -> Constants.ESTADO_ACTIVO.equals(o.getEstado()))
                .filter(o -> Boolean.TRUE.equals(o.getVisibleMenu()))
                .filter(o -> opcionesVisibles.contains(o.getIdOpcion()))
                .sorted(Comparator.comparingInt(o -> o.getOrden() == null ? 0 : o.getOrden()))
                .toList();

        // Construcción de árbol (un nivel de anidamiento para el sidebar).
        Map<Long, MenuItemDTO> porId = new LinkedHashMap<>();
        for (Opcion o : candidatas) {
            porId.put(o.getIdOpcion(), aDto(o));
        }

        List<MenuItemDTO> raiz = new ArrayList<>();
        for (Opcion o : candidatas) {
            MenuItemDTO dto = porId.get(o.getIdOpcion());
            Long padre = o.getIdOpcionPadre();
            if (padre != null && porId.containsKey(padre)) {
                porId.get(padre).getHijos().add(dto);
            } else {
                raiz.add(dto);
            }
        }
        return raiz;
    }

    private MenuItemDTO aDto(Opcion o) {
        MenuItemDTO dto = new MenuItemDTO();
        dto.setIdOpcion(o.getIdOpcion());
        dto.setCodigo(o.getCodigo());
        dto.setNombre(o.getNombre());
        dto.setRuta(o.getRuta());
        dto.setIcono(o.getIcono());
        dto.setOrden(o.getOrden());
        return dto;
    }
}