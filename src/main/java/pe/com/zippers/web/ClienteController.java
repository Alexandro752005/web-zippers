package pe.com.zippers.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import pe.com.zippers.common.Constants;
import pe.com.zippers.common.exception.BusinessException;
import pe.com.zippers.common.util.HtmxUtils;
import pe.com.zippers.service.ClienteService;
import pe.com.zippers.web.dto.AccesoWebDTO;
import pe.com.zippers.web.dto.ClienteFormDTO;

/**
 * Dispatcher HTMX del módulo Clientes.
 * GET   /modulo/clientes?action=list|table|form|detalle|acceso-web|compras
 * POST  /modulo/clientes?action=save-crear|save-editar|toggle|habilitar-web
 */
@Controller
@RequestMapping("/modulo/clientes")
public class ClienteController {

    private final ClienteService clienteService;

    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    // ============================ GET ============================
    @GetMapping // <-- RESPONDE A LA RAÍZ DEL MÓDULO
    @PreAuthorize("hasAuthority('CLIENTES_VER') or hasRole('ADMIN')")
    public String get(@RequestParam(defaultValue = "list") String action,
                      @RequestParam(required = false) Long id,
                      @RequestParam(required = false) String q,
                      Model model,
                      HttpServletRequest request) { // <-- Se inyecta request

        // Protege la navegación interna (anti-acceso directo)
        request.setAttribute(Constants.ATTR_FORWARD_INTERNO, Boolean.TRUE);

        switch (action) {
            case "table":
                model.addAttribute("clientes", clienteService.listar(q));
                model.addAttribute("filtro", q);
                return "modules/clientes/table :: tabla";

            case "form":
                if (id != null) {
                    model.addAttribute("cliente", clienteService.obtenerParaEditar(id));
                } else {
                    ClienteFormDTO nuevo = new ClienteFormDTO();
                    nuevo.setEstado("ACTIVO");
                    model.addAttribute("cliente", nuevo);
                }
                return "modules/clientes/form :: modal";

            case "detalle":
                if (id == null) throw new BusinessException("Debe indicar el cliente");
                model.addAttribute("c", clienteService.obtenerDetalle(id));
                return "modules/clientes/detalle :: modal";

            case "compras":
                if (id == null) throw new BusinessException("Debe indicar el cliente");
                model.addAttribute("cliente", clienteService.obtenerParaEditar(id));
                model.addAttribute("compras", clienteService.comprasDe(id));
                return "modules/clientes/compras :: modal";

            case "acceso-web":
                if (id == null) throw new BusinessException("Debe indicar el cliente");
                AccesoWebDTO acc = new AccesoWebDTO();
                acc.setIdCliente(id);
                model.addAttribute("acceso", acc);
                model.addAttribute("cliente", clienteService.obtenerParaEditar(id));
                return "modules/clientes/acceso-web :: modal";

            case "list":
            default:
                model.addAttribute("clientes", clienteService.listar(null));
                model.addAttribute("filtro", "");
                return "modules/clientes/list :: modulo";
        }
    }

    // ============================ POST ===========================
    @PostMapping
    @PreAuthorize("hasAuthority('CLIENTES_VER') or hasRole('ADMIN')")
    public String post(@RequestParam String action,
                       @Valid @ModelAttribute("cliente") ClienteFormDTO cliente,
                       BindingResult bindingCliente,
                       @RequestParam(required = false) Long idCliente,
                       Model model,
                       HttpServletRequest request) {

        // Protege la navegación interna (anti-acceso directo)
        request.setAttribute(Constants.ATTR_FORWARD_INTERNO, Boolean.TRUE);

        switch (action) {
            case "save-crear":
            case "save-editar": {
                if (bindingCliente.hasErrors()) {
                    model.addAttribute("cliente", cliente);
                    return "modules/clientes/form :: modal";
                }
                String msg;
                if ("save-crear".equals(action)) {
                    clienteService.crear(cliente);
                    msg = "Cliente registrado correctamente";
                } else {
                    clienteService.editar(cliente);
                    msg = "Cliente actualizado correctamente";
                }
                model.addAttribute("clientes", clienteService.listar(null));
                model.addAttribute("filtro", "");
                HtmxUtils.trigger(request, """
                        {"zpToast":{"tipo":"success","msg":"%s"},"zpClose":true}""".formatted(msg));
                return "modules/clientes/table :: tabla";
            }

            case "toggle": {
                ClienteFormDTO actual = clienteService.obtenerParaEditar(idCliente);
                String nuevo = "ACTIVO".equals(actual.getEstado()) ? "INACTIVO" : "ACTIVO";
                clienteService.cambiarEstado(idCliente, nuevo);
                model.addAttribute("clientes", clienteService.listar(null));
                model.addAttribute("filtro", "");
                HtmxUtils.trigger(request, """
                        {"zpToast":{"tipo":"success","msg":"Estado actualizado"},"zpClose":true}""");
                return "modules/clientes/table :: tabla";
            }

            default:
                throw new BusinessException("Acción no soportada: " + action);
        }
    }

    // POST separado para acceso web (su propio DTO + validación)
    @PostMapping(params = "action=habilitar-web")
    @PreAuthorize("hasAuthority('CLIENTES_EDITAR') or hasRole('ADMIN')")
    public String habilitarWeb(@Valid @ModelAttribute("acceso") AccesoWebDTO acceso,
                               BindingResult binding,
                               Model model,
                               HttpServletRequest request) {

        // Protege la navegación interna (anti-acceso directo)
        request.setAttribute(Constants.ATTR_FORWARD_INTERNO, Boolean.TRUE);

        if (binding.hasErrors()) {
            model.addAttribute("acceso", acceso);
            model.addAttribute("cliente", clienteService.obtenerParaEditar(acceso.getIdCliente()));
            return "modules/clientes/acceso-web :: modal";
        }

        clienteService.habilitarAccesoWeb(acceso);
        model.addAttribute("clientes", clienteService.listar(null));
        model.addAttribute("filtro", "");
        HtmxUtils.trigger(request, """
                {"zpToast":{"tipo":"success","msg":"Acceso web habilitado"},"zpClose":true}""");
        return "modules/clientes/table :: tabla";
    }
}