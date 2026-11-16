package co.edu.uniquindio.proyectofinal.vistas;

/**
 * Panel que puede volver a cargar sus datos desde el controlador.
 * La ventana principal lo invoca al cambiar de pestaña, de modo que cada
 * panel siempre muestre el estado actual del sistema.
 */
public interface Refrescable {

    /** Vuelve a leer los datos del controlador y actualiza la vista. */
    void refrescar();
}