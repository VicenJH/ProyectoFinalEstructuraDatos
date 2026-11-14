package co.edu.uniquindio.proyectofinal.Estructuras.ModeloDatos;

import java.time.LocalDateTime;

public class Transaccion extends OperacionFinanciera {

    private int puntosGenerados;


    public Transaccion(String id, double valor, String billeteraOrigen, String billeteraDestino,
                       LocalDateTime fechaHora, TipoTransaccion tipo, String estado, int puntosGenerados) {
        super(id, valor, billeteraOrigen, billeteraDestino, fechaHora, tipo,  estado);
        this.estado = estado;
        this.puntosGenerados = puntosGenerados;
    }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public int getPuntosGenerados() { return puntosGenerados; }
    public void setPuntosGenerados(int puntosGenerados) { this.puntosGenerados = puntosGenerados; }
}
