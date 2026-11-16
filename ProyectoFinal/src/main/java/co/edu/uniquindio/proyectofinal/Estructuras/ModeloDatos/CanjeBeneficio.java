package co.edu.uniquindio.proyectofinal.Estructuras.ModeloDatos;

import java.time.LocalDateTime;

/** Registro inmutable de un canje realizado por un usuario. */
public class CanjeBeneficio {

    private final String id;
    private final String idUsuario;
    private final String idBeneficio;
    private final String nombreBeneficio;
    private final LocalDateTime fechaHora;
    private final int puntosGastados;
    private final String estado;

    public CanjeBeneficio(String id, String idUsuario, Beneficio beneficio,
                          LocalDateTime fechaHora, int puntosGastados) {
        if (id == null || id.trim().isEmpty()
                || idUsuario == null || idUsuario.trim().isEmpty()
                || beneficio == null || fechaHora == null || puntosGastados <= 0) {
            throw new IllegalArgumentException("Los datos del canje no son válidos.");
        }
        this.id = id.trim();
        this.idUsuario = idUsuario.trim();
        this.idBeneficio = beneficio.getId();
        this.nombreBeneficio = beneficio.getNombre();
        this.fechaHora = fechaHora;
        this.puntosGastados = puntosGastados;
        this.estado = "REALIZADO";
    }

    public String getId() { return id; }
    public String getIdUsuario() { return idUsuario; }
    public String getIdBeneficio() { return idBeneficio; }
    public String getNombreBeneficio() { return nombreBeneficio; }
    public LocalDateTime getFechaHora() { return fechaHora; }
    public int getPuntosGastados() { return puntosGastados; }
    public String getEstado() { return estado; }

    @Override
    public String toString() {
        return id + " - " + nombreBeneficio + " - " + puntosGastados + " puntos - " + fechaHora;
    }
}
