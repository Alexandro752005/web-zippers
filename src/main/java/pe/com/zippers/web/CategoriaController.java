package pe.com.zippers.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import pe.com.zippers.common.Constants;
import pe.com.zippers.common.exception.BusinessException;
import pe.com.zippers.common.util.HtmxUtils;
import pe.com.zippers.service.CategoriaService;
import pe.com.zippers.web.dto.CategoriaFormDTO;
import pe.com.zippers.web.dto.CategoriaListItemDTO;

@Controller
@RequestMapping("/modulo/categorias")
public class CategoriaController {

    private final CategoriaService categoriaService;

    public CategoriaController(CategoriaService categoriaService) {
        this.categoriaService = categoriaService;
    }

    private void cargarTabla(Model model, String q, int page, int size, String sort, String dir) {
        Page<CategoriaListItemDTO> pagina = categoriaService.listar(q, page, size, sort, dir);
        model.addAttribute("pagina", pagina);
        model.addAttribute("categorias", pagina.getContent());
        model.addAttribute("filtro", q == null ? "" : q);
        model.addAttribute("size", size);
        model.addAttribute("sort", sort);
        model.addAttribute("dir", dir);
        model.addAttribute("pageActual", pagina.getNumber());
        model.addAttribute("totalPaginas", pagina.getTotalPages());
        model.addAttribute("totalRegistros", pagina.getTotalElements());
    }

    @GetMapping
    @PreAuthorize("hasAuthority('CATEGORIAS_VER') or hasRole('ADMIN')")
    public String get(@RequestParam(defaultValue = "list") String action,
                      @RequestParam(required = false) Long id,
                      @RequestParam(required = false) String q,
                      @RequestParam(defaultValue = "0") int page,
                      @RequestParam(defaultValue = "10") int size,
                      @RequestParam(defaultValue = "idCategoria") String sort,
                      @RequestParam(defaultValue = "asc") String dir,
                      Model model, 
                      HttpServletRequest request) {
        
        // Marca el acceso como interno (evita redirección del guard al refrescar el shell)
        request.setAttribute(Constants.ATTR_FORWARD_INTERNO, Boolean.TRUE);
        
        switch (action) {
            case "table":
                cargarTabla(model, q, page, size, sort, dir);
                return "modules/categorias/table :: tabla";
            case "form":
                if (id != null) {
                    model.addAttribute("categoria", categoriaService.obtenerParaEditar(id));
                } else {
                    CategoriaFormDTO nuevo = new CategoriaFormDTO();
                    nuevo.setEstado("ACTIVO");
                    model.addAttribute("categoria", nuevo);
                }
                return "modules/categorias/form :: modal";
            case "list":
            default:
                cargarTabla(model, "", 0, 10, "idCategoria", "asc");
                return "modules/categorias/list :: modulo";
        }
    }

    @PostMapping
    @PreAuthorize("hasAuthority('CATEGORIAS_VER') or hasRole('ADMIN')")
    public String post(@RequestParam String action,
                       @Valid @ModelAttribute("categoria") CategoriaFormDTO categoria,
                       BindingResult binding,
                       @RequestParam(required = false) Long idCategoria,
                       Model model, 
                       HttpServletRequest request) {
        
        request.setAttribute(Constants.ATTR_FORWARD_INTERNO, Boolean.TRUE);
        
        switch (action) {
            case "save-crear":
            case "save-editar": {
                if (binding.hasErrors()) {
                    model.addAttribute("categoria", categoria);
                    return "modules/categorias/form :: modal";
                }
                String msg;
                if ("save-crear".equals(action)) {
                    categoriaService.crear(categoria);
                    msg = "Categoría creada correctamente";
                } else {
                    categoriaService.editar(categoria);
                    msg = "Categoría actualizada correctamente";
                }
                cargarTabla(model, "", 0, 10, "idCategoria", "asc");
                HtmxUtils.trigger(request, """
                        {"zpToast":{"tipo":"success","msg":"%s"},"zpClose":true}""".formatted(msg));
                return "modules/categorias/table :: tabla";
            }
            case "toggle": {
                CategoriaFormDTO actual = categoriaService.obtenerParaEditar(idCategoria);
                String nuevo = "ACTIVO".equals(actual.getEstado()) ? "INACTIVO" : "ACTIVO";
                categoriaService.cambiarEstado(idCategoria, nuevo);
                cargarTabla(model, "", 0, 10, "idCategoria", "asc");
                HtmxUtils.trigger(request, """
                        {"zpToast":{"tipo":"success","msg":"Estado actualizado"},"zpClose":true}""");
                return "modules/categorias/table :: tabla";
            }
            default:
                throw new BusinessException("Acción no soportada: " + action);
        }
    }
}