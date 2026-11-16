package co.edu.uniquindio.proyectofinal.vistas;

import co.edu.uniquindio.proyectofinal.Estructuras.ModeloDatos.Alerta;
import co.edu.uniquindio.proyectofinal.Estructuras.ModeloDatos.Usuario;
import co.edu.uniquindio.proyectofinal.Estructuras.SimpleList.SimpleLinkedList;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableColumn;

import java.time.format.DateTimeFormatter;
import java.util.function.Function;

/**
 * Utilidades compartidas por los paneles de la interfaz.
 */
public final class Util {

    /** Formato de fecha y hora que se muestra al usuario. */
    public static final DateTimeFormatter FORMATO_FECHA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    private Util() {
    }

    /** Copia una lista enlazada propia a una lista observable de JavaFX. */
    public static <T> ObservableList<T> aLista(SimpleLinkedList<T> origen) {
        ObservableList<T> resultado = FXCollections.observableArrayList();

        for (T elemento : origen) {
            resultado.add(elemento);
        }

        return resultado;
    }

    /** Igual que aLista, pero con el último elemento primero (más reciente arriba). */
    public static <T> ObservableList<T> aListaInversa(SimpleLinkedList<T> origen) {
        ObservableList<T> resultado = FXCollections.observableArrayList();

        for (T elemento : origen) {
            resultado.add(0, elemento);
        }

        return resultado;
    }

    /** Crea una columna de tabla a partir de una función, sin usar reflexión. */
    public static <S, T> TableColumn<S, T> columna(String titulo, Function<S, T> valor) {
        TableColumn<S, T> columna = new TableColumn<>(titulo);
        columna.setCellValueFactory(
                celda -> new ReadOnlyObjectWrapper<>(valor.apply(celda.getValue()))
        );
        return columna;
    }

    /** @return el entero escrito en el texto, o null si no es un número válido. */
    public static Integer entero(String texto) {
        if (texto == null) {
            return null;
        }

        try {
            return Integer.parseInt(texto.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static void mostrar(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alerta = new Alert(tipo, mensaje, ButtonType.OK);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.showAndWait();
    }

    /** Pide confirmación al usuario. */
    public static boolean confirmar(String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.CONFIRMATION, mensaje, ButtonType.YES, ButtonType.NO);
        alerta.setTitle("Confirmar");
        alerta.setHeaderText(null);
        return alerta.showAndWait().orElse(ButtonType.NO) == ButtonType.YES;
    }

    /**
     * Junta los mensajes de las alertas que el usuario recibió desde una posición.
     * Sirve para explicar el resultado de una operación (rechazos, saldo bajo, etc.).
     *
     * @param desde cantidad de alertas que tenía antes de la operación.
     */
    public static String alertasNuevas(Usuario usuario, int desde) {
        StringBuilder texto = new StringBuilder();
        int indice = 0;

        for (Alerta alerta : usuario.getHistorialAlertas()) {
            if (indice >= desde) {
                if (texto.length() > 0) {
                    texto.append(" · ");
                }
                texto.append(alerta.getMensaje());
            }
            indice++;
        }

        return texto.toString();
    }
}