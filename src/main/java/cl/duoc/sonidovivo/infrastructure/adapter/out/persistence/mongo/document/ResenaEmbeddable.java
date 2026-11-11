package cl.duoc.sonidovivo.infrastructure.adapter.out.persistence.mongo.document;

import java.time.Instant;

/**
 * Embedded MongoDB subdocument representing a customer product review inside {@code productos.resenas}.
 */
public class ResenaEmbeddable {

    private String cliente;
    private Integer calificacion;
    private String comentario;
    private Instant fecha;

    public ResenaEmbeddable() {
    }

    public ResenaEmbeddable(String cliente, Integer calificacion, String comentario, Instant fecha) {
        this.cliente = cliente;
        this.calificacion = calificacion;
        this.comentario = comentario;
        this.fecha = fecha;
    }

    public String getCliente() {
        return cliente;
    }

    public void setCliente(String cliente) {
        this.cliente = cliente;
    }

    public Integer getCalificacion() {
        return calificacion;
    }

    public void setCalificacion(Integer calificacion) {
        this.calificacion = calificacion;
    }

    public String getComentario() {
        return comentario;
    }

    public void setComentario(String comentario) {
        this.comentario = comentario;
    }

    public Instant getFecha() {
        return fecha;
    }

    public void setFecha(Instant fecha) {
        this.fecha = fecha;
    }
}
