package pe.com.zippers.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.com.zippers.common.Constants;
import pe.com.zippers.common.exception.BusinessException;
import pe.com.zippers.domain.Opcion;
import pe.com.zippers.domain.Permiso;
import pe.com.zippers.domain.Rol;
import pe.com.zippers.repository.OpcionRepository;
import pe.com.zippers.repository.PermisoRepository;
import pe.com.zippers.repository.RolRepository;
import pe.com.zippers.service.AuditoriaService;
import pe.com.zippers.service.PerfilService;
import pe.com.zippers.web.dto.MatrizPermisoDTO;
import pe.com.zippers.web.dto.PerfilFormDTO;
import pe.com.zippers.web.dto.PerfilListItemDTO;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class PerfilServiceImpl implements PerfilService {

    private static final String COD_ADMIN = "ADMIN";

    private final RolRepository rolRepository;
    private final OpcionRepository opcionRepository;
    private final PermisoRepository permisoRepository;
    private final AuditoriaService auditoriaService;

    public PerfilServiceImpl(RolRepository rolRepository,
                             OpcionRepository opcionRepository,
                             PermisoRepository permisoRepository,
                             AuditoriaService auditoriaService) {
        this.rolRepository = rolRepository;
        this.opcionRepository = opcionRepository;
        this.permisoRepository = permisoRepository;
        this.auditoriaService = auditoriaService;
    }

    // ---------------------------------------------------------------------
    // Helpers de negocio
    // ---------------------------------------------------------------------
    private boolean esBase(Rol r) {
        return r != null && (Long.valueOf(1L).equals(r.getIdRol())
                || COD_ADMIN.equalsIgnoreCase(r.getCodigo()));
    }

    private Rol obtenerRolGestionable(Long idRol) {
        Rol r = rolRepository.findById(idRol)
                .orElseThrow(() -> new BusinessException("El perfil indicado no existe"));
        if ("CLIENTE".equalsIgnoreCase(r.getCodigo())) {
            throw new BusinessException("El perfil CLIENTE no se gestiona desde este módulo");
        }
        return r;
    }

    // ---------------------------------------------------------------------
    // Listado
    // ---------------------------------------------------------------------
    @Override
    @Transactional(readOnly = true)
    public List<PerfilListItemDTO> listar(String filtro) {
        List<Rol> roles = rolRepository.buscarGestionables(filtro);
        List<PerfilListItemDTO> out = new ArrayList<>(roles.size());
        for (Rol r : roles) {
            out.add(new PerfilListItemDTO(
                    r.getIdRol(), r.getCodigo(), r.getNombre(),
                    r.getDescripcion(), r.getEstado(), esBase(r)));
        }
        return out;
    }

    @Override
    @Transactional(readOnly = true)
    public PerfilFormDTO obtenerParaEditar(Long idRol) {
        Rol r = obtenerRolGestionable(idRol);
        PerfilFormDTO dto = new PerfilFormDTO();
        dto.setIdRol(r.getIdRol());
        dto.setNombre(r.getNombre());
        dto.setDescripcion(r.getDescripcion());
        dto.setEstado(r.getEstado());
        return dto;
    }

    // ---------------------------------------------------------------------
    // Crear
    // ---------------------------------------------------------------------
    @Override
    @Transactional
    public Long crear(PerfilFormDTO dto) {
        validarNombre(dto, null);

        Rol r = new Rol();
        r.setNombre(dto.getNombre());
        r.setDescripcion(dto.getDescripcion());
        r.setEstado(Constants.ESTADO_ACTIVO);          // nace ACTIVO siempre
        r.setCodigo(generarCodigo(dto.getNombre()));    // código derivado y único

        Rol guardado = rolRepository.save(r);

        auditoriaService.registrarOk(
                "PERFIL_CREAR", "roles", guardado.getIdRol(),
                "Creación de perfil '" + guardado.getNombre()
                        + "' (codigo=" + guardado.getCodigo() + ")");

        return guardado.getIdRol();
    }

    private String generarCodigo(String nombre) {
        String base = nombre.toUpperCase()
                .replaceAll("[ÁÀÄÂ]", "A").replaceAll("[ÉÈËÊ]", "E")
                .replaceAll("[ÍÌÏÎ]", "I").replaceAll("[ÓÒÖÔ]", "O")
                .replaceAll("[ÚÙÜÛ]", "U").replaceAll("Ñ", "N")
                .replaceAll("[^A-Z0-9]+", "_")
                .replaceAll("^_+|_+$", "");
        if (base.isBlank()) {
            base = "PERFIL";
        }
        if (base.length() > 25) {
            base = base.substring(0, 25);
        }
        String candidato = base;
        int i = 1;
        while (rolRepository.findByCodigo(candidato).isPresent()) {
            candidato = base + "_" + i;
            i++;
        }
        return candidato;
    }

    // ---------------------------------------------------------------------
    // Editar
    // ---------------------------------------------------------------------
    @Override
    @Transactional
    public void editar(PerfilFormDTO dto) {
        Rol r = obtenerRolGestionable(dto.getIdRol());
        validarNombre(dto, r.getIdRol());

        String estadoSolicitado = dto.getEstado();
        if (estadoSolicitado == null
                || !(Constants.ESTADO_ACTIVO.equals(estadoSolicitado)
                     || Constants.ESTADO_INACTIVO.equals(estadoSolicitado))) {
            throw new BusinessException("Estado inválido para el perfil");
        }

        // Bloqueo backend del perfil base
        if (esBase(r) && Constants.ESTADO_INACTIVO.equals(estadoSolicitado)) {
            auditoriaService.registrarDenegado(
                    "PERFIL_EDITAR", "roles", r.getIdRol(),
                    "Intento de desactivar el Perfil Administrador Base");
            throw new BusinessException(
                    "El Perfil Administrador Base no puede ser desactivado");
        }

        r.setNombre(dto.getNombre());
        r.setDescripcion(dto.getDescripcion());
        r.setEstado(estadoSolicitado);
        rolRepository.save(r);

        auditoriaService.registrarOk(
                "PERFIL_EDITAR", "roles", r.getIdRol(),
                "Edición de perfil '" + r.getNombre() + "' estado=" + estadoSolicitado);
    }

    @Override
    @Transactional
    public void cambiarEstado(Long idRol, String nuevoEstado) {
        Rol r = obtenerRolGestionable(idRol);
        if (!(Constants.ESTADO_ACTIVO.equals(nuevoEstado)
                || Constants.ESTADO_INACTIVO.equals(nuevoEstado))) {
            throw new BusinessException("Estado inválido");
        }
        if (esBase(r) && Constants.ESTADO_INACTIVO.equals(nuevoEstado)) {
            auditoriaService.registrarDenegado(
                    "PERFIL_TOGGLE", "roles", r.getIdRol(),
                    "Intento de desactivar el Perfil Administrador Base");
            throw new BusinessException(
                    "El Perfil Administrador Base no puede ser desactivado");
        }
        r.setEstado(nuevoEstado);
        rolRepository.save(r);
        auditoriaService.registrarOk(
                "PERFIL_TOGGLE", "roles", r.getIdRol(),
                "Cambio de estado de perfil '" + r.getNombre() + "' a " + nuevoEstado);
    }

    private void validarNombre(PerfilFormDTO dto, Long idRolActual) {
        if (dto.getNombre() == null || dto.getNombre().isBlank()) {
            throw new BusinessException("El nombre del perfil es obligatorio");
        }
        boolean dup = (idRolActual == null)
                ? rolRepository.existsByNombreIgnoreCase(dto.getNombre())
                : rolRepository.existsByNombreIgnoreCaseAndIdRolNot(dto.getNombre(), idRolActual);
        if (dup) {
            throw new BusinessException("Ya existe un perfil con ese nombre");
        }
    }

    // ---------------------------------------------------------------------
    // Matriz de permisos
    // ---------------------------------------------------------------------
    @Override
    @Transactional(readOnly = true)
    public List<MatrizPermisoDTO> obtenerMatriz(Long idRol) {
        Rol r = obtenerRolGestionable(idRol);

        List<Opcion> opciones = opcionRepository.findVisiblesOrdenadas();

        // Mapa idOpcion -> permiso existente
        Map<Long, Permiso> existentes = new LinkedHashMap<>();
        for (Permiso p : permisoRepository.findByRol(r.getIdRol())) {
            existentes.put(p.getIdOpcion(), p);
        }

        List<MatrizPermisoDTO> filas = new ArrayList<>(opciones.size());
        for (Opcion o : opciones) {
            MatrizPermisoDTO fila = new MatrizPermisoDTO(
                    o.getIdOpcion(), o.getCodigo(), o.getNombre());
            Permiso p = existentes.get(o.getIdOpcion());
            if (p != null) {
                fila.setVer(Boolean.TRUE.equals(p.getPermisoVer()));
                fila.setCrear(Boolean.TRUE.equals(p.getPermisoCrear()));
                fila.setEditar(Boolean.TRUE.equals(p.getPermisoEditar()));
                fila.setEliminar(Boolean.TRUE.equals(p.getPermisoEliminar()));
                fila.setExportar(Boolean.TRUE.equals(p.getPermisoExportar()));
            }
            filas.add(fila);
        }
        return filas;
    }

    @Override
    @Transactional
    public void guardarMatriz(Long idRol, List<MatrizPermisoDTO> filas) {
        Rol r = obtenerRolGestionable(idRol);

        // El perfil base NO puede quedar sin permisos ni perder accesos críticos.
        // Permitimos editar su matriz pero garantizamos que nunca quede vacía.
        if (filas == null) {
            filas = new ArrayList<>();
        }

        // 1) Borrar permisos actuales del rol (transaccional)
        permisoRepository.deleteByRol(r.getIdRol());

        // 2) Reinsertar solo filas con al menos un flag activo
        int insertados = 0;
        for (MatrizPermisoDTO f : filas) {
            boolean alguno = f.isVer() || f.isCrear() || f.isEditar()
                    || f.isEliminar() || f.isExportar();
            if (!alguno) {
                continue; // no se guarda módulo sin ningún permiso
            }
            Permiso p = new Permiso();
            p.setIdRol(r.getIdRol());
            p.setIdOpcion(f.getIdOpcion());
            // Regla de coherencia: si tiene cualquier acción, debe poder VER el módulo
            p.setPermisoVer(f.isVer() || f.isCrear() || f.isEditar()
                    || f.isEliminar() || f.isExportar());
            p.setPermisoCrear(f.isCrear());
            p.setPermisoEditar(f.isEditar());
            p.setPermisoEliminar(f.isEliminar());
            p.setPermisoExportar(f.isExportar());
            p.setEstado(Constants.ESTADO_ACTIVO);
            permisoRepository.save(p);
            insertados++;
        }

        if (esBase(r) && insertados == 0) {
            // Rollback por excepción: el admin base nunca puede quedar sin permisos
            throw new BusinessException(
                    "El Perfil Administrador Base debe conservar al menos un permiso");
        }

        auditoriaService.registrarOk(
                "PERFIL_PERMISOS", "permisos", r.getIdRol(),
                "Actualización de matriz de permisos para perfil '" + r.getNombre()
                        + "' (módulos con acceso=" + insertados + ")");
    }
}