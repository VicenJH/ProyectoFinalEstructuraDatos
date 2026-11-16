package co.edu.uniquindio.proyectofinal.Estructuras.ModeloDatos;

import java.time.LocalDateTime;

/**
 * Comprobante inmutable de un canje. Conserva una copia de los datos del
 * beneficio al momento de redimirlo, aunque después el catálogo sea editado.
 * El estado controla si la recompensa ya fue utilizada por el usuario.
 */
public class CanjeBeneficio {

    public static final String DISPONIBLE = "DISPONIBLE";
    public static final String UTILIZADO = "UTILIZADO";

    private final String id;
    private final String codigoRedencion;
    private final String idUsuario;
    private final String idBeneficio;
    private final String nombreBeneficio;
    private final String descripcionBeneficio;
    private final LocalDateTime fechaHora;
    private final int puntosGastados;
    private final int valorBono;
    private String estado;
    private LocalDateTime fechaUso;

    public CanjeBeneficio(String id, String idUsuario, Beneficio beneficio,
                          LocalDateTime fechaHora, int puntosGastados) {
        if (textoVacio(id) || textoVacio(idUsuario) || beneficio == null
                || fechaHora == null || puntosGastados <= 0) {
            throw new IllegalArgumentException("Los datos del canje no son válidos.");
        }
        if (puntosGastados != beneficio.getCostoPuntos()) {
            throw new IllegalArgumentException("Los puntos del canje deben coincidir con el costo del beneficio.");
        }
        this.id = id.trim();
        this.codigoRedencion = "RED-" + this.id;
        this.idUsuario = idUsuario.trim();
        this.idBeneficio = beneficio.getId();
        this.nombreBeneficio = beneficio.getNombre();
        this.descripcionBeneficio = beneficio.getDescripcion();
        this.fechaHora = fechaHora;
        this.puntosGastados = puntosGastados;
        this.valorBono = beneficio.getValorBono();
        this.estado = DISPONIBLE;
    }

    private CanjeBeneficio(CanjeBeneficio original) {
        this.id = original.id;
        this.codigoRedencion = original.codigoRedencion;
        this.idUsuario = original.idUsuario;
        this.idBeneficio = original.idBeneficio;
        this.nombreBeneficio = original.nombreBeneficio;
        this.descripcionBeneficio = original.descripcionBeneficio;
        this.fechaHora = original.fechaHora;
        this.puntosGastados = original.puntosGastados;
        this.valorBono = original.valorBono;
        this.estado = original.estado;
        this.fechaUso = original.fechaUso;
    }

    /** Copia defensiva que conserva el estado del comprobante. */
    public CanjeBeneficio copia() {
        return new CanjeBeneficio(this);
    }

    /** Reconstruye un cupón desde un respaldo persistido. */
    public static CanjeBeneficio restaurar(String id, String codigoRedencion, String idUsuario,
                                            String idBeneficio, String nombreBeneficio,
                                            String descripcionBeneficio, LocalDateTime fechaHora,
                                            int puntosGastados, int valorBono, String estado,
                                            LocalDateTime fechaUso) {
        Beneficio beneficio = new Beneficio(idBeneficio, nombreBeneficio, descripcionBeneficio,
                puntosGastados, valorBono);
        CanjeBeneficio canje = new CanjeBeneficio(id, idUsuario, beneficio, fechaHora, puntosGastados);
        if (codigoRedencion != null && !codigoRedencion.isBlank()
                && !canje.codigoRedencion.equals(codigoRedencion)) {
            throw new IllegalArgumentException("El código de redención del respaldo no coincide.");
        }
        if (UTILIZADO.equals(estado)) {
            canje.estado = UTILIZADO;
            canje.fechaUso = fechaUso == null ? fechaHora : fechaUso;
        } else if (!DISPONIBLE.equals(estado)) {
            throw new IllegalArgumentException("Estado de cupón desconocido: " + estado);
        }
        return canje;
    }

    private static boolean textoVacio(String texto) {
        return texto == null || texto.trim().isEmpty();
    }

    /** Marca la recompensa como utilizada. No permite consumirla dos veces. */
    public boolean marcarComoUtilizado() {
        if (!DISPONIBLE.equals(estado)) return false;
        estado = UTILIZADO;
        fechaUso = LocalDateTime.now();
        return true;
    }

    public String getId() { return id; }
    public String getCodigoRedencion() { return codigoRedencion; }
    public String getIdUsuario() { return idUsuario; }
    public String getIdBeneficio() { return idBeneficio; }
    public String getNombreBeneficio() { return nombreBeneficio; }
    public String getDescripcionBeneficio() { return descripcionBeneficio; }
    public LocalDateTime getFechaHora() { return fechaHora; }
    public int getPuntosGastados() { return puntosGastados; }
    public int getValorBono() { return valorBono; }
    public String getEstado() { return estado; }
    public LocalDateTime getFechaUso() { return fechaUso; }
    public boolean isDisponible() { return DISPONIBLE.equals(estado); }
    public boolean isUtilizado() { return UTILIZADO.equals(estado); }

    @Override
    public String toString() {
        return codigoRedencion + " - " + nombreBeneficio + " - " + puntosGastados
                + " puntos - " + estado + " - " + fechaHora;
    }
}
