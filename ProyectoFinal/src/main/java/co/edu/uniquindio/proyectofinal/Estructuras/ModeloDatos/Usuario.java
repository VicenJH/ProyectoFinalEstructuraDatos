
package co.edu.uniquindio.proyectofinal.Estructuras.ModeloDatos;

import co.edu.uniquindio.proyectofinal.Estructuras.SimpleList.SimpleLinkedList;

/**
 * Representa a un usuario registrado en el sistema Fintech.
 *
 * Un usuario puede administrar varias billeteras, acumular puntos
 * de fidelización y consultar su historial de transacciones y alertas.
 *
 * Implementa Comparable para permitir su organización en el árbol
 * binario de búsqueda según los puntos acumulados.
 *
 * Cuando dos usuarios tienen los mismos puntos, se utiliza su
 * identificador como criterio secundario de comparación.
 *
 * @author Proyecto Fintech
 * @version 1.0
 */
public class Usuario implements Comparable<Usuario> {

    /** Identificador único del usuario. */
    public String id;

    /** Nombre completo del usuario. */
    public String nombre;

    /** Correo electrónico del usuario. */
    public String correo;

    /** Puntos acumulados por operaciones válidas. */
    public int puntosAcumulados;

    /** Nivel de fidelización actual. */
    public String nivel;

    /** Billeteras asociadas al usuario. */
    public SimpleLinkedList<Billetera> billeteras;

    /** Historial de transacciones del usuario. */
    public SimpleLinkedList<Transaccion> historialTransacciones;

    /** Historial de alertas generadas para el usuario. */
    public SimpleLinkedList<Alerta> historialAlertas;

    /**
     * Construye un usuario nuevo.
     *
     * @param id identificador único.
     * @param nombre nombre del usuario.
     * @param correo correo electrónico.
     *
     * @throws IllegalArgumentException si alguno de los textos
     * obligatorios es null o está vacío.
     */
    public Usuario(String id, String nombre, String correo) {
        validarTexto(id, "El identificador");
        validarTexto(nombre, "El nombre");
        validarTexto(correo, "El correo");

        this.id = id;
        this.nombre = nombre;
        this.correo = correo;
        this.puntosAcumulados = 0;
        this.nivel = "Bronce";

        this.billeteras = new SimpleLinkedList<>();
        this.historialTransacciones = new SimpleLinkedList<>();
        this.historialAlertas = new SimpleLinkedList<>();
    }

    /**
     * Actualiza el nivel según los puntos acumulados.
     *
     * Rangos conservados del proyecto:
     * - Bronce: hasta 500 puntos.
     * - Plata: más de 500 y hasta 1000.
     * - Oro: más de 1000 y hasta 5000.
     * - Platino: más de 5000.
     */
    public void actualizarNivel() {
        if (puntosAcumulados > 5000) {
            nivel = "Platino";
        } else if (puntosAcumulados > 1000) {
            nivel = "Oro";
        } else if (puntosAcumulados > 500) {
            nivel = "Plata";
        } else {
            nivel = "Bronce";
        }
    }

    /**
     * Agrega puntos al usuario.
     *
     * @param puntos cantidad que se agregará.
     * @throws IllegalArgumentException si la cantidad es negativa.
     * @throws ArithmeticException si se produce desbordamiento entero.
     */
    public void agregarPuntos(int puntos) {
        if (puntos < 0) {
            throw new IllegalArgumentException(
                    "No se pueden agregar puntos negativos."
            );
        }

        puntosAcumulados = Math.addExact(puntosAcumulados, puntos);
        actualizarNivel();
    }

    /**
     * Resta puntos al usuario.
     *
     * Se utiliza principalmente al revertir operaciones financieras.
     *
     * @param puntos cantidad que se descontará.
     * @throws IllegalArgumentException si la cantidad es negativa.
     */
    public void descontarPuntos(int puntos) {
        if (puntos < 0) {
            throw new IllegalArgumentException(
                    "La cantidad a descontar no puede ser negativa."
            );
        }

        if (puntos > puntosAcumulados) {
            throw new IllegalArgumentException(
                    "El usuario no tiene suficientes puntos."
            );
        }

        puntosAcumulados -= puntos;
        actualizarNivel();
    }

    /**
     * Compara dos usuarios por sus puntos acumulados.
     *
     * Si tienen la misma cantidad de puntos, compara sus identificadores.
     *
     * @param otro usuario que se comparará.
     * @return valor negativo, cero o positivo según el orden.
     * @throws NullPointerException si el otro usuario es null.
     */
    @Override
    public int compareTo(Usuario otro) {
        if (otro == null) {
            throw new NullPointerException(
                    "No se puede comparar con un usuario null."
            );
        }

        int comparacionPuntos = Integer.compare(
                this.puntosAcumulados,
                otro.puntosAcumulados
        );

        if (comparacionPuntos != 0) {
            return comparacionPuntos;
        }

        return this.id.compareTo(otro.id);
    }

    /** @return identificador del usuario. */
    public String getId() {
        return id;
    }

    /** @param id nuevo identificador. */
    public void setId(String id) {
        validarTexto(id, "El identificador");
        this.id = id;
    }

    /** @return nombre del usuario. */
    public String getNombre() {
        return nombre;
    }

    /** @param nombre nuevo nombre. */
    public void setNombre(String nombre) {
        validarTexto(nombre, "El nombre");
        this.nombre = nombre;
    }

    /** @return correo electrónico. */
    public String getCorreo() {
        return correo;
    }

    /** @param correo nuevo correo. */
    public void setCorreo(String correo) {
        validarTexto(correo, "El correo");
        this.correo = correo;
    }

    /** @return puntos acumulados. */
    public int getPuntosAcumulados() {
        return puntosAcumulados;
    }

    /**
     * Establece los puntos acumulados.
     *
     * @param puntosAcumulados nueva cantidad, no negativa.
     */
    public void setPuntosAcumulados(int puntosAcumulados) {
        if (puntosAcumulados < 0) {
            throw new IllegalArgumentException(
                    "Los puntos no pueden ser negativos."
            );
        }

        this.puntosAcumulados = puntosAcumulados;
        actualizarNivel();
    }

    /** @return nivel de fidelización. */
    public String getNivel() {
        return nivel;
    }

    /**
     * Establece manualmente el nivel.
     *
     * @param nivel nuevo nivel.
     */
    public void setNivel(String nivel) {
        validarTexto(nivel, "El nivel");
        this.nivel = nivel;
    }

    /** @return lista de billeteras. */
    public SimpleLinkedList<Billetera> getBilleteras() {
        return billeteras;
    }

    /** @return historial de transacciones. */
    public SimpleLinkedList<Transaccion> getHistorialTransacciones() {
        return historialTransacciones;
    }

    /** @return historial de alertas. */
    public SimpleLinkedList<Alerta> getHistorialAlertas() {
        return historialAlertas;
    }

    /**
     * Valida un texto obligatorio.
     *
     * @param texto texto recibido.
     * @param nombre nombre del dato.
     */
    private static void validarTexto(String texto, String nombre) {
        if (texto == null || texto.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    nombre + " no puede estar vacío."
            );
        }
    }
}
