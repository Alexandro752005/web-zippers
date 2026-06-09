package pe.com.zippers.service.impl;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.com.zippers.common.Constants;
import pe.com.zippers.common.exception.BusinessException;
import pe.com.zippers.domain.Cliente;
import pe.com.zippers.domain.Rol;
import pe.com.zippers.domain.Usuario;
import pe.com.zippers.repository.ClienteRepository;
import pe.com.zippers.repository.RolRepository;
import pe.com.zippers.repository.UsuarioRepository;
import pe.com.zippers.service.AuditoriaService;
import pe.com.zippers.service.ClienteService;
import pe.com.zippers.web.dto.AccesoWebDTO;
import pe.com.zippers.web.dto.ClienteFormDTO;
import pe.com.zippers.web.dto.ClienteListItemDTO;
import pe.com.zippers.web.dto.CompraDTO;
import pe.com.zippers.web.dto.CompraItemDTO;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class ClienteServiceImpl implements ClienteService {

    private static final String COD_ROL_CLIENTE = "CLIENTE";

    private final ClienteRepository clienteRepository;
    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditoriaService auditoriaService;

    public ClienteServiceImpl(ClienteRepository clienteRepository,
                              UsuarioRepository usuarioRepository,
                              RolRepository rolRepository,
                              PasswordEncoder passwordEncoder,
                              AuditoriaService auditoriaService) {
        this.clienteRepository = clienteRepository;
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.passwordEncoder = passwordEncoder;
        this.auditoriaService = auditoriaService;
    }

    // ---------------------------------------------------------------------
    // Listado / lectura
    // ---------------------------------------------------------------------
    @Override
    @Transactional(readOnly = true)
    public List<ClienteListItemDTO> listar(String filtro) {
        List<Cliente> clientes = clienteRepository.buscar(filtro);
        List<ClienteListItemDTO> out = new ArrayList<>(clientes.size());
        for (Cliente c : clientes) {
            long compras = clienteRepository.contarComprasCompletadas(c.getIdCliente());
            out.add(new ClienteListItemDTO(
                    c.getIdCliente(), c.getDni(), c.getNombres(), c.getApellidos(),
                    c.getEmail(), c.getTelefono(), c.getEstado(),
                    c.getIdUsuario() != null, compras));
        }
        return out;
    }

    @Override
    @Transactional(readOnly = true)
    public ClienteFormDTO obtenerParaEditar(Long idCliente) {
        Cliente c = obtener(idCliente);
        ClienteFormDTO dto = new ClienteFormDTO();
        dto.setIdCliente(c.getIdCliente());
        dto.setDni(c.getDni());
        dto.setNombres(c.getNombres());
        dto.setApellidos(c.getApellidos());
        dto.setEmail(c.getEmail());
        dto.setTelefono(c.getTelefono());
        dto.setDireccion(c.getDireccion());
        dto.setReferenciaDireccion(c.getReferenciaDireccion());
        dto.setEstado(c.getEstado());
        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public Cliente obtenerDetalle(Long idCliente) {
        return obtener(idCliente);
    }

    private Cliente obtener(Long idCliente) {
        return clienteRepository.findById(idCliente)
                .orElseThrow(() -> new BusinessException("El cliente indicado no existe"));
    }

    // ---------------------------------------------------------------------
    // Compras del cliente (SQL nativo: ventas / detalle_venta)
    // ---------------------------------------------------------------------
    @Override
    @Transactional(readOnly = true)
    public long contarCompras(Long idCliente) {
        if (idCliente == null) return 0L;
        return clienteRepository.contarComprasCompletadas(idCliente);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CompraDTO> comprasDe(Long idCliente) {
        if (idCliente == null) throw new BusinessException("Debe indicar el cliente");
        // Valida que exista (mensaje claro si no).
        obtener(idCliente);

        List<Object[]> cabeceras = clienteRepository.cabecerasComprasDe(idCliente);
        List<CompraDTO> compras = new ArrayList<>(cabeceras.size());

        for (Object[] r : cabeceras) {
            CompraDTO compra = new CompraDTO(
                    toLong(r[0]),                 // id_venta
                    toStr(r[1]),                  // codigo_venta
                    toLocalDateTime(r[2]),        // fecha_venta
                    toStr(r[3]),                  // estado_venta
                    toStr(r[4]),                  // tipo_comprobante
                    toStr(r[5]),                  // metodo_pago
                    toBig(r[6]),                  // subtotal
                    toBig(r[7]),                  // descuento_aplicado
                    toBig(r[8]));                 // total

            List<Object[]> det = clienteRepository.detalleDeVenta(compra.getIdVenta());
            List<CompraItemDTO> items = new ArrayList<>(det.size());
            for (Object[] d : det) {
                items.add(new CompraItemDTO(
                        toLong(d[0]),             // id_detalle_venta
                        toStr(d[1]),              // producto_nombre_snapshot
                        toStr(d[2]),              // variante_snapshot
                        toInt(d[3]),              // cantidad
                        toBig(d[4]),              // precio_unitario
                        toBig(d[5])));            // subtotal
            }
            compra.setItems(items);
            compras.add(compra);
        }
        return compras;
    }

    /* --------- helpers de mapeo seguro desde Object[] (SQL nativo) --------- */

    private static Long toLong(Object o) {
        return (o == null) ? null : ((Number) o).longValue();
    }

    private static Integer toInt(Object o) {
        return (o == null) ? null : ((Number) o).intValue();
    }

    private static BigDecimal toBig(Object o) {
        if (o == null) return BigDecimal.ZERO;
        if (o instanceof BigDecimal b) return b;
        return new BigDecimal(o.toString());
    }

    private static String toStr(Object o) {
        return (o == null) ? null : o.toString();
    }

    private static LocalDateTime toLocalDateTime(Object o) {
        if (o == null) return null;
        if (o instanceof Timestamp ts) return ts.toLocalDateTime();
        if (o instanceof LocalDateTime ldt) return ldt;
        return null;
    }

    // ---------------------------------------------------------------------
    // Crear / Editar (validación congruente)
    // ---------------------------------------------------------------------
    @Override
    @Transactional
    public Long crear(ClienteFormDTO dto) {
        validar(dto, null);

        Cliente c = new Cliente();
        c.setIdUsuario(null);                 // nace como cliente POS (sin login)
        aplicar(dto, c);
        c.setEstado(Constants.ESTADO_ACTIVO); // nace ACTIVO

        Cliente guardado = clienteRepository.save(c);

        auditoriaService.registrarOk(
                "CLIENTE_CREAR", "clientes", guardado.getIdCliente(),
                "Alta de cliente DNI=" + guardado.getDni()
                        + " (" + guardado.getNombres() + " " + guardado.getApellidos() + ")");
        return guardado.getIdCliente();
    }

    @Override
    @Transactional
    public void editar(ClienteFormDTO dto) {
        Cliente c = obtener(dto.getIdCliente());
        validar(dto, c.getIdCliente());

        String estado = dto.getEstado();
        if (estado == null
                || !(Constants.ESTADO_ACTIVO.equals(estado) || Constants.ESTADO_INACTIVO.equals(estado))) {
            throw new BusinessException("Estado inválido");
        }

        aplicar(dto, c);
        c.setEstado(estado);
        clienteRepository.save(c);

        auditoriaService.registrarOk(
                "CLIENTE_EDITAR", "clientes", c.getIdCliente(),
                "Edición de cliente DNI=" + c.getDni() + " estado=" + estado);
    }

    private void aplicar(ClienteFormDTO dto, Cliente c) {
        c.setDni(dto.getDni());
        c.setNombres(dto.getNombres());
        c.setApellidos(dto.getApellidos());
        c.setEmail(dto.getEmail());
        c.setTelefono(dto.getTelefono());
        c.setDireccion(dto.getDireccion());
        c.setReferenciaDireccion(dto.getReferenciaDireccion());
    }

    /**
     * Validación de negocio estricta (defensa en profundidad).
     * Las colisiones cruzadas con la tabla usuarios las atrapa el
     * GlobalExceptionHandler (SQLSTATE 45000) si llegaran a la BD.
     */
    private void validar(ClienteFormDTO dto, Long idClienteActual) {
        if (dto.getDni() == null || !dto.getDni().matches("^[0-9]{8}$")) {
            throw new BusinessException("El DNI debe tener exactamente 8 dígitos");
        }
        if (dto.getTelefono() != null && !dto.getTelefono().matches("^[0-9]{9}$")) {
            throw new BusinessException("El teléfono debe tener exactamente 9 dígitos");
        }
        boolean dniDup = (idClienteActual == null)
                ? clienteRepository.existsByDni(dto.getDni())
                : clienteRepository.existsByDniAndIdClienteNot(dto.getDni(), idClienteActual);
        if (dniDup) {
            throw new BusinessException("Ya existe un cliente con ese DNI");
        }
        boolean emailDup = (idClienteActual == null)
                ? clienteRepository.existsByEmailIgnoreCase(dto.getEmail())
                : clienteRepository.existsByEmailIgnoreCaseAndIdClienteNot(dto.getEmail(), idClienteActual);
        if (emailDup) {
            throw new BusinessException("Ya existe un cliente con ese correo");
        }
    }

    // ---------------------------------------------------------------------
    // Soft delete (toggle)
    // ---------------------------------------------------------------------
    @Override
    @Transactional
    public void cambiarEstado(Long idCliente, String nuevoEstado) {
        Cliente c = obtener(idCliente);
        if (!(Constants.ESTADO_ACTIVO.equals(nuevoEstado) || Constants.ESTADO_INACTIVO.equals(nuevoEstado))) {
            throw new BusinessException("Estado inválido");
        }
        c.setEstado(nuevoEstado);
        clienteRepository.save(c);
        auditoriaService.registrarOk(
                "CLIENTE_TOGGLE", "clientes", c.getIdCliente(),
                "Cambio de estado de cliente DNI=" + c.getDni() + " a " + nuevoEstado);
    }

    // ---------------------------------------------------------------------
    // Onboarding digital: habilitar acceso web
    // ---------------------------------------------------------------------
    @Override
    @Transactional
    public void habilitarAccesoWeb(AccesoWebDTO dto) {
        Cliente c = obtener(dto.getIdCliente());

        if (c.getIdUsuario() != null) {
            throw new BusinessException("El cliente ya tiene acceso web habilitado");
        }
        if (!Constants.ESTADO_ACTIVO.equals(c.getEstado())) {
            throw new BusinessException("No se puede habilitar acceso web a un cliente inactivo");
        }

        // El correo de login no debe existir en usuarios
        if (usuarioRepository.existsByEmailIgnoreCase(dto.getEmail())) {
            auditoriaService.registrarDenegado(
                    "CLIENTE_ACCESO_WEB", "usuarios", c.getIdCliente(),
                    "Correo ya registrado en usuarios: " + dto.getEmail());
            throw new BusinessException("Ese correo ya está registrado como usuario del sistema");
        }

        Rol rolCliente = rolRepository.findByCodigo(COD_ROL_CLIENTE)
                .orElseThrow(() -> new BusinessException("No existe el rol CLIENTE en el sistema"));

        // Usuario portal: DNI técnico placeholder único (el DNI real vive en clientes
        // y los triggers cruzados impiden reutilizar el mismo DNI en usuarios).
        Usuario u = new Usuario();
        u.setIdRol(rolCliente.getIdRol());
        u.setUsername(generarUsername(c));
        u.setEmail(dto.getEmail());
        u.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
        u.setNombres(c.getNombres());
        u.setApellidos(c.getApellidos());
        u.setDni(generarDniTecnico(c.getIdCliente()));
        u.setTelefono(c.getTelefono());
        u.setEstado(Constants.ESTADO_ACTIVO);
        u.setIntentosFallidos(0);

        Usuario usuarioGuardado = usuarioRepository.save(u);

        // Vincular cliente ↔ usuario
        c.setIdUsuario(usuarioGuardado.getIdUsuario());
        clienteRepository.save(c);

        auditoriaService.registrarOk(
                "CLIENTE_ACCESO_WEB", "clientes", c.getIdCliente(),
                "Habilitación de acceso web. usuario_id=" + usuarioGuardado.getIdUsuario()
                        + " email=" + dto.getEmail());
    }

    private String generarUsername(Cliente c) {
        String base = ("cli_" + c.getDni());
        String candidato = base;
        int i = 1;
        while (usuarioRepository.existsByUsernameIgnoreCase(candidato)) {
            candidato = base + "_" + i;
            i++;
        }
        return candidato;
    }

    /**
     * DNI técnico para el usuario portal, único y no colisionable con DNIs reales
     * (8 dígitos). Usa prefijo '9' + relleno con el idCliente.
     */
    private String generarDniTecnico(Long idCliente) {
        String base = "9" + String.format("%07d", idCliente % 10_000_000L);
        String candidato = base;
        int i = 0;
        while (usuarioRepository.existsByDni(candidato)) {
            i++;
            candidato = "9" + String.format("%07d", (idCliente + i) % 10_000_000L);
        }
        return candidato;
    }
}