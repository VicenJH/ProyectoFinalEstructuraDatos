package co.edu.uniquindio.proyectofinal.Presentacion;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;

import java.text.Normalizer;
import java.util.Locale;

/**
 * Convierte el texto de una columna (estado, tipo, nivel de riesgo...) en una
 * "etiqueta" de color, para que las tablas sean fáciles de leer en la sustentación.
 * Los colores se definen en {@code fintech.css} (clases {@code tag-*}).
 */
public final class EtiquetasTabla {

    private EtiquetasTabla() {
    }

    /** Aplica la etiqueta de color a una columna. */
    public static <S, T> void aplicar(TableColumn<S, T> columna) {
        if (columna != null) {
            columna.setCellFactory(c -> new CeldaEtiqueta<>());
        }
    }

    /** Devuelve la clase CSS asociada a un texto (por ejemplo "EXITOSA" → tag-ok). */
    static String claseParaTexto(String texto) {
        String t = Normalizer.normalize(texto, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toUpperCase(Locale.ROOT)
                .trim();
        switch (t) {
            case "EXITOSA", "ACTIVO", "ACTIVA", "APROBADO", "EJECUTADA", "DISPONIBLE",
                 "BAJO", "ATENDIDA", "SI", "RECARGA":
                return "tag-ok";
            case "PENDIENTE", "MEDIO", "UTILIZADO":
                return "tag-warn";
            case "RECHAZADA", "BAJA", "INACTIVA", "FALLO", "ALTO", "RETIRO":
                return "tag-bad";
            case "REVERTIDA", "BONIFICACION", "PLATINO":
                return "tag-violet";
            case "BRONCE":
                return "tag-bronze";
            case "PLATA":
                return "tag-silver";
            case "ORO":
                return "tag-gold";
            default:
                if (t.startsWith("TRANSFERENCIA")) {
                    return "tag-info";
                }
                return "tag-neutral";
        }
    }

    private static final class CeldaEtiqueta<S, T> extends TableCell<S, T> {
        private final Label etiqueta = new Label();

        CeldaEtiqueta() {
            setAlignment(Pos.CENTER_LEFT);
        }

        @Override
        protected void updateItem(T item, boolean vacio) {
            super.updateItem(item, vacio);
            if (vacio || item == null) {
                setGraphic(null);
                setText(null);
                return;
            }
            String texto = item.toString();
            etiqueta.setText(texto);
            etiqueta.getStyleClass().setAll("tag", claseParaTexto(texto));
            setText(null);
            setGraphic(etiqueta);
        }
    }
}
