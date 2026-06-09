package pe.com.zippers.service.impl;

import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import pe.com.zippers.common.Constants;
import pe.com.zippers.common.exception.BusinessException;
import pe.com.zippers.domain.*;
import pe.com.zippers.repository.*;
import pe.com.zippers.security.SecurityUtils;
import pe.com.zippers.service.AuditoriaService;
import pe.com.zippers.service.ProductoService;
import pe.com.zippers.service.StorageService;
import pe.com.zippers.web.dto.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

@Service
public class ProductoServiceImpl implements ProductoService {

    private static final Set<String> COLS_ORDEN =
            Set.of("idProducto", "nombre", "sku", "precioVenta", "estado", "marca");
    private static final Set<String> ESTADOS =
            Set.of("ACTIVO", "INACTIVO", "AGOTADO", "SUSPENDIDO");
    private static final int MAX_FOTOS = 5;

    private final ProductoRepository productoRepository;
    private final ProductoVarianteRepository varianteRepository;
    private final ProductoFotoRepository fotoRepository;
    private final ProductoCategoriaRepository productoCategoriaRepository;
    private final StorageService storageService;
    private final AuditoriaService auditoriaService;

    public ProductoServiceImpl(ProductoRepository productoRepository,
                               ProductoVarianteRepository varianteRepository,
                               ProductoFotoRepository fotoRepository,
                               ProductoCategoriaRepository productoCategoriaRepository,
                               StorageService storageService,
                               AuditoriaService auditoriaService) {
        this.productoRepository = productoRepository;
        this.varianteRepository = varianteRepository;
        this.fotoRepository = fotoRepository;
        this.productoCategoriaRepository = productoCategoriaRepository;
        this.storageService = storageService;
        this.auditoriaService = auditoriaService;
    }

    // -------------------- Resumen / listado --------------------
    @Override
    @Transactional(readOnly = true)
    public ProductoResumenDTO resumen() {
        return new ProductoResumenDTO(
                productoRepository.count(),
                productoRepository.countByEstado(Constants.ESTADO_ACTIVO),
                productoRepository.contarMarcas(),
                productoRepository.precioVentaPromedio());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductoListItemDTO> listar(String q, String estado, String marca,
                                            String temporada, int page, int size,
                                            String sort, String dir) {
        int p = Math.max(page, 0);
        int s = (size == 10 || size == 25 || size == 50 || size == 100) ? size : 10;
        String campo = COLS_ORDEN.contains(sort) ? sort : "idProducto";
        Sort.Direction d = "desc".equalsIgnoreCase(dir) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(p, s, Sort.by(d, campo));

        Page<Producto> pagina = productoRepository.buscar(q, estado, marca, temporada, pageable);
        return pagina.map(this::aListItem);
    }

    private ProductoListItemDTO aListItem(Producto p) {
        ProductoListItemDTO dto = new ProductoListItemDTO();
        dto.setIdProducto(p.getIdProducto());
        dto.setSku(p.getSku());
        dto.setNombre(p.getNombre());
        dto.setDescripcion(p.getDescripcion());
        dto.setMarca(p.getMarca());
        dto.setTemporada(p.getTemporada());
        dto.setPrecioVenta(p.getPrecioVenta());
        dto.setEstado(p.getEstado());
        dto.setVisibleWeb(Boolean.TRUE.equals(p.getVisibleWeb()));
        dto.setTotalVariantes(varianteRepository.contarActivasPorProducto(p.getIdProducto()));
        fotoRepository.findPrincipal(p.getIdProducto())
                .ifPresent(f -> dto.setFotoPrincipal(f.getUrlFoto()));
        return dto;
    }

    // -------------------- Lectura detalle --------------------
    @Override
    @Transactional(readOnly = true)
    public ProductoFormDTO obtenerParaEditar(Long idProducto) {
        Producto p = obtener(idProducto);
        ProductoFormDTO dto = new ProductoFormDTO();
        dto.setIdProducto(p.getIdProducto());
        dto.setSku(p.getSku());
        dto.setNombre(p.getNombre());
        dto.setMarca(p.getMarca());
        dto.setTemporada(p.getTemporada());
        dto.setDescripcion(p.getDescripcion());
        dto.setPrecioCompra(p.getPrecioCompra());
        dto.setPrecioVenta(p.getPrecioVenta());
        dto.setStockMinimo(p.getStockMinimo());
        dto.setEstado(p.getEstado());
        dto.setVisibleWeb(Boolean.TRUE.equals(p.getVisibleWeb()));
        dto.setCategoriaIds(productoCategoriaRepository.categoriasDeProducto(idProducto));
        for (ProductoVariante v : varianteRepository.findByProducto(idProducto)) {
            VarianteDTO vd = new VarianteDTO();
            vd.setIdVariante(v.getIdVariante());
            vd.setSkuVariante(v.getSkuVariante());
            vd.setColor(v.getColor());
            vd.setTalla(v.getTalla());
            vd.setPrecioCompra(v.getPrecioCompra());
            vd.setPrecioVenta(v.getPrecioVenta());
            vd.setStockActual(v.getStockActual());
            vd.setEstado(v.getEstado());
            dto.getVariantes().add(vd);
        }
        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public Producto obtenerDetalle(Long idProducto) { return obtener(idProducto); }

    @Override
    @Transactional(readOnly = true)
    public List<ProductoVariante> variantesDe(Long idProducto) {
        return varianteRepository.findByProducto(idProducto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductoFoto> fotosDe(Long idProducto) {
        return fotoRepository.findActivasByProducto(idProducto);
    }

    private Producto obtener(Long id) {
        return productoRepository.findById(id)
                .orElseThrow(() -> new BusinessException("El producto indicado no existe"));
    }

    // -------------------- Crear --------------------
    @Override
    @Transactional
    public Long crear(ProductoFormDTO dto) {
        validarProducto(dto, null);

        Producto p = new Producto();
        aplicar(dto, p);
        p.setOfertaPorcentaje(BigDecimal.ZERO);
        p.setCreatedBy(SecurityUtils.getUsuarioIdOrNull());
        p.setUpdatedBy(SecurityUtils.getUsuarioIdOrNull());
        Producto guardado = productoRepository.save(p);

        guardarCategorias(guardado.getIdProducto(), dto.getCategoriaIds());
        guardarVariantes(guardado.getIdProducto(), dto.getVariantes(), true);

        auditoriaService.registrarOk(
                "PRODUCTO_CREAR", "productos", guardado.getIdProducto(),
                "Alta de producto SKU=" + guardado.getSku() + " (" + guardado.getNombre() + ")");
        return guardado.getIdProducto();
    }

    // -------------------- Editar --------------------
    @Override
    @Transactional
    public void editar(ProductoFormDTO dto) {
        Producto p = obtener(dto.getIdProducto());
        validarProducto(dto, p.getIdProducto());

        if (dto.getEstado() == null || !ESTADOS.contains(dto.getEstado())) {
            throw new BusinessException("Estado inválido");
        }

        aplicar(dto, p);
        p.setUpdatedBy(SecurityUtils.getUsuarioIdOrNull());
        productoRepository.save(p);

        guardarCategorias(p.getIdProducto(), dto.getCategoriaIds());
        guardarVariantes(p.getIdProducto(), dto.getVariantes(), false);

        auditoriaService.registrarOk(
                "PRODUCTO_EDITAR", "productos", p.getIdProducto(),
                "Edición de producto SKU=" + p.getSku() + " estado=" + p.getEstado());
    }

    private void aplicar(ProductoFormDTO dto, Producto p) {
        p.setSku(dto.getSku());
        p.setNombre(dto.getNombre());
        p.setMarca(dto.getMarca());
        p.setTemporada(dto.getTemporada());
        p.setDescripcion(dto.getDescripcion());
        p.setPrecioCompra(dto.getPrecioCompra());
        p.setPrecioVenta(dto.getPrecioVenta());
        p.setStockMinimo(dto.getStockMinimo() != null ? dto.getStockMinimo() : 10);
        p.setEstado(dto.getEstado() != null ? dto.getEstado() : Constants.ESTADO_ACTIVO);
        p.setVisibleWeb(Boolean.TRUE.equals(dto.getVisibleWeb()));
    }

    // -------------------- Estado (soft delete) --------------------
    @Override
    @Transactional
    public void cambiarEstado(Long idProducto, String nuevoEstado) {
        Producto p = obtener(idProducto);
        if (!ESTADOS.contains(nuevoEstado)) {
            throw new BusinessException("Estado inválido");
        }
        p.setEstado(nuevoEstado);
        p.setUpdatedBy(SecurityUtils.getUsuarioIdOrNull());
        productoRepository.save(p);
        auditoriaService.registrarOk(
                "PRODUCTO_TOGGLE", "productos", p.getIdProducto(),
                "Cambio de estado de producto SKU=" + p.getSku() + " a " + nuevoEstado);
    }

    // -------------------- Fotos --------------------
    @Override
    @Transactional
    public void agregarFoto(Long idProducto, MultipartFile file, boolean principal) {
        Producto p = obtener(idProducto);
        long actuales = fotoRepository.countByIdProductoAndEstado(idProducto, Constants.ESTADO_ACTIVO);
        if (actuales >= MAX_FOTOS) {
            throw new BusinessException("Máximo " + MAX_FOTOS + " imágenes por producto");
        }
        String url = storageService.guardarImagenProducto(file, idProducto);

        ProductoFoto foto = new ProductoFoto();
        foto.setIdProducto(idProducto);
        foto.setUrlFoto(url);
        foto.setAltText(p.getNombre());
        foto.setOrden((int) actuales);
        foto.setEsPrincipal(principal || actuales == 0); // primera foto es principal
        foto.setEstado(Constants.ESTADO_ACTIVO);
        fotoRepository.save(foto);

        auditoriaService.registrarOk(
                "PRODUCTO_FOTO", "productos_fotos", idProducto,
                "Imagen agregada a producto SKU=" + p.getSku());
    }

    // -------------------- Helpers transaccionales --------------------
    private void guardarCategorias(Long idProducto, List<Long> categoriaIds) {
        productoCategoriaRepository.borrarPorProducto(idProducto);
        if (categoriaIds != null) {
            for (Long idCat : categoriaIds.stream().distinct().toList()) {
                if (idCat != null) {
                    productoCategoriaRepository.vincular(idProducto, idCat);
                }
            }
        }
    }

    private void guardarVariantes(Long idProducto, List<VarianteDTO> variantes, boolean esNuevo) {
        if (variantes == null) return;
        for (VarianteDTO vd : variantes) {
            validarVariante(vd);
            ProductoVariante v;
            if (vd.getIdVariante() != null) {
                v = varianteRepository.findById(vd.getIdVariante())
                        .orElseThrow(() -> new BusinessException("Variante inexistente"));
                if (!v.getIdProducto().equals(idProducto)) {
                    throw new BusinessException("La variante no pertenece al producto");
                }
                if (varianteRepository.existsBySkuVarianteIgnoreCaseAndIdVarianteNot(
                        vd.getSkuVariante(), vd.getIdVariante())) {
                    throw new BusinessException("SKU de variante duplicado: " + vd.getSkuVariante());
                }
            } else {
                if (varianteRepository.existsBySkuVarianteIgnoreCase(vd.getSkuVariante())) {
                    throw new BusinessException("SKU de variante duplicado: " + vd.getSkuVariante());
                }
                v = new ProductoVariante();
                v.setIdProducto(idProducto);
                v.setStockReservado(0);
                v.setCreatedBy(SecurityUtils.getUsuarioIdOrNull());
            }
            v.setSkuVariante(vd.getSkuVariante());
            v.setColor(vd.getColor());
            v.setTalla(vd.getTalla());
            v.setPrecioCompra(vd.getPrecioCompra());
            v.setPrecioVenta(vd.getPrecioVenta());
            v.setStockActual(vd.getStockActual() != null ? vd.getStockActual() : 0);
            v.setEstado("INACTIVO".equals(vd.getEstado()) ? "INACTIVO" : "ACTIVO");
            v.setUpdatedBy(SecurityUtils.getUsuarioIdOrNull());
            varianteRepository.save(v);
        }
    }

    // -------------------- Validaciones --------------------
    private void validarProducto(ProductoFormDTO dto, Long idActual) {
        if (dto.getSku() == null || dto.getSku().isBlank()) {
            throw new BusinessException("El SKU base es obligatorio");
        }
        boolean dup = (idActual == null)
                ? productoRepository.existsBySkuIgnoreCase(dto.getSku())
                : productoRepository.existsBySkuIgnoreCaseAndIdProductoNot(dto.getSku(), idActual);
        if (dup) throw new BusinessException("Ya existe un producto con ese SKU");

        if (dto.getPrecioCompra() == null || dto.getPrecioCompra().signum() <= 0) {
            throw new BusinessException("El precio de compra debe ser mayor a 0");
        }
        if (dto.getPrecioVenta() == null || dto.getPrecioVenta().signum() <= 0) {
            throw new BusinessException("El precio de venta debe ser mayor a 0");
        }
        if (dto.getStockMinimo() == null || dto.getStockMinimo() < 10) {
            throw new BusinessException("El stock mínimo debe ser al menos 10");
        }
    }

    private void validarVariante(VarianteDTO vd) {
        if (vd.getSkuVariante() == null || vd.getSkuVariante().isBlank()) {
            throw new BusinessException("Cada variante requiere SKU");
        }
        if (vd.getPrecioCompra() == null || vd.getPrecioCompra().signum() <= 0) {
            throw new BusinessException("Precio de compra de variante debe ser mayor a 0");
        }
        if (vd.getPrecioVenta() == null || vd.getPrecioVenta().signum() <= 0) {
            throw new BusinessException("Precio de venta de variante debe ser mayor a 0");
        }
        if (vd.getStockActual() != null && vd.getStockActual() < 0) {
            throw new BusinessException("El stock no puede ser negativo");
        }
    }
}