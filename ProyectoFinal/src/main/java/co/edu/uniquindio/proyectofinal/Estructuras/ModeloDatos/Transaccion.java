package co.edu.uniquindio.proyectofinal.Estructuras.ModeloDatos;

import java.time.LocalDateTime;

/** Registro de una operación financiera ejecutada o rechazada. */
public class Transaccion extends OperacionFinanciera {

    public static final String EXITOSA = "EXITOSA";
    public static final String REVERTIDA = "REVERTIDA";
    public static final String RECHAZADA = "RECHAZADA";

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
        if (!EXITOSA.equals(estado) && !REVERTIDA.equals(estado) && !RECHAZADA.equals(estado)) {
            throw new IllegalArgumentException("Estado de transacción desconocido: " + estado);
        }
        if (RECHAZADA.equals(estado) && puntosGenerados != 0) {
            throw new IllegalArgumentException("Una transacción rechazada no puede generar puntos.");
        }
        this.puntosGenerados = puntosGenerados;
        this.nivelRiesgo = nivelRiesgo;
    }

    private Transaccion(Transaccion original) {
        this(original.getId(), original.getValor(), original.getBilleteraOrigen(),
                original.getBilleteraDestino(), original.getFechaHora(), original.getTipo(),
                original.getEstado(), original.puntosGenerados, original.nivelRiesgo);
    }

    public Transaccion copia() { return new Transaccion(this); }
    public int getPuntosGenerados() { return puntosGenerados; }
    public NivelRiesgo getNivelRiesgo() { return nivelRiesgo; }

    /** Única transición de estado de una operación ejecutada. */
    public void marcarRevertida() {
        if (!EXITOSA.equals(getEstado())) {
            throw new IllegalStateException("Solo se puede revertir una transacción exitosa.");
        }
        cambiarEstado(REVERTIDA);
    }
}
