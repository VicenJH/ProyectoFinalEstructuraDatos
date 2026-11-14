package co.edu.uniquindio.proyectofinal.Estructuras.ModeloDatos;

import java.time.LocalDateTime;

public class OperacionProgramada extends OperacionFinanciera {
    private Frecuencia frecuencia;

    public OperacionProgramada(String id, double valor, String billeteraOrigen, String billeteraDestino, LocalDateTime fechaHora, TipoTransaccion tipo, Frecuencia frecuencia, String estado) {
        super(id, valor, billeteraOrigen, billeteraDestino, fechaHora, tipo, estado);
        this.frecuencia = frecuencia;
    }

    public Frecuencia getFrecuencia() {
        return frecuencia;
    }

    public void setFrecuencia(Frecuencia frecuencia) {
        this.frecuencia = frecuencia;
    }
}