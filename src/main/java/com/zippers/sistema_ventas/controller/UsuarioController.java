package com.zippers.sistema_ventas.controller;

import com.zippers.sistema_ventas.dto.UsuarioDto;
import com.zippers.sistema_ventas.entity.AuditoriaLog;
import com.zippers.sistema_ventas.entity.Rol;
import com.zippers.sistema_ventas.entity.Usuario;
import com.zippers.sistema_ventas.repository.AuditoriaRepository;
import com.zippers.sistema_ventas.repository.RolRepository;
import com.zippers.sistema_ventas.repository.UsuarioRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@Controller
public class UsuarioController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RolRepository rolRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuditoriaRepository auditoriaRepository;

    @GetMapping("/dashboard/usuarios")
    public String listarUsuarios(
            @RequestParam(name = "buscar", required = false) String buscar,
            @RequestParam(name = "page", defaultValue = "0") int page,
            Model model) {
        
        cargarDatosTabla(buscar, page, model);
        model.addAttribute("roles", rolRepository.findAll());
        return "usuarios";
    }

    @GetMapping("/dashboard/usuarios/tabla")
    public String tablaUsuarios(
            @RequestParam(name = "buscar", required = false) String buscar,
            @RequestParam(name = "page", defaultValue = "0") int page,
            Model model) {
        
        cargarDatosTabla(buscar, page, model);
        return "usuarios :: #tabla-usuarios";
    }

    private void cargarDatosTabla(String buscar, int page, Model model) {
        Pageable pageable = PageRequest.of(page, 10);
        Page<Usuario> paginaUsuarios;

        if (buscar != null && !buscar.isEmpty()) {
            paginaUsuarios = usuarioRepository.buscarPorTerminoNoClientes(buscar, pageable);
        } else {
            paginaUsuarios = usuarioRepository.buscarUsuariosNoClientes(pageable);
        }

        model.addAttribute("usuarios", paginaUsuarios.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", paginaUsuarios.getTotalPages());
        model.addAttribute("buscar", buscar);
    }

    @GetMapping("/dashboard/usuarios/formulario")
    public String nuevoUsuarioForm(Model model) {
        model.addAttribute("usuarioDto", new UsuarioDto());
        model.addAttribute("roles", rolRepository.findAll());
        return "usuarios-form :: form-usuario";
    }

    @GetMapping("/dashboard/usuarios/formulario/{id}")
    public String editarUsuarioForm(@PathVariable("id") Integer id, Model model) {
        if (id == null) {
            throw new RuntimeException("El id de usuario no puede ser nulo");
        }
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        UsuarioDto dto = new UsuarioDto();
        dto.setId(usuario.getId_usuario());
        dto.setNombreCompleto(usuario.getNombre_completo());
        dto.setEmail(usuario.getEmail());
        dto.setEstado(usuario.getEstado());
        
        if (usuario.getRol() != null) {
            dto.setRolId(usuario.getRol().getId_rol());
        }

        model.addAttribute("usuarioDto", dto);
        model.addAttribute("roles", rolRepository.findAll());
        return "usuarios-form :: form-usuario";
    }

    @PostMapping("/dashboard/usuarios/guardar")
    public String guardarUsuario(@Valid @ModelAttribute("usuarioDto") UsuarioDto dto,
                                 BindingResult result,
                                 Model model,
                                 Principal principal) {
                                     
        if (dto.getId() == null && (dto.getPassword() == null || dto.getPassword().trim().isEmpty())) {
            result.rejectValue("password", "NotBlank", "La contraseña es obligatoria para un nuevo usuario");
        }

        if (result.hasErrors()) {
            model.addAttribute("roles", rolRepository.findAll());
            return "usuarios-form :: form-usuario";
        }

        Usuario usuario;
        boolean isNew = (dto.getId() == null);

        if (!isNew) {
            Integer idUsuario = dto.getId();
            if (idUsuario == null) {
                throw new RuntimeException("El ID no puede ser nulo en edición");
            }
            
            usuario = usuarioRepository.findById(idUsuario)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
                    
            usuario.setNombre_completo(dto.getNombreCompleto());
            usuario.setEmail(dto.getEmail());
            
            if ("ADMIN".equalsIgnoreCase(usuario.getRol().getNombre())) {
                usuario.setEstado("ACTIVO");
            } else {
                usuario.setEstado(dto.getEstado());
            }

            if (dto.getPassword() != null && !dto.getPassword().trim().isEmpty()) {
                usuario.setPassword_hash(passwordEncoder.encode(dto.getPassword()));
            }
            
            // SOLUCIÓN: Asignamos a variable local para que el compilador confíe en la validación de nulos
            Integer rolId = dto.getRolId();
            if (rolId != null) {
                Rol rol = rolRepository.findById(rolId)
                        .orElseThrow(() -> new RuntimeException("Rol no encontrado"));
                usuario.setRol(rol);
            }
        } else {
            // SOLUCIÓN: Asignamos a variable local para que el compilador confíe en la validación de nulos
            Integer rolId = dto.getRolId();
            if (rolId == null) {
                result.rejectValue("rolId", "NotNull", "Debe seleccionar un perfil");
                model.addAttribute("roles", rolRepository.findAll());
                return "usuarios-form :: form-usuario";
            }
            
            usuario = new Usuario();
            usuario.setNombre_completo(dto.getNombreCompleto());
            usuario.setEmail(dto.getEmail());
            usuario.setPassword_hash(passwordEncoder.encode(dto.getPassword()));
            usuario.setEstado("ACTIVO");
            
            Rol rol = rolRepository.findById(rolId)
                    .orElseThrow(() -> new RuntimeException("Rol no encontrado"));
            usuario.setRol(rol);
        }

        Usuario guardado = usuarioRepository.save(usuario);

        if (principal != null) {
            Usuario usuarioEjecutor = usuarioRepository.findByEmail(principal.getName()).orElse(null);
            if (usuarioEjecutor != null) {
                AuditoriaLog log = new AuditoriaLog();
                log.setUsuario(usuarioEjecutor);
                log.setAccion(isNew ? "INSERT" : "UPDATE");
                log.setTabla_afectada("usuarios");
                log.setRegistro_id(guardado.getId_usuario());
                log.setDescripcion("Acción sobre usuario: " + guardado.getEmail());
                auditoriaRepository.save(log);
            }
        }

        return "usuarios-form :: exito";
    }

    @GetMapping("/dashboard/usuarios/eliminar/{id}")
    public String eliminarUsuario(@PathVariable("id") int id, Principal principal) {
        Usuario usuario = usuarioRepository.findById(id).orElse(null);

        if (usuario != null) {
            if ("ADMIN".equalsIgnoreCase(usuario.getRol().getNombre())) {
                throw new RuntimeException("Acción denegada: No se puede desactivar a un Administrador principal.");
            }
            
            usuario.setEstado("INACTIVO");
            usuarioRepository.save(usuario);

            if (principal != null) {
                Usuario usuarioEjecutor = usuarioRepository.findByEmail(principal.getName()).orElse(null);
                if (usuarioEjecutor != null) {
                    AuditoriaLog log = new AuditoriaLog();
                    log.setUsuario(usuarioEjecutor);
                    log.setAccion("UPDATE"); 
                    log.setTabla_afectada("usuarios");
                    log.setRegistro_id(id);
                    log.setDescripcion("Usuario desactivado (Soft Delete)");
                    auditoriaRepository.save(log);
                }
            }
        }
        
        return "usuarios-form :: exito";
    }
}