package co.edu.uniquindio.proyectofinal.Estructuras.ModeloDatos;

import java.util.Objects;

/** Recompensa del catálogo, canjeable por puntos y aplicable como bono de saldo. */
public class Beneficio {

    private final String id;
    private String nombre;
    private String descripcion;
    private int costoPuntos;
    private int valorBono;
    private boolean activo;

    /** Constructor de compatibilidad para recompensas sin abono monetario. */
    public Beneficio(String id, String nombre, String descripcion, int costoPuntos) {
        this(id, nombre, descripcion, costoPuntos, 0);
    }

    public Beneficio(String id, String nombre, String descripcion, int costoPuntos, int valorBono) {
        this.id = normalizarId(id);
        actualizarDatos(nombre, descripcion, costoPuntos);
        setValorBono(valorBono);
        this.activo = true;
    }

    public void actualizarDatos(String nombre, String descripcion, int costoPuntos) {
        String nombreValidado = validarTexto(nombre, "El nombre");
        String descripcionValidada = validarTexto(descripcion, "La descripción");
        if (costoPuntos <= 0) throw new IllegalArgumentException("El costo en puntos debe ser mayor que cero.");
        this.nombre = nombreValidado;
        this.descripcion = descripcionValidada;
        this.costoPuntos = costoPuntos;
    }

    public Beneficio copia() {
        Beneficio copia = new Beneficio(id, nombre, descripcion, costoPuntos, valorBono);
        copia.setActivo(activo);
        return copia;
    }

    private static String normalizarId(String id) {
        return validarTexto(id, "El identificador").toUpperCase();
    }

    private static String validarTexto(String texto, String campo) {
        if (texto == null || texto.trim().isEmpty()) throw new IllegalArgumentException(campo + " no puede estar vacío.");
        return texto.trim();
    }

    public String getId() { return id; }
    public String getNombre() { return nombre; }
    public String getDescripcion() { return descripcion; }
    public int getCostoPuntos() { return costoPuntos; }
    public int getValorBono() { return valorBono; }
    public boolean isActivo() { return activo; }
    public void setNombre(String nombre) { this.nombre = validarTexto(nombre, "El nombre"); }
    public void setDescripcion(String descripcion) { this.descripcion = validarTexto(descripcion, "La descripción"); }
    public void setCostoPuntos(int costoPuntos) {
        if (costoPuntos <= 0) throw new IllegalArgumentException("El costo debe ser positivo.");
        this.costoPuntos = costoPuntos;
    }
    public void setValorBono(int valorBono) {
        if (valorBono < 0) throw new IllegalArgumentException("El valor del bono no puede ser negativo.");
        this.valorBono = valorBono;
    }
    public void setActivo(boolean activo) { this.activo = activo; }

    @Override public boolean equals(Object objeto) {
        return this == objeto || objeto instanceof Beneficio otro && id.equals(otro.id);
    }
    @Override public int hashCode() { return Objects.hash(id); }
    @Override public String toString() {
        return id + " - " + nombre + " (" + costoPuntos + " puntos; bono $" + valorBono + ")";
    }
}
