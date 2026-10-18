package cl.tnt.notifications.dto;

import java.io.Serializable;
import java.time.LocalDateTime;

public class PedidoEventDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String cliente;
    private String producto;
    private Double monto;
    private String estado;
    private LocalDateTime fecha;
    private String tipoEvento;

    public PedidoEventDTO() {
    }

    public PedidoEventDTO(Long id, String cliente, String producto, Double monto, String estado, LocalDateTime fecha, String tipoEvento) {
        this.id = id;
        this.cliente = cliente;
        this.producto = producto;
        this.monto = monto;
        this.estado = estado;
        this.fecha = fecha;
        this.tipoEvento = tipoEvento;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCliente() {
        return cliente;
    }

    public void setCliente(String cliente) {
        this.cliente = cliente;
    }

    public String getProducto() {
        return producto;
    }

    public void setProducto(String producto) {
        this.producto = producto;
    }

    public Double getMonto() {
        return monto;
    }

    public void setMonto(Double monto) {
        this.monto = monto;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public String getTipoEvento() {
        return tipoEvento;
    }

    public void setTipoEvento(String tipoEvento) {
        this.tipoEvento = tipoEvento;
    }

    @Override
    public String toString() {
        return "PedidoEventDTO{" +
                "id=" + id +
                ", cliente='" + cliente + '\'' +
                ", producto='" + producto + '\'' +
                ", monto=" + monto +
                ", estado='" + estado + '\'' +
                ", fecha=" + fecha +
                ", tipoEvento='" + tipoEvento + '\'' +
                '}';
    }
}
