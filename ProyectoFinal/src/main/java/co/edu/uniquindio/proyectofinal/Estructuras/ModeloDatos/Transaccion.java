package co.edu.uniquindio.proyectofinal.Estructuras.ModeloDatos;

import java.time.LocalDateTime;

/**
 * Representa una transacción financiera realizada dentro del sistema Fintech.
 *
 * Hereda de OperacionFinanciera los atributos comunes (id, valor, billeteras,
 * fecha, tipo y estado) y agrega los puntos generados y el nivel de riesgo
 * asignado por el módulo de detección de comportamiento inusual.
 *
 * @author Proyecto Fintech
 * @version 1.1
 */
public class Transaccion extends OperacionFinanciera {

    /** Puntos generados por la transacción. No puede ser negativo. */
    private int puntosGenerados;

    /** Nivel de riesgo asignado. Por defecto BAJO. */
    private NivelRiesgo nivelRiesgo;

    /**
     * Construye una transacción con todos sus datos.
     *
     * @throws IllegalArgumentException si los puntos son negativos o algún
     * dato heredado incumple las validaciones del padre.
     */
    public Transaccion(
            String id,
            double valor,
            String billeteraOrigen,
            String billeteraDestino,
            LocalDateTime fechaHora,
            TipoTransaccion tipo,
            String estado,
            int puntosGenerados
    ) {
        super(id, valor, billeteraOrigen, billeteraDestino, fechaHora, tipo, estado);

        validarPuntos(puntosGenerados);
        this.puntosGenerados = puntosGenerados;
        this.nivelRiesgo = NivelRiesgo.BAJO;
    }

    public int getPuntosGenerados() {
        return puntosGenerados;
    }

    public void setPuntosGenerados(int puntosGenerados) {
        validarPuntos(puntosGenerados);
        this.puntosGenerados = puntosGenerados;
    }

    /** @return nivel de riesgo de la transacción. */
    public NivelRiesgo getNivelRiesgo() {
        return nivelRiesgo;
    }

    /**
     * @param nivelRiesgo nuevo nivel de riesgo.
     * @throws IllegalArgumentException si es null.
     */
    public void setNivelRiesgo(NivelRiesgo nivelRiesgo) {
        if (nivelRiesgo == null) {
            throw new IllegalArgumentException("El nivel de riesgo no puede ser null.");
        }
        this.nivelRiesgo = nivelRiesgo;
    }

    @Override
    public String getEstado() {
        return super.getEstado();
    }

    @Override
    public void setEstado(String estado) {
        super.setEstado(estado);
    }

    private void validarPuntos(int puntos) {
        if (puntos < 0) {
            throw new IllegalArgumentException("Los puntos generados no pueden ser negativos.");
        }
    }
}