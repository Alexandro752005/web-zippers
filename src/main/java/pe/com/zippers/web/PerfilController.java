package pe.com.zippers.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import pe.com.zippers.common.Constants;
import pe.com.zippers.common.exception.BusinessException;
import pe.com.zippers.common.util.HtmxUtils;
import pe.com.zippers.service.PerfilService;
import pe.com.zippers.web.dto.MatrizPermisoDTO;
import pe.com.zippers.web.dto.PerfilFormDTO;

import java.util.ArrayList;
import java.util.List;

/**
 * Dispatcher HTMX del módulo Perfiles (bajo /dashboard, sin recarga).
 *   GET  /modulo/perfiles?action=list|table|form|matriz
 *   POST /modulo/perfiles?action=save-crear|save-editar|toggle|save-matriz
 *
 * FIX bug b:
 *   - GET pasa de @GetMapping("/list") a @GetMapping (dispatcher por ?action),
 *     de modo que action=form|matriz sean alcanzables → los 3 modales abren.
 *   - Cada GET marca el forward interno que exige HtmxGuardInterceptor para /modulo/.
 */
@Controller
@RequestMapping("/modulo/perfiles")
public class PerfilController {

    private final PerfilService perfilService;

    public PerfilController(PerfilService perfilService) {
        this.perfilService = perfilService;
    }

    // ============================ GET ============================
    @GetMapping
    @PreAuthorize("hasAuthority('PERFILES_VER') or hasRole('ADMIN')")
    public String get(@RequestParam(defaultValue = "list") String action,
                      @RequestParam(required = false) Long id,
                      @RequestParam(required = false) String q,
                      HttpServletRequest request,
                      Model model) {

        // OBLIGATORIO para pasar HtmxGuardInterceptor en URIs /modulo/*
        request.setAttribute(Constants.ATTR_FORWARD_INTERNO, Boolean.TRUE);

        switch (action) {
            case "table":
                model.addAttribute("perfiles", perfilService.listar(q));
                model.addAttribute("filtro", q);
                return "modules/perfiles/table :: tabla";

            case "form":
                if (id != null) {
                    model.addAttribute("perfil", perfilService.obtenerParaEditar(id));
                } else {
                    PerfilFormDTO nuevo = new PerfilFormDTO();
                    nuevo.setEstado("ACTIVO");
                    model.addAttribute("perfil", nuevo);
                }
                return "modules/perfiles/form :: modal";

            case "matriz":
                if (id == null) {
                    throw new BusinessException("Debe indicar el perfil");
                }
                model.addAttribute("idRol", id);
                model.addAttribute("matriz", perfilService.obtenerMatriz(id));
                model.addAttribute("perfil", perfilService.obtenerParaEditar(id));
                return "modules/perfiles/matriz :: modal";

            case "list":
            default:
                model.addAttribute("perfiles", perfilService.listar(null));
                model.addAttribute("filtro", "");
                return "modules/perfiles/list :: modulo";
        }
    }

    // ============================ POST ===========================
    // (idéntico a tu versión actual: setea forward interno opcional para coherencia)
    @PostMapping
    @PreAuthorize("hasAuthority('PERFILES_VER') or hasRole('ADMIN')")
    public String post(@RequestParam String action,
                       @Valid @ModelAttribute("perfil") PerfilFormDTO perfil,
                       BindingResult binding,
                       @RequestParam(required = false) Long idRol,
                       @RequestParam(required = false) List<Long> opcionId,
                       @RequestParam(required = false) List<Long> verIds,
                       @RequestParam(required = false) List<Long> crearIds,
                       @RequestParam(required = false) List<Long> editarIds,
                       @RequestParam(required = false) List<Long> eliminarIds,
                       @RequestParam(required = false) List<Long> exportarIds,
                       HttpServletRequest request,
                       Model model) {

        request.setAttribute(Constants.ATTR_FORWARD_INTERNO, Boolean.TRUE);

        switch (action) {
            case "save-crear":
            case "save-editar": {
                if (binding.hasErrors()) {
                    model.addAttribute("perfil", perfil);
                    return "modules/perfiles/form :: modal";
                }
                String msg;
                if ("save-crear".equals(action)) {
                    perfilService.crear(perfil);
                    msg = "Perfil creado correctamente";
                } else {
                    perfilService.editar(perfil);
                    msg = "Perfil actualizado correctamente";
                }
                model.addAttribute("perfiles", perfilService.listar(null));
                model.addAttribute("filtro", "");
                HtmxUtils.trigger(request, """
                        {"zpToast":{"tipo":"success","msg":"%s"},"zpClose":true}"""
                        .formatted(msg));
                return "modules/perfiles/table :: tabla";
            }

            case "toggle": {
                if (idRol == null) throw new BusinessException("Debe indicar el perfil");
                PerfilFormDTO actual = perfilService.obtenerParaEditar(idRol);
                String nuevo = "ACTIVO".equals(actual.getEstado()) ? "INACTIVO" : "ACTIVO";
                perfilService.cambiarEstado(idRol, nuevo);
                model.addAttribute("perfiles", perfilService.listar(null));
                model.addAttribute("filtro", "");
                HtmxUtils.trigger(request, """
                        {"zpToast":{"tipo":"success","msg":"Estado actualizado"},"zpClose":true}""");
                return "modules/perfiles/table :: tabla";
            }

            case "save-matriz": {
                if (idRol == null) throw new BusinessException("Debe indicar el perfil");
                List<MatrizPermisoDTO> filas = construirMatriz(
                        opcionId, verIds, crearIds, editarIds, eliminarIds, exportarIds);
                perfilService.guardarMatriz(idRol, filas);
                model.addAttribute("perfiles", perfilService.listar(null));
                model.addAttribute("filtro", "");
                HtmxUtils.trigger(request, """
                        {"zpToast":{"tipo":"success","msg":"Permisos actualizados"},"zpClose":true}""");
                return "modules/perfiles/table :: tabla";
            }

            default:
                throw new BusinessException("Acción no soportada: " + action);
        }
    }

    private List<MatrizPermisoDTO> construirMatriz(
            List<Long> opcionId, List<Long> verIds, List<Long> crearIds,
            List<Long> editarIds, List<Long> eliminarIds, List<Long> exportarIds) {

        List<MatrizPermisoDTO> filas = new ArrayList<>();
        if (opcionId == null) {
            return filas;
        }
        for (Long oid : opcionId) {
            MatrizPermisoDTO f = new MatrizPermisoDTO();
            f.setIdOpcion(oid);
            f.setVer(verIds != null && verIds.contains(oid));
            f.setCrear(crearIds != null && crearIds.contains(oid));
            f.setEditar(editarIds != null && editarIds.contains(oid));
            f.setEliminar(eliminarIds != null && eliminarIds.contains(oid));
            f.setExportar(exportarIds != null && exportarIds.contains(oid));
            filas.add(f);
        }
        return filas;
    }
}