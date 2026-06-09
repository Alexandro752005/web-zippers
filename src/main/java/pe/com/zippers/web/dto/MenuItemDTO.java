package pe.com.zippers.web.dto;

import lombok.Getter;
import lombok.Setter;
import java.util.ArrayList;
import java.util.List;

/** Ítem de menú resuelto por permisos, con soporte de jerarquía (hijos). */
@Getter
@Setter
public class MenuItemDTO {
    private Long idOpcion;
    private String codigo;
    private String nombre;
    private String ruta;
    private String icono;
    private Integer orden;
    private List<MenuItemDTO> hijos = new ArrayList<>();

    public boolean isTienePadre() {
        return false;
    }
}