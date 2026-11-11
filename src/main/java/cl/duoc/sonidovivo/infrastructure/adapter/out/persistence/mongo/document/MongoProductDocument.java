package cl.duoc.sonidovivo.infrastructure.adapter.out.persistence.mongo.document;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * MongoDB document mapping for the {@code productos} collection in {@code sonido_vivo_nosql}.
 */
@Document(collection = "productos")
@CompoundIndexes({
    @CompoundIndex(name = "idx_cat_precio", def = "{'categoria': 1, 'precio_clp': 1}"),
    @CompoundIndex(name = "idx_espec_tipo", def = "{'especificaciones.tipo': 1}", sparse = true)
})
public class MongoProductDocument {

    @Id
    private String id;

    @Indexed(name = "idx_codigo_unique", unique = true)
    private String codigo;

    private String nombre;
    private String categoria;

    @Field("precio_clp")
    private Integer precioClp;

    @Field("stock_disponible")
    private Integer stockDisponible;

    private String estado;

    private Map<String, Object> especificaciones;

    @Indexed(name = "idx_tags")
    private List<String> tags;

    private List<ResenaEmbeddable> resenas;

    @Field("ultima_venta")
    private Instant ultimaVenta;

    @Field("fecha_baja")
    private Instant fechaBaja;

    @Field("created_at")
    private Instant createdAt;

    @Field("updated_at")
    private Instant updatedAt;

    public MongoProductDocument() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public Integer getPrecioClp() {
        return precioClp;
    }

    public void setPrecioClp(Integer precioClp) {
        this.precioClp = precioClp;
    }

    public Integer getStockDisponible() {
        return stockDisponible;
    }

    public void setStockDisponible(Integer stockDisponible) {
        this.stockDisponible = stockDisponible;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public Map<String, Object> getEspecificaciones() {
        return especificaciones;
    }

    public void setEspecificaciones(Map<String, Object> especificaciones) {
        this.especificaciones = especificaciones;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public List<ResenaEmbeddable> getResenas() {
        return resenas;
    }

    public void setResenas(List<ResenaEmbeddable> resenas) {
        this.resenas = resenas;
    }

    public Instant getUltimaVenta() {
        return ultimaVenta;
    }

    public void setUltimaVenta(Instant ultimaVenta) {
        this.ultimaVenta = ultimaVenta;
    }

    public Instant getFechaBaja() {
        return fechaBaja;
    }

    public void setFechaBaja(Instant fechaBaja) {
        this.fechaBaja = fechaBaja;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
