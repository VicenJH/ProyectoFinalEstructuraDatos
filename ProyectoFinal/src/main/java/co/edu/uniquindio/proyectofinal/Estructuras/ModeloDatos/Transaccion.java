package co.edu.uniquindio.proyectofinal.Estructuras.ModeloDatos;

import java.time.LocalDateTime;

/** Registro de una operación ejecutada, con puntos generados y nivel de riesgo. */
public class Transaccion extends OperacionFinanciera {

    private final int puntosGenerados;
    private final NivelRiesgo nivelRiesgo;

    public Transaccion(String id, double valor, String billeteraOrigen, String billeteraDestino,
                       LocalDateTime fechaHora, TipoTransaccion tipo, String estado,
                       int puntosGenerados) {
        this(id, valor, billeteraOrigen, billeteraDestino, fechaHora, tipo, estado,
                puntosGenerados, NivelRiesgo.BAJO);
    }

    public Transaccion(String id, double valor, String billeteraOrigen, String billeteraDestino,
                       LocalDateTime fechaHora, TipoTransaccion tipo, String estado,
                       int puntosGenerados, NivelRiesgo nivelRiesgo) {
        super(id, valor, billeteraOrigen, billeteraDestino, fechaHora, tipo, estado);
        if (puntosGenerados < 0) throw new IllegalArgumentException("Los puntos no pueden ser negativos.");
        if (nivelRiesgo == null) throw new IllegalArgumentException("El nivel de riesgo no puede ser null.");
        this.puntosGenerados = puntosGenerados;
        this.nivelRiesgo = nivelRiesgo;
    }

    public int getPuntosGenerados() { return puntosGenerados; }
    public NivelRiesgo getNivelRiesgo() { return nivelRiesgo; }

    /** Única transición de estado permitida después de ejecutar una transacción. */
    public void marcarRevertida() {
        if (!"EXITOSA".equals(getEstado())) {
            throw new IllegalStateException("Solo se puede revertir una transacción exitosa.");
        }
        cambiarEstado("REVERTIDA");
    }
}
