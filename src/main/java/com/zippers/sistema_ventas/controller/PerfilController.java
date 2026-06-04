package com.zippers.sistema_ventas.controller;

import com.zippers.sistema_ventas.entity.*;
import com.zippers.sistema_ventas.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.stream.Collectors;

import java.security.Principal;
import java.util.*;

@Controller
@PreAuthorize("hasRole('ADMIN')")
public class PerfilController {

    @Autowired
    private RolRepository rolRepository;

    @Autowired
    private OpcionRepository opcionRepository;

    @Autowired
    private PermisoRepository permisoRepository;

    @Autowired
    private AuditoriaRepository auditoriaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    // 1. CARGAR LA VISTA PRINCIPAL
    @GetMapping("/dashboard/perfiles")
    public String listarPerfiles(Model model) {
        List<Rol> roles = rolRepository.findAll();
        roles = roles.stream()
                .filter(r -> !"CLIENTE".equalsIgnoreCase(r.getNombre()))
                .collect(Collectors.toList());
        model.addAttribute("roles", roles);
        return "perfiles";
    }

    // 2. RECARGAR SOLO LA TABLA (SPA)
    @GetMapping("/dashboard/perfiles/tabla")
    public String tablaPerfiles(Model model) {
        List<Rol> roles = rolRepository.findAll();
        roles = roles.stream()
                .filter(r -> !"CLIENTE".equalsIgnoreCase(r.getNombre()))
                .collect(Collectors.toList());
        model.addAttribute("roles", roles);
        return "perfiles :: #tabla-perfiles";
    }

    // 3. OBTENER EL FORMULARIO DE PERFIL (Nuevo o Editar)
    @GetMapping("/dashboard/perfiles/formulario")
    public String formularioPerfil(@RequestParam(value = "id", required = false) Integer id, Model model) {
        Rol rol = (id != null) ? rolRepository.findById(id).orElse(new Rol()) : new Rol();
        model.addAttribute("rol", rol);
        return "perfiles-form :: form-perfil";
    }

    // 4. GUARDAR PERFIL Y ESTADO
    @PostMapping("/dashboard/perfiles/guardar")
    public String guardarRol(@ModelAttribute Rol rol, Principal principal) {
        if (rol == null) throw new IllegalArgumentException("El rol no puede ser nulo");
        
        boolean isNew = (rol.getId_rol() == null);
        if (isNew) {
            rol.setEstado("ACTIVO"); // Los nuevos nacen activos
        }
        
        Rol guardado = rolRepository.save(rol);

        // Auditoría
        if (principal != null) {
            Usuario usuarioEjecutor = usuarioRepository.findByEmail(principal.getName()).orElse(null);
            if (usuarioEjecutor != null) {
                AuditoriaLog log = new AuditoriaLog();
                log.setUsuario(usuarioEjecutor);
                log.setAccion(isNew ? "INSERT" : "UPDATE");
                log.setTabla_afectada("roles");
                log.setRegistro_id(guardado.getId_rol());
                log.setDescripcion("Gestión de perfil: " + guardado.getNombre());
                auditoriaRepository.save(log);
            }
        }
        
        return "perfiles-form :: exito";
    }

    // 5. OBTENER EL FORMULARIO DE PERMISOS
    @GetMapping("/dashboard/perfiles/permisos/{id}")
    public String formularioPermisos(@PathVariable("id") int id, Model model) {
        Rol rol = rolRepository.findById(id).orElse(null);
        List<Opcion> opciones = opcionRepository.findAll();
        List<Permiso> permisosActuales = permisoRepository.findByIdRol(id);
        
        Map<Integer, Permiso> permisoMap = permisosActuales.stream()
                .collect(Collectors.toMap(Permiso::getId_opcion, p -> p));

        model.addAttribute("rol", rol);
        model.addAttribute("opciones", opciones);
        model.addAttribute("permisoMap", permisoMap);
        
        return "perfiles-form :: form-permisos";
    }

    // 6. GUARDAR LOS PERMISOS
    @PostMapping("/dashboard/perfiles/permisos/guardar")
    public String guardarPermisos(@RequestParam("id_rol") int id,
                                  @RequestParam Map<String, String> allParams,
                                  Principal principal) {
        Rol rol = rolRepository.findById(id).orElse(null);
        if (rol == null) return "error";

        permisoRepository.deleteByIdRol(id);

        Map<Integer, Permiso> nuevosPermisos = new LinkedHashMap<>();
        for (Map.Entry<String, String> entry : allParams.entrySet()) {
            String key = entry.getKey();
            if (key.startsWith("checkbox_")) {
                String[] parts = key.split("_");
                if (parts.length < 3) continue;
                String tipo = parts[1];
                int idOpcion = Integer.parseInt(parts[2]);
                boolean valor = "on".equals(entry.getValue()) || "true".equals(entry.getValue());

                Permiso permiso = nuevosPermisos.computeIfAbsent(idOpcion, k -> {
                    Permiso p = new Permiso();
                    p.setId_rol(id);
                    p.setId_opcion(k);
                    return p;
                });

                switch (tipo) {
                    case "crear"    -> permiso.setPermiso_crear(valor);
                    case "leer"     -> permiso.setPermiso_leer(valor);
                    case "editar"   -> permiso.setPermiso_editar(valor);
                    case "eliminar" -> permiso.setPermiso_eliminar(valor);
                    case "exportar" -> permiso.setPermiso_exportar(valor);
                }
            }
        }

        for (Permiso p : nuevosPermisos.values()) {
            if (p != null) {
                permisoRepository.save(p);
            }
        }

        if (principal != null) {
            Usuario usuarioEjecutor = usuarioRepository.findByEmail(principal.getName()).orElse(null);
            if (usuarioEjecutor != null) {
                AuditoriaLog log = new AuditoriaLog();
                log.setUsuario(usuarioEjecutor);
                log.setAccion("UPDATE");
                log.setTabla_afectada("permisos");
                log.setRegistro_id(id);
                log.setDescripcion("Permisos actualizados para el rol: " + rol.getNombre());
                auditoriaRepository.save(log);
            }
        }

        return "perfiles-form :: exito";
    }
}