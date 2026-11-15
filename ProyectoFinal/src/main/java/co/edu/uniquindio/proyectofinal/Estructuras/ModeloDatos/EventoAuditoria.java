package co.edu.uniquindio.proyectofinal.Estructuras.ModeloDatos;

import java.time.LocalDateTime;

/**
 * Registro inmutable del historial de auditoría. Se crea cada vez que el
 * sistema detecta una transacción con riesgo MEDIO o ALTO y permite su
 * consulta posterior.
 *
 * @author Proyecto Fintech
 * @version 1.0
 */
public class EventoAuditoria {

    private final int id;
    private final LocalDateTime fecha;
    private final String idUsuario;
    private final String idTransaccion;
    private final NivelRiesgo nivel;
    private final String motivo;

    /**
     * @throws IllegalArgumentException si el id no es positivo, hay datos
     * null o textos vacíos.
     */
    public EventoAuditoria(
            int id,
            LocalDateTime fecha,
            String idUsuario,
            String idTransaccion,
            NivelRiesgo nivel,
            String motivo
    ) {
        if (id <= 0) {
            throw new IllegalArgumentException("El identificador debe ser positivo.");
        }
        if (fecha == null || nivel == null) {
            throw new IllegalArgumentException("La fecha y el nivel de riesgo no pueden ser null.");
        }
        if (esVacio(idUsuario) || esVacio(idTransaccion) || esVacio(motivo)) {
            throw new IllegalArgumentException("El usuario, la transacción y el motivo son obligatorios.");
        }

        this.id = id;
        this.fecha = fecha;
        this.idUsuario = idUsuario;
        this.idTransaccion = idTransaccion;
        this.nivel = nivel;
        this.motivo = motivo;
    }

    public int getId() {
        return id;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public String getIdUsuario() {
        return idUsuario;
    }

    public String getIdTransaccion() {
        return idTransaccion;
    }

    public NivelRiesgo getNivel() {
        return nivel;
    }

    public String getMotivo() {
        return motivo;
    }

    private static boolean esVacio(String texto) {
        return texto == null || texto.trim().isEmpty();
    }

    @Override
    public String toString() {
        return "[" + fecha + "] " + nivel + " - usuario " + idUsuario
                + " - " + idTransaccion + ": " + motivo;
    }
}