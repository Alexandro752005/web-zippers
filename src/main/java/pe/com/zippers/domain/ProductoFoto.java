package pe.com.zippers.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "productos_fotos")
public class ProductoFoto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_foto")
    private Long idFoto;

    @Column(name = "id_producto", nullable = false)
    private Long idProducto;

    @Column(name = "url_foto", length = 255, nullable = false)
    private String urlFoto;

    @Column(name = "alt_text", length = 255)
    private String altText;

    @Column(name = "orden", nullable = false)
    private Integer orden;

    @Column(name = "es_principal", nullable = false)
    private Boolean esPrincipal;

    @Column(name = "estado", length = 20, nullable = false)
    private String estado;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    public Long getIdFoto() { return idFoto; }
    public void setIdFoto(Long v) { this.idFoto = v; }
    public Long getIdProducto() { return idProducto; }
    public void setIdProducto(Long v) { this.idProducto = v; }
    public String getUrlFoto() { return urlFoto; }
    public void setUrlFoto(String v) { this.urlFoto = v; }
    public String getAltText() { return altText; }
    public void setAltText(String v) { this.altText = v; }
    public Integer getOrden() { return orden; }
    public void setOrden(Integer v) { this.orden = v; }
    public Boolean getEsPrincipal() { return esPrincipal; }
    public void setEsPrincipal(Boolean v) { this.esPrincipal = v; }
    public String getEstado() { return estado; }
    public void setEstado(String v) { this.estado = v; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}