package co.edu.uniquindio.proyectofinal.Estructuras.ModeloDatos;

/**
 * Beneficio que un usuario puede obtener utilizando sus puntos de fidelización.
 */
public class Beneficio {

    private final String id;
    private String nombre;
    private String descripcion;
    private int costoPuntos;
    private boolean activo;

    public Beneficio(String id, String nombre, String descripcion, int costoPuntos) {
        this.id = validarTexto(id, "El identificador");
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del beneficio es obligatorio.");
        }
        if (descripcion == null || descripcion.trim().isEmpty()) {
            throw new IllegalArgumentException("La descripción del beneficio es obligatoria.");
        }
        if (costoPuntos <= 0) {
            throw new IllegalArgumentException("El costo del beneficio debe ser positivo.");
        }
        this.nombre = nombre.trim();
        this.descripcion = descripcion.trim();
        this.costoPuntos = costoPuntos;
        this.activo = true;
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
            throw new IllegalArgumentException("El costo en puntos debe ser positivo.");
        }
        this.costoPuntos = costoPuntos;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    @Override
    public String toString() {
        return nombre + " (" + costoPuntos + " puntos)";
    }
}
