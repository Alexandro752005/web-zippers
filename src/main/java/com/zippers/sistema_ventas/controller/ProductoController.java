package com.zippers.sistema_ventas.controller;

import com.zippers.sistema_ventas.dto.ProductoDto;
import com.zippers.sistema_ventas.dto.VarianteDto;
import com.zippers.sistema_ventas.entity.*;
import com.zippers.sistema_ventas.repository.*;
import com.zippers.sistema_ventas.service.ProductoService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Controller
@RequestMapping("/dashboard/productos")
public class ProductoController {

    @Autowired private ProductoRepository productoRepository;
    @Autowired private ProductoFotoRepository fotoRepository;
    @Autowired private CategoriaRepository categoriaRepository;
    @Autowired private ProductoService productoService;
    @Autowired private AuditoriaRepository auditoriaRepository;
    @Autowired private UsuarioRepository usuarioRepository;

    @GetMapping
    public String despachadorGet(
            @RequestParam(name = "action", required = false) String action,
            @RequestParam(name = "id", required = false) Integer id,
            @RequestParam(name = "buscar", required = false, defaultValue = "") String buscar,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "sort", defaultValue = "nombre") String sort,
            @RequestParam(name = "dir", defaultValue = "asc") String dir,
            Model model, HttpServletRequest request) {

        boolean isHtmx = "true".equalsIgnoreCase(request.getHeader("HX-Request"));
        boolean isForwarded = request.getAttribute("jakarta.servlet.forward.request_uri") != null;

        if (!isHtmx && !isForwarded) {
            return "redirect:/dashboard?modulo=productos";
        }

        if ("form".equals(action)) {
            ProductoDto productoDto = new ProductoDto();
            productoDto.getVariantes().add(new VarianteDto());
            model.addAttribute("productoDto", productoDto);
            model.addAttribute("categorias", categoriaRepository.findAll());
            model.addAttribute("fotosCount", 0);
            return "productos-form :: form-producto";
        }

        if ("edit".equals(action) && id != null) {
            Producto producto = productoRepository.findByIdWithVariantesAndFotos(id).orElseThrow();
            model.addAttribute("productoDto", mapToDto(producto));
            model.addAttribute("categorias", categoriaRepository.findAll());
            model.addAttribute("fotosCount", productoService.contarFotos(id));
            return "productos-form :: form-producto";
        }

        if ("fotos".equals(action) && id != null) {
            List<ProductoFoto> fotos = fotoRepository.findByProductoIdProducto(id);
            model.addAttribute("fotos", fotos);
            model.addAttribute("productoId", id);
            model.addAttribute("fotosCount", fotos.size());
            return "productos-form :: producto-fotos-wrap";
        }

        cargarListado(model, buscar, page, size, sort, dir);

        if ("list".equals(action) && isHtmx) {
            return "productos-table :: tabla-productos";
        }

        return "productos";
    }

    @PostMapping
    public String despachadorPost(
            @RequestParam(name = "action") String action,
            @RequestParam(name = "id", required = false) Integer id,
            @Valid @ModelAttribute("productoDto") ProductoDto productoDto,
            BindingResult result,
            @RequestParam(name = "fotos", required = false) MultipartFile[] fotos,
            Principal principal,
            Model model,
            HttpServletRequest request) {

        if (!"true".equalsIgnoreCase(request.getHeader("HX-Request"))) {
            return "redirect:/dashboard?modulo=productos";
        }

        if ("save".equals(action)) {
            return guardarProducto(productoDto, result, fotos, principal, model);
        } else if ("toggle-estado".equals(action) && id != null) {
            toggleEstado(id, principal);
            return "productos-table :: tabla-productos";
        }
        throw new IllegalArgumentException("Acción POST no soportada");
    }

    private void cargarListado(Model model, String buscar, int page, int size, String sort, String dir) {
        Sort sorting = "desc".equalsIgnoreCase(dir) ? Sort.by(sort).descending() : Sort.by(sort).ascending();
        Pageable pageable = PageRequest.of(page, size, sorting);

        Page<Producto> pagina = (buscar == null || buscar.isBlank())
                ? productoRepository.findAll(pageable)
                : productoRepository.buscarPorTermino(buscar, pageable);
                
        model.addAttribute("productos", pagina.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", pagina.getTotalPages());
        model.addAttribute("totalElements", pagina.getTotalElements());
        model.addAttribute("size", size);
        model.addAttribute("buscar", buscar);
        model.addAttribute("sort", sort);
        model.addAttribute("dir", dir);
    }

    private String guardarProducto(ProductoDto productoDto, BindingResult result, MultipartFile[] fotos, Principal principal, Model model) {
        if (productoDto.getIdProducto() == null && productoRepository.existsByCodigo(productoDto.getCodigo())) {
            result.rejectValue("codigo", "Duplicate", "El código ya existe");
        } else if (productoDto.getIdProducto() != null &&
                !productoRepository.esCodigoUnicoExceptoId(productoDto.getCodigo(), productoDto.getIdProducto())) {
            result.rejectValue("codigo", "Duplicate", "El código ya está en uso por otro producto");
        }

        if (productoDto.getCategoriaId() == null) {
            result.rejectValue("categoriaId", "NotNull", "La categoría es obligatoria");
        }

        if (productoDto.getVariantes() == null || productoDto.getVariantes().isEmpty()) {
            result.rejectValue("variantes", "NotEmpty", "Debe agregar al menos una variante");
        }

        if (result.hasErrors()) {
            model.addAttribute("categorias", categoriaRepository.findAll());
            model.addAttribute("fotosCount", (productoDto.getIdProducto() == null) ? 0 : productoService.contarFotos(productoDto.getIdProducto()));
            return "productos-form :: form-producto";
        }

        Producto producto = mapToEntity(productoDto);
        boolean isNew = (producto.getId_producto() == null);
        Producto guardado = productoService.guardarProducto(Objects.requireNonNull(producto));
        
        if (fotos != null) {
            for (MultipartFile foto : fotos) {
                try {
                    if (foto != null && !foto.isEmpty()) {
                        productoService.subirFoto(guardado.getId_producto(), foto);
                    }
                } catch (IOException | IllegalStateException ignored) {
                }
            }
        }

        registrarAuditoria(principal, isNew ? "INSERT" : "UPDATE", "productos", guardado.getId_producto(), "Gestión de producto: " + guardado.getNombre());
        return "productos-form :: exito";
    }

    private void toggleEstado(Integer id, Principal principal) {
        Producto producto = productoRepository.findById(Objects.requireNonNull(id)).orElse(null);
        if (producto != null) {
            producto.setEstado("ACTIVO".equals(producto.getEstado()) ? "INACTIVO" : "ACTIVO");
            productoRepository.save(producto);
            registrarAuditoria(principal, "UPDATE", "productos", id, "Cambio de estado a " + producto.getEstado());
        }
    }

    private ProductoDto mapToDto(Producto producto) {
        ProductoDto dto = new ProductoDto();
        dto.setIdProducto(producto.getId_producto());
        dto.setNombre(producto.getNombre());
        dto.setDescripcion(producto.getDescripcion());
        dto.setCodigo(producto.getCodigo());
        dto.setCategoriaId(producto.getCategoria() != null ? producto.getCategoria().getId_categoria() : null);
        dto.setGenero(producto.getGenero() != null ? producto.getGenero() : "UNISEX");
        dto.setMarca(producto.getMarca());
        dto.setTemporada(producto.getTemporada() != null ? producto.getTemporada() : "Atemporal");
        dto.setMaterialPrincipal(producto.getMaterial_principal());
        dto.setPrecioCosto(producto.getPrecio_costo());
        dto.setEstado(producto.getEstado() != null ? producto.getEstado() : "ACTIVO");

        if (producto.getVariantes() != null) {
            for (ProductoVariante variante : producto.getVariantes()) {
                VarianteDto varianteDto = new VarianteDto();
                varianteDto.setIdVariante(variante.getId_variante());
                varianteDto.setTalla(variante.getTalla());
                varianteDto.setMedidaCm(variante.getMedidaCm());
                varianteDto.setColor(variante.getColor());
                varianteDto.setCodigoHex(variante.getCodigoHex());
                varianteDto.setPrecioVenta(variante.getPrecioVenta());
                varianteDto.setStock(variante.getStockActual());
                dto.getVariantes().add(varianteDto);
            }
        }

        if (dto.getVariantes().isEmpty()) {
            dto.getVariantes().add(new VarianteDto());
        }
        return dto;
    }

    private Producto mapToEntity(ProductoDto dto) {
        Producto producto = dto.getIdProducto() != null
                ? productoRepository.findByIdWithVariantesAndFotos(dto.getIdProducto()).orElse(new Producto())
                : new Producto();

        producto.setNombre(dto.getNombre());
        producto.setDescripcion(dto.getDescripcion());
        producto.setCodigo(dto.getCodigo());
        
        Integer catId = dto.getCategoriaId();
        if (catId != null) {
            producto.setCategoria(categoriaRepository.findById(catId).orElse(null));
        }

        producto.setGenero(dto.getGenero());
        producto.setMarca(dto.getMarca());
        producto.setTemporada(dto.getTemporada());
        producto.setMaterial_principal(dto.getMaterialPrincipal());
        producto.setPrecio_costo(dto.getPrecioCosto());
        producto.setEstado(dto.getEstado() != null ? dto.getEstado() : "ACTIVO");
        
        if (producto.getVariantes() == null) {
            producto.setVariantes(new ArrayList<>());
        } else {
            producto.getVariantes().clear();
        }

        int stockTotal = 0;
        double precioVentaMinimo = -1.0; 

        for (VarianteDto varianteDto : dto.getVariantes()) {
            ProductoVariante variante = new ProductoVariante();
            variante.setId_variante(varianteDto.getIdVariante());
            variante.setProducto(producto);
            variante.setTalla(varianteDto.getTalla());
            variante.setMedidaCm(varianteDto.getMedidaCm());
            variante.setColor(varianteDto.getColor());
            variante.setCodigoHex(varianteDto.getCodigoHex());
            variante.setPrecioVenta(varianteDto.getPrecioVenta());
            
            // --- SOLUCIÓN: HEREDAR EL PRECIO DE COSTO DEL PADRE ---
            variante.setPrecioCosto(dto.getPrecioCosto() != null ? dto.getPrecioCosto() : java.math.BigDecimal.ZERO);
            // ------------------------------------------------------

            Integer stockVariante = varianteDto.getStock() == null ? 0 : varianteDto.getStock();
            variante.setStockActual(stockVariante);
            stockTotal += stockVariante;

            if (varianteDto.getPrecioVenta() != null) {
                double precioActual = varianteDto.getPrecioVenta().doubleValue();
                if (precioVentaMinimo == -1.0 || precioActual < precioVentaMinimo) {
                    precioVentaMinimo = precioActual;
                }
            }

            producto.getVariantes().add(variante);
        }

        producto.setStock(stockTotal);
        producto.setStock_minimo(10); 
        producto.setPrecio_venta(precioVentaMinimo != -1.0 ? precioVentaMinimo : 0.0);

        return producto;
    }

    @PostMapping("/fotos/upload")
    public String subirFoto(@RequestParam("idProducto") Integer idProducto, @RequestParam("archivo") MultipartFile archivo, Model model, Principal principal) {
        try {
            productoService.subirFoto(Objects.requireNonNull(idProducto, "idProducto requerido"), archivo);
        } catch (IOException | IllegalStateException e) {
            model.addAttribute("error", e.getMessage());
        }
        List<ProductoFoto> fotos = fotoRepository.findByProductoIdProducto(idProducto);
        model.addAttribute("fotos", fotos);
        model.addAttribute("productoId", idProducto);
        model.addAttribute("fotosCount", fotos.size());
        return "productos-form :: producto-fotos-wrap";
    }

    @PostMapping("/fotos/portada")
    public String marcarPortada(@RequestParam("idFoto") Integer idFoto, Model model) {
        productoService.marcarPortada(Objects.requireNonNull(idFoto, "idFoto requerido"));
        ProductoFoto foto = fotoRepository.findById(idFoto).orElseThrow();
        Integer idProd = foto.getProducto().getId_producto();
        List<ProductoFoto> fotos = fotoRepository.findByProductoIdProducto(idProd);
        model.addAttribute("fotos", fotos);
        model.addAttribute("productoId", idProd);
        model.addAttribute("fotosCount", fotos.size());
        return "productos-form :: producto-fotos-wrap";
    }

    @PostMapping("/fotos/delete")
    public String eliminarFoto(@RequestParam("idFoto") Integer idFoto, Model model) {
        ProductoFoto foto = fotoRepository.findById(Objects.requireNonNull(idFoto, "idFoto requerido")).orElseThrow();
        Integer idProd = foto.getProducto().getId_producto();
        productoService.eliminarFoto(idFoto);
        List<ProductoFoto> fotos = fotoRepository.findByProductoIdProducto(idProd);
        model.addAttribute("fotos", fotos);
        model.addAttribute("productoId", idProd);
        model.addAttribute("fotosCount", fotos.size());
        return "productos-form :: producto-fotos-wrap";
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