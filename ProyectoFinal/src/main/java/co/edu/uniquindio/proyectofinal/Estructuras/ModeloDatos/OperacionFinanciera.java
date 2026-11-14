package co.edu.uniquindio.proyectofinal.Estructuras.ModeloDatos;

import java.time.LocalDateTime;

public abstract class OperacionFinanciera {
    public String id;
    public double valor;
    public String billeteraOrigen;
    public String billeteraDestino;
    public LocalDateTime fechaHora; // ¡Subido al padre!
    public TipoTransaccion tipo;
    public String estado;

    public OperacionFinanciera(String id, double valor, String billeteraOrigen, String billeteraDestino, LocalDateTime fechaHora, TipoTransaccion tipo,  String estado) {
        this.id = id;
        this.valor = valor;
        this.billeteraOrigen = billeteraOrigen;
        this.billeteraDestino = billeteraDestino;
        this.fechaHora = fechaHora;
        this.tipo = tipo;
        this.estado = estado;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public double getValor() { return valor; }
    public void setValor(double valor) { this.valor = valor; }

    public String getBilleteraOrigen() { return billeteraOrigen; }
    public void setBilleteraOrigen(String billeteraOrigen) { this.billeteraOrigen = billeteraOrigen; }

    public String getBilleteraDestino() { return billeteraDestino; }
    public void setBilleteraDestino(String billeteraDestino) { this.billeteraDestino = billeteraDestino; }

    public LocalDateTime getFechaHora() { return fechaHora; }
    public void setFechaHora(LocalDateTime fechaHora) { this.fechaHora = fechaHora; }

    public TipoTransaccion getTipo() { return tipo; }
    public void setTipo(TipoTransaccion tipo) { this.tipo = tipo; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
}