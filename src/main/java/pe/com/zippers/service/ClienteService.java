package pe.com.zippers.service;

import pe.com.zippers.domain.Cliente;
import pe.com.zippers.web.dto.AccesoWebDTO;
import pe.com.zippers.web.dto.ClienteFormDTO;
import pe.com.zippers.web.dto.ClienteListItemDTO;
import pe.com.zippers.web.dto.CompraDTO;

import java.util.List;

public interface ClienteService {

    List<ClienteListItemDTO> listar(String filtro);

    ClienteFormDTO obtenerParaEditar(Long idCliente);

    Cliente obtenerDetalle(Long idCliente);

    Long crear(ClienteFormDTO dto);

    void editar(ClienteFormDTO dto);

    void cambiarEstado(Long idCliente, String nuevoEstado);

    /** Onboarding digital: crea usuario portal (rol CLIENTE) y vincula al cliente. */
    void habilitarAccesoWeb(AccesoWebDTO dto);

    /** Cantidad de compras COMPLETADAS del cliente (para la columna "Compras"). */
    long contarCompras(Long idCliente);

    /** Historial de compras COMPLETADAS del cliente, con su detalle (modal "Compras"). */
    List<CompraDTO> comprasDe(Long idCliente);
}