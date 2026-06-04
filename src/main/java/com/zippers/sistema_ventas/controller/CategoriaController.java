package com.zippers.sistema_ventas.controller;

import com.zippers.sistema_ventas.entity.AuditoriaLog;
import com.zippers.sistema_ventas.entity.Categoria;
import com.zippers.sistema_ventas.entity.Usuario;
import com.zippers.sistema_ventas.repository.AuditoriaRepository;
import com.zippers.sistema_ventas.repository.CategoriaRepository;
import com.zippers.sistema_ventas.repository.UsuarioRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@Controller
@RequestMapping("/dashboard/categorias")
public class CategoriaController {

    @Autowired private CategoriaRepository categoriaRepository;
    @Autowired private AuditoriaRepository auditoriaRepository;
    @Autowired private UsuarioRepository usuarioRepository;

    // =========================================================================
    // ENRUTADOR GET: Carga inicial, paginación, búsqueda y formularios
    // =========================================================================
    @GetMapping
    public String despachadorGet(
            @RequestParam(name = "action", required = false) String action,
            @RequestParam(name = "id", required = false) Integer id,
            @RequestParam(name = "buscar", required = false, defaultValue = "") String buscar,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size, // NUEVO: Controla la cantidad de registros
            @RequestParam(name = "sort", defaultValue = "nombre") String sort,
            @RequestParam(name = "dir", defaultValue = "asc") String dir,
            Model model, HttpServletRequest request) {

        // BLOQUEO DE SEGURIDAD PARA RUTAS INTERNAS
        if (!"true".equals(request.getHeader("HX-Request"))) {
            return "redirect:/dashboard?modulo=categorias";
        }

        // 1. SI LA ACCIÓN ES PEDIR EL FORMULARIO (NUEVO/EDITAR)
        if ("form".equals(action)) {
            Categoria categoria = (id != null) ? categoriaRepository.findById(id).orElse(new Categoria()) : new Categoria();
            model.addAttribute("categoria", categoria);
            return "categorias-form :: form-categoria";
        }

        // 2. LÓGICA DE LISTADO (Para la carga inicial o la paginación interna)
        Sort sorting = dir.equals("asc") ? Sort.by(sort).ascending() : Sort.by(sort).descending();
        Pageable pageable = PageRequest.of(page, size, sorting); // APLICADO: Inyectamos el "size" dinámico
        
        Page<Categoria> pagina = buscar.isEmpty() 
            ? categoriaRepository.findAll(pageable) 
            : categoriaRepository.buscarPorTerminoPaginado(buscar, pageable);

        model.addAttribute("categorias", pagina.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", pagina.getTotalPages());
        model.addAttribute("buscar", buscar);
        model.addAttribute("size", size); // Guardamos la cantidad seleccionada para la vista
        model.addAttribute("sort", sort);
        model.addAttribute("dir", dir);

        // Si HTMX pide actualizar la tabla (buscador, paginación, sort, cambio de tamaño)
        if ("list".equals(action)) {
            return "categorias-table :: tabla-categorias"; 
        }

        // CARGA INICIAL: Si no hay 'action', devolvemos la vista completa 
        return "categorias"; 
    }

    // =========================================================================
    // ENRUTADOR POST: Guardar y Eliminar
    // =========================================================================
    @PostMapping
    public String despachadorPost(
            @RequestParam(name = "action") String action,
            @RequestParam(name = "id", required = false) Integer id,
            @Valid @ModelAttribute("categoria") Categoria categoria,
            BindingResult result, Principal principal) {

        if ("save".equals(action)) {
            return guardarCategoria(categoria, result, principal);
        } else if ("delete".equals(action)) {
            return eliminarCategoria(id, principal);
        }
        
        throw new IllegalArgumentException("Acción POST no soportada");
    }

    // --- MÉTODOS PRIVADOS ---

    @PreAuthorize("hasRole('ADMIN')")
    private String guardarCategoria(Categoria categoria, BindingResult result, Principal principal) {
        boolean nombreExiste = (categoria.getId_categoria() == null) 
            ? categoriaRepository.existsByNombreIgnoreCase(categoria.getNombre())
            : categoriaRepository.existeNombreExcluyendoId(categoria.getNombre(), categoria.getId_categoria());

        if (nombreExiste) {
            result.rejectValue("nombre", "Duplicate", "Ya existe una categoría con este nombre");
        }

        if (result.hasErrors()) {
            return "categorias-form :: form-categoria";
        }

        boolean isNew = (categoria.getId_categoria() == null);
        Categoria guardado = categoriaRepository.save(categoria);
        registrarAuditoria(principal, isNew ? "INSERT" : "UPDATE", "categorias", guardado.getId_categoria(), "Guardó categoría: " + guardado.getNombre());

        return "categorias-form :: exito";
    }

    @PreAuthorize("hasRole('ADMIN')")
    private String eliminarCategoria(Integer id, Principal principal) {
        if (id != null) {
            Categoria categoria = categoriaRepository.findById(id).orElse(null);
            if (categoria != null) {
                categoria.setEstado("INACTIVO");
                categoriaRepository.save(categoria);
                registrarAuditoria(principal, "UPDATE", "categorias", id, "Desactivó categoría (Soft Delete)");
            }
        }
        return "categorias-form :: exito";
    }

    private void registrarAuditoria(Principal principal, String accion, String tabla, Integer idRegistro, String descripcion) {
        if (principal != null) {
            Usuario ejecutor = usuarioRepository.findByEmail(principal.getName()).orElse(null);
            if (ejecutor != null) {
                AuditoriaLog log = new AuditoriaLog();
                log.setUsuario(ejecutor);
                log.setAccion(accion);
                log.setTabla_afectada(tabla);
                log.setRegistro_id(idRegistro);
                log.setDescripcion(descripcion);
                auditoriaRepository.save(log);
            }
        }
    }
}