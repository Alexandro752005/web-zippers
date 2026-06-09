package pe.com.zippers.web;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import pe.com.zippers.common.Constants;
import pe.com.zippers.repository.UsuarioRepository;
import pe.com.zippers.security.SecurityUtils;
import pe.com.zippers.security.UsuarioPrincipal;
import pe.com.zippers.service.MenuService;

/**
 * Shell del backoffice. Resuelve el menú dinámico por permisos
 * y entrega el módulo inicial. Todo navega bajo /dashboard.
 */
@Controller
@RequiredArgsConstructor
public class DashboardController {

    private final MenuService menuService;
    private final UsuarioRepository usuarioRepository;

    @GetMapping(Constants.DASHBOARD)
    public String dashboard(@RequestParam(value = "modulo", required = false) String modulo,
                            Model model,
                            HttpServletRequest request) { // Se inyecta el request para detectar HTMX

        UsuarioPrincipal p = SecurityUtils.actual();
        if (p != null) {
            Long idRol = usuarioRepository.findByUsername(p.getUsername())
                    .map(u -> u.getRol().getIdRol()).orElse(null);
            if (idRol != null) {
                model.addAttribute("menu", menuService.menuPara(idRol));
            }
            model.addAttribute("usuarioNombre", p.getNombreCompleto());
            model.addAttribute("usuarioRol", p.getRolCodigo());
        }

        String mod = (modulo == null) ? "home" : modulo;
        model.addAttribute("moduloInicial", mod);

        // ==============================================================================
        // ARQUITECTURA LIMPIA: La pre-carga masiva de datos ha sido eliminada.
        // Ahora el Dashboard delega el trabajo al Lazy Loading de HTMX en la vista.
        // ==============================================================================

        // MAGIA HTMX: Si es un clic desde el menú lateral, devolvemos SOLO el contenedor de sección
        if ("true".equals(request.getHeader(Constants.HX_REQUEST))) {
            return "dashboard :: seccion";
        }

        // Si es una recarga completa en el navegador (F5), devolvemos el layout completo
        return "dashboard";
    }

    // FIX 2: Estandarización de home en Sidebar y Controlador
    // Ahora el home es congruente con el despachador: /modulo/home?action=list
    @GetMapping("/modulo/home")
    @PreAuthorize("isAuthenticated()")
    public String homeFragmento(@RequestParam(defaultValue = "list") String action,
                                HttpServletRequest request) {

        // Si en el futuro se requiere lógica por acción (list, create, etc.), usar "action" aquí.
        request.setAttribute(Constants.ATTR_FORWARD_INTERNO, Boolean.TRUE);
        return "modules/home :: home";
    }
}