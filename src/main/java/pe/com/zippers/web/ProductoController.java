package pe.com.zippers.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pe.com.zippers.common.Constants;
import pe.com.zippers.common.exception.BusinessException;
import pe.com.zippers.common.util.HtmxUtils;
import pe.com.zippers.repository.CategoriaRepository;
import pe.com.zippers.service.ProductoService;
import pe.com.zippers.web.dto.ProductoFormDTO;
import pe.com.zippers.web.dto.ProductoListItemDTO;

/**
 * Dispatcher HTMX del módulo Productos (bajo /dashboard, sin recarga).
 *   GET  /modulo/productos?action=list|table|form|detalle
 *   POST /modulo/productos?action=save-crear|save-editar|toggle
 *   POST /modulo/productos/foto (multipart)
 *
 * FIX bug d:
 *   - @RequestMapping pasa de "/dashboard/productos" a "/modulo/productos"
 *     (coherente con el sidebar y los botones del bloque C).
 *   - Cada GET marca el forward interno que exige HtmxGuardInterceptor para /modulo/.
 */
@Controller
@RequestMapping("/modulo/productos")
public class ProductoController {

    private final ProductoService productoService;
    private final CategoriaRepository categoriaRepository;

    public ProductoController(ProductoService productoService,
                              CategoriaRepository categoriaRepository) {
        this.productoService = productoService;
        this.categoriaRepository = categoriaRepository;
    }

    private void cargarTabla(Model model, String q, String estado, String marca,
                             String temporada, int page, int size, String sort, String dir) {
        Page<ProductoListItemDTO> pagina =
                productoService.listar(q, estado, marca, temporada, page, size, sort, dir);
        model.addAttribute("pagina", pagina);
        model.addAttribute("productos", pagina.getContent());
        model.addAttribute("filtro", q == null ? "" : q);
        model.addAttribute("fEstado", estado == null ? "" : estado);
        model.addAttribute("fMarca", marca == null ? "" : marca);
        model.addAttribute("fTemporada", temporada == null ? "" : temporada);
        model.addAttribute("size", size);
        model.addAttribute("sort", sort);
        model.addAttribute("dir", dir);
        model.addAttribute("pageActual", pagina.getNumber());
        model.addAttribute("totalPaginas", pagina.getTotalPages());
        model.addAttribute("totalRegistros", pagina.getTotalElements());
    }

    @GetMapping
    @PreAuthorize("hasAuthority('PRODUCTOS_VER') or hasRole('ADMIN')")
    public String get(@RequestParam(defaultValue = "list") String action,
                      @RequestParam(required = false) Long id,
                      @RequestParam(required = false) String q,
                      @RequestParam(required = false) String estado,
                      @RequestParam(required = false) String marca,
                      @RequestParam(required = false) String temporada,
                      @RequestParam(defaultValue = "0") int page,
                      @RequestParam(defaultValue = "10") int size,
                      @RequestParam(defaultValue = "idProducto") String sort,
                      @RequestParam(defaultValue = "asc") String dir,
                      HttpServletRequest request,
                      Model model) {

        // OBLIGATORIO para pasar HtmxGuardInterceptor en URIs /modulo/*
        request.setAttribute(Constants.ATTR_FORWARD_INTERNO, Boolean.TRUE);

        switch (action) {
            case "table":
                cargarTabla(model, q, estado, marca, temporada, page, size, sort, dir);
                return "modules/productos/table :: tabla";

            case "form":
                if (id != null) {
                    model.addAttribute("producto", productoService.obtenerParaEditar(id));
                } else {
                    model.addAttribute("producto", new ProductoFormDTO());
                }
                model.addAttribute("categorias", categoriaRepository.findAll(Sort.by("nombre")));
                return "modules/productos/form :: modal";

            case "detalle":
                if (id == null) throw new BusinessException("Debe indicar el producto");
                model.addAttribute("p", productoService.obtenerDetalle(id));
                model.addAttribute("variantes", productoService.variantesDe(id));
                model.addAttribute("fotos", productoService.fotosDe(id));
                return "modules/productos/detalle :: modal";

            case "list":
            default:
                model.addAttribute("resumen", productoService.resumen());
                cargarTabla(model, "", "", "", "", 0, 10, "idProducto", "asc");
                return "modules/productos/list :: modulo";
        }
    }

    @PostMapping
    @PreAuthorize("hasAuthority('PRODUCTOS_VER') or hasRole('ADMIN')")
    public String post(@RequestParam String action,
                       @Valid @ModelAttribute("producto") ProductoFormDTO producto,
                       BindingResult binding,
                       @RequestParam(required = false) Long idProducto,
                       HttpServletRequest request,
                       Model model) {

        request.setAttribute(Constants.ATTR_FORWARD_INTERNO, Boolean.TRUE);

        switch (action) {
            case "save-crear":
            case "save-editar": {
                if (binding.hasErrors()) {
                    model.addAttribute("producto", producto);
                    model.addAttribute("categorias", categoriaRepository.findAll(Sort.by("nombre")));
                    return "modules/productos/form :: modal";
                }
                String msg;
                if ("save-crear".equals(action)) {
                    productoService.crear(producto);
                    msg = "Producto creado correctamente";
                } else {
                    productoService.editar(producto);
                    msg = "Producto actualizado correctamente";
                }
                cargarTabla(model, "", "", "", "", 0, 10, "idProducto", "asc");
                HtmxUtils.trigger(request, """
                        {"zpToast":{"tipo":"success","msg":"%s"},"zpClose":true}""".formatted(msg));
                return "modules/productos/table :: tabla";
            }

            case "toggle": {
                if (idProducto == null) throw new BusinessException("Debe indicar el producto");
                ProductoFormDTO actual = productoService.obtenerParaEditar(idProducto);
                String nuevo = "ACTIVO".equals(actual.getEstado()) ? "SUSPENDIDO" : "ACTIVO";
                productoService.cambiarEstado(idProducto, nuevo);
                cargarTabla(model, "", "", "", "", 0, 10, "idProducto", "asc");
                HtmxUtils.trigger(request, """
                        {"zpToast":{"tipo":"success","msg":"Estado actualizado"},"zpClose":true}""");
                return "modules/productos/table :: tabla";
            }

            default:
                throw new BusinessException("Acción no soportada: " + action);
        }
    }

    // Subida de imagen separada (multipart). Nota: NO setea forward interno porque
    // es @ResponseBody con fetch() y va con header X-XSRF-TOKEN; el guard solo
    // redirige peticiones de navegación, no estas llamadas AJAX que retornan texto.
    @PostMapping(value = "/foto", consumes = "multipart/form-data")
    @PreAuthorize("hasAuthority('PRODUCTOS_EDITAR') or hasRole('ADMIN')")
    @ResponseBody
    public String subirFoto(@RequestParam Long idProducto,
                            @RequestParam("file") MultipartFile file,
                            @RequestParam(defaultValue = "false") boolean principal) {
        productoService.agregarFoto(idProducto, file, principal);
        return "OK";
    }
}