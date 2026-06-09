package pe.com.zippers.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import pe.com.zippers.common.Constants;
import pe.com.zippers.common.util.HtmxUtils;
import pe.com.zippers.security.SecurityUtils;
import pe.com.zippers.service.UsuarioService;
import pe.com.zippers.web.dto.UsuarioCreateDTO;
import pe.com.zippers.web.dto.UsuarioUpdateDTO;

@Controller
@RequestMapping("/modulo/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    @GetMapping
    @PreAuthorize("hasAuthority('USUARIOS_VER') or hasRole('ADMIN')")
    public String get(@RequestParam(defaultValue = "list") String action,
                      @RequestParam(required = false) Long id,
                      @RequestParam(required = false) String q,
                      HttpServletRequest request,
                      Model model) {

        request.setAttribute(Constants.ATTR_FORWARD_INTERNO, Boolean.TRUE);

        switch (action) {
            case "table":
                model.addAttribute("usuarios", usuarioService.listar(q));
                model.addAttribute("q", q);
                return "modules/usuarios/table :: tabla";

            case "form":
                model.addAttribute("roles", usuarioService.rolesSeleccionables());
                if (id == null) {
                    model.addAttribute("modo", "crear");
                    model.addAttribute("usuario", new UsuarioCreateDTO());
                } else {
                    model.addAttribute("modo", "editar");
                    model.addAttribute("usuario", usuarioService.obtenerParaEditar(id));
                }
                return "modules/usuarios/form :: modal";

            case "list":
            default:
                model.addAttribute("usuarios", usuarioService.listar(q));
                model.addAttribute("q", q);
                return "modules/usuarios/list"; // Retorno estándar de vista completa sin fragmento
        }
    }

    @PostMapping(params = "action=save-crear")
    @PreAuthorize("hasAuthority('USUARIOS_CREAR') or hasRole('ADMIN')")
    public String saveCrear(@Valid @ModelAttribute("usuario") UsuarioCreateDTO dto, 
                            BindingResult br, Model model, HttpServletRequest request) {
        request.setAttribute(Constants.ATTR_FORWARD_INTERNO, Boolean.TRUE);
        if (br.hasErrors()) {
            model.addAttribute("modo", "crear");
            model.addAttribute("roles", usuarioService.rolesSeleccionables());
            return "modules/usuarios/form :: modal";
        }
        usuarioService.crear(dto, SecurityUtils.idUsuarioActual());
        model.addAttribute("usuarios", usuarioService.listar(""));
        HtmxUtils.trigger(request, "{\"zpToast\":{\"tipo\":\"success\",\"msg\":\"Usuario registrado con éxito\"},\"zpClose\":true}");
        return "modules/usuarios/table :: tabla";
    }

    @PostMapping(params = "action=save-editar")
    @PreAuthorize("hasAuthority('USUARIOS_EDITAR') or hasRole('ADMIN')")
    public String saveEditar(@Valid @ModelAttribute("usuario") UsuarioUpdateDTO dto, 
                             BindingResult br, Model model, HttpServletRequest request) {
        request.setAttribute(Constants.ATTR_FORWARD_INTERNO, Boolean.TRUE);
        if (br.hasErrors()) {
            model.addAttribute("modo", "editar");
            model.addAttribute("roles", usuarioService.rolesSeleccionables());
            return "modules/usuarios/form :: modal";
        }
        usuarioService.actualizar(dto, SecurityUtils.idUsuarioActual());
        model.addAttribute("usuarios", usuarioService.listar(""));
        HtmxUtils.trigger(request, "{\"zpToast\":{\"tipo\":\"success\",\"msg\":\"Usuario actualizado correctamente\"},\"zpClose\":true}");
        return "modules/usuarios/table :: tabla";
    }

    @PostMapping(params = "action=toggle")
    @PreAuthorize("hasAuthority('USUARIOS_ELIMINAR') or hasRole('ADMIN')")
    public String toggle(@RequestParam Long idUsuario, Model model, HttpServletRequest request) {
        request.setAttribute(Constants.ATTR_FORWARD_INTERNO, Boolean.TRUE);
        usuarioService.desactivar(idUsuario, SecurityUtils.idUsuarioActual());
        model.addAttribute("usuarios", usuarioService.listar(""));
        HtmxUtils.trigger(request, "{\"zpToast\":{\"tipo\":\"success\",\"msg\":\"Estado de cuenta modificado\"}}");
        return "modules/usuarios/table :: tabla";
    }
}