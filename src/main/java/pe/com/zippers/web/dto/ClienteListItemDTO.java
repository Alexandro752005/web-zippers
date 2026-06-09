package pe.com.zippers.web.dto;

public class ClienteListItemDTO {

    private Long idCliente;
    private String dni;
    private String nombres;
    private String apellidos;
    private String email;
    private String telefono;
    private String estado;
    private boolean tieneAccesoWeb;   // id_usuario != null
    private long totalCompras;        // cantidad de ventas COMPLETADAS del cliente

    public ClienteListItemDTO() { }

    /** Constructor original (compatibilidad). Deja totalCompras en 0. */
    public ClienteListItemDTO(Long idCliente, String dni, String nombres, String apellidos,
                              String email, String telefono, String estado, boolean tieneAccesoWeb) {
        this(idCliente, dni, nombres, apellidos, email, telefono, estado, tieneAccesoWeb, 0L);
    }

    /** Constructor extendido con contador de compras. */
    public ClienteListItemDTO(Long idCliente, String dni, String nombres, String apellidos,
                              String email, String telefono, String estado,
                              boolean tieneAccesoWeb, long totalCompras) {
        this.idCliente = idCliente;
        this.dni = dni;
        this.nombres = nombres;
        this.apellidos = apellidos;
        this.email = email;
        this.telefono = telefono;
        this.estado = estado;
        this.tieneAccesoWeb = tieneAccesoWeb;
        this.totalCompras = totalCompras;
    }

    public Long getIdCliente() { return idCliente; }
    public void setIdCliente(Long idCliente) { this.idCliente = idCliente; }
    public String getDni() { return dni; }
    public void setDni(String dni) { this.dni = dni; }
    public String getNombres() { return nombres; }
    public void setNombres(String nombres) { this.nombres = nombres; }
    public String getApellidos() { return apellidos; }
    public void setApellidos(String apellidos) { this.apellidos = apellidos; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public boolean isTieneAccesoWeb() { return tieneAccesoWeb; }
    public void setTieneAccesoWeb(boolean v) { this.tieneAccesoWeb = v; }
    public long getTotalCompras() { return totalCompras; }
    public void setTotalCompras(long totalCompras) { this.totalCompras = totalCompras; }
}