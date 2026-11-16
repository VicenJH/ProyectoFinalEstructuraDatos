package co.edu.uniquindio.proyectofinal.Estructuras.ModeloDatos;

import java.util.Objects;

/**
 * Elemento del catálogo que puede ser canjeado con puntos de fidelización.
 * El beneficio representa una recompensa/bono que se entrega al usuario;
 * su aplicación económica concreta depende de las reglas del producto.
 */
public class Beneficio {

    private final String id;
    private String nombre;
    private String descripcion;
    private int costoPuntos;
    private boolean activo;

    public Beneficio(String id, String nombre, String descripcion, int costoPuntos) {
        this.id = normalizarId(id);
        actualizarDatos(nombre, descripcion, costoPuntos);
        this.activo = true;
    }

    /** Actualiza los datos comerciales sin cambiar la identidad del beneficio. */
    public void actualizarDatos(String nombre, String descripcion, int costoPuntos) {
        String nombreValidado = validarTexto(nombre, "El nombre");
        String descripcionValidada = validarTexto(descripcion, "La descripción");
        if (costoPuntos <= 0) {
            throw new IllegalArgumentException("El costo del beneficio debe ser mayor que cero.");
        }
        this.nombre = nombreValidado;
        this.descripcion = descripcionValidada;
        this.costoPuntos = costoPuntos;
    }

    /** Copia defensiva para que las vistas no modifiquen el catálogo interno. */
    public Beneficio copia() {
        Beneficio copia = new Beneficio(id, nombre, descripcion, costoPuntos);
        copia.setActivo(activo);
        return copia;
    }

    private static String normalizarId(String id) {
        return validarTexto(id, "El identificador").toUpperCase();
    }

    private static String validarTexto(String texto, String campo) {
        if (texto == null || texto.trim().isEmpty()) {
            throw new IllegalArgumentException(campo + " no puede estar vacío.");
        }
        return texto.trim();
    }

    public String getId() { return id; }
    public String getNombre() { return nombre; }
    public String getDescripcion() { return descripcion; }
    public int getCostoPuntos() { return costoPuntos; }
    public boolean isActivo() { return activo; }

    public void setNombre(String nombre) {
        this.nombre = validarTexto(nombre, "El nombre");
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = validarTexto(descripcion, "La descripción");
    }

    public void setCostoPuntos(int costoPuntos) {
        if (costoPuntos <= 0) {
            throw new IllegalArgumentException("El costo del beneficio debe ser mayor que cero.");
        }
        this.costoPuntos = costoPuntos;
    }

    public void setActivo(boolean activo) { this.activo = activo; }

    @Override
    public boolean equals(Object objeto) {
        if (this == objeto) return true;
        if (!(objeto instanceof Beneficio)) return false;
        Beneficio otro = (Beneficio) objeto;
        return id.equals(otro.id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }

    @Override
    public String toString() {
        return id + " - " + nombre + " (" + costoPuntos + " puntos)";
    }
}
