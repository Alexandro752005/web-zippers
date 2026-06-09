package pe.com.zippers.service.impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.com.zippers.common.Constants;
import pe.com.zippers.common.exception.BusinessException;
import pe.com.zippers.domain.Categoria;
import pe.com.zippers.repository.CategoriaRepository;
import pe.com.zippers.security.SecurityUtils;
import pe.com.zippers.service.AuditoriaService;
import pe.com.zippers.service.CategoriaService;
import pe.com.zippers.web.dto.CategoriaFormDTO;
import pe.com.zippers.web.dto.CategoriaListItemDTO;

import java.util.List;
import java.util.Set;

@Service
public class CategoriaServiceImpl implements CategoriaService {

    private static final Set<String> COLUMNAS_ORDEN =
            Set.of("idCategoria", "nombre", "descripcion", "estado");

    private final CategoriaRepository categoriaRepository;
    private final AuditoriaService auditoriaService;

    public CategoriaServiceImpl(CategoriaRepository categoriaRepository,
                                AuditoriaService auditoriaService) {
        this.categoriaRepository = categoriaRepository;
        this.auditoriaService = auditoriaService;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CategoriaListItemDTO> listar(String filtro, int page, int size,
                                             String sort, String dir) {
        int p = Math.max(page, 0);
        int s = (size == 10 || size == 25 || size == 50 || size == 100) ? size : 10;
        String campo = COLUMNAS_ORDEN.contains(sort) ? sort : "idCategoria";
        Sort.Direction direccion = "desc".equalsIgnoreCase(dir)
                ? Sort.Direction.DESC : Sort.Direction.ASC;

        Pageable pageable = PageRequest.of(p, s, Sort.by(direccion, campo));
        Page<Categoria> pagina = categoriaRepository.buscar(filtro, pageable);

        return pagina.map(c -> new CategoriaListItemDTO(
                c.getIdCategoria(), c.getNombre(), c.getDescripcion(), c.getEstado()));
    }

    @Override
    @Transactional(readOnly = true)
    public CategoriaFormDTO obtenerParaEditar(Long idCategoria) {
        Categoria c = obtener(idCategoria);
        CategoriaFormDTO dto = new CategoriaFormDTO();
        dto.setIdCategoria(c.getIdCategoria());
        dto.setNombre(c.getNombre());
        dto.setDescripcion(c.getDescripcion());
        dto.setEstado(c.getEstado());
        return dto;
    }

    private Categoria obtener(Long id) {
        return categoriaRepository.findById(id)
                .orElseThrow(() -> new BusinessException("La categoría indicada no existe"));
    }

    @Override
    @Transactional
    public Long crear(CategoriaFormDTO dto) {
        validar(dto, null);
        Categoria c = new Categoria();
        c.setNombre(dto.getNombre());
        c.setDescripcion(dto.getDescripcion());
        c.setEstado(Constants.ESTADO_ACTIVO);  // nace ACTIVO
        c.setOrden(0);
        c.setIdCategoriaPadre(null);            // categorías de primer nivel
        c.setCreatedBy(SecurityUtils.getUsuarioIdOrNull());
        c.setUpdatedBy(SecurityUtils.getUsuarioIdOrNull());
        Categoria guardada = categoriaRepository.save(c);

        auditoriaService.registrarOk(
                "CATEGORIA_CREAR", "categorias", guardada.getIdCategoria(),
                "Alta de categoría '" + guardada.getNombre() + "'");
        return guardada.getIdCategoria();
    }

    @Override
    @Transactional
    public void editar(CategoriaFormDTO dto) {
        Categoria c = obtener(dto.getIdCategoria());
        validar(dto, c.getIdCategoria());

        String estado = dto.getEstado();
        if (estado == null
                || !(Constants.ESTADO_ACTIVO.equals(estado) || Constants.ESTADO_INACTIVO.equals(estado))) {
            throw new BusinessException("Estado inválido");
        }

        // Si pasa a INACTIVO, validar dependencias
        if (Constants.ESTADO_INACTIVO.equals(estado)
                && Constants.ESTADO_ACTIVO.equals(c.getEstado())) {
            validarSinProductosActivos(c.getIdCategoria(), "editar");
        }

        c.setNombre(dto.getNombre());
        c.setDescripcion(dto.getDescripcion());
        c.setEstado(estado);
        c.setUpdatedBy(SecurityUtils.getUsuarioIdOrNull());
        categoriaRepository.save(c);

        auditoriaService.registrarOk(
                "CATEGORIA_EDITAR", "categorias", c.getIdCategoria(),
                "Edición de categoría '" + c.getNombre() + "' estado=" + estado);
    }

    @Override
    @Transactional
    public void cambiarEstado(Long idCategoria, String nuevoEstado) {
        Categoria c = obtener(idCategoria);
        if (!(Constants.ESTADO_ACTIVO.equals(nuevoEstado)
                || Constants.ESTADO_INACTIVO.equals(nuevoEstado))) {
            throw new BusinessException("Estado inválido");
        }
        if (Constants.ESTADO_INACTIVO.equals(nuevoEstado)) {
            validarSinProductosActivos(idCategoria, "toggle");
        }
        c.setEstado(nuevoEstado);
        c.setUpdatedBy(SecurityUtils.getUsuarioIdOrNull());
        categoriaRepository.save(c);

        auditoriaService.registrarOk(
                "CATEGORIA_TOGGLE", "categorias", c.getIdCategoria(),
                "Cambio de estado de categoría '" + c.getNombre() + "' a " + nuevoEstado);
    }

    private void validarSinProductosActivos(Long idCategoria, String accion) {
        long activos = categoriaRepository.contarProductosActivos(idCategoria);
        if (activos > 0) {
            auditoriaService.registrarDenegado(
                    "CATEGORIA_" + ("toggle".equals(accion) ? "TOGGLE" : "EDITAR"),
                    "categorias", idCategoria,
                    "Intento de desactivar categoría con " + activos + " producto(s) activo(s)");
            throw new BusinessException(
                    "No se puede desactivar: la categoría tiene " + activos
                            + " producto(s) activo(s) asociado(s)");
        }
    }

    private void validar(CategoriaFormDTO dto, Long idActual) {
        if (dto.getNombre() == null || dto.getNombre().isBlank()) {
            throw new BusinessException("El nombre de la categoría es obligatorio");
        }
        boolean dup = (idActual == null)
                ? categoriaRepository.existsByNombreIgnoreCase(dto.getNombre())
                : categoriaRepository.existsByNombreIgnoreCaseAndIdCategoriaNot(dto.getNombre(), idActual);
        if (dup) {
            throw new BusinessException("Ya existe una categoría con ese nombre");
        }
    }
}