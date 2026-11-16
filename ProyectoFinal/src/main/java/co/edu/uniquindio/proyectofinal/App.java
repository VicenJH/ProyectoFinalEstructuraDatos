
package co.edu.uniquindio.proyectofinal;

import co.edu.uniquindio.proyectofinal.Estructuras.Controllers.FintechController;
import co.edu.uniquindio.proyectofinal.vistas.*;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * Ventana principal de la plataforma Fintech.
 *
 * Todos los paneles comparten un único FintechController, que es la fachada
 * del sistema. Al cambiar de pestaña, el panel se actualiza con el estado actual.
 */
public class App extends Application {

    private final FintechController controlador = new FintechController();
    private TabPane pestanas;

    @Override
    public void start(Stage escenario) {
        pestanas = new TabPane(
                new Tab("Usuarios y billeteras", new PanelUsuarios(controlador)),
                new Tab("Operaciones", new PanelOperaciones(controlador))
        );
        pestanas.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        pestanas.getSelectionModel().selectedItemProperty().addListener((obs, anterior, pestana) -> {
            if (pestana != null && pestana.getContent() instanceof Refrescable) {
                ((Refrescable) pestana.getContent()).refrescar();
            }
        });

        MenuItem cargarDemo = new MenuItem("Cargar datos de ejemplo");
        cargarDemo.setOnAction(e -> cargarDatosDeEjemplo());

        MenuBar menu = new MenuBar(new Menu("Datos", null, cargarDemo));

        BorderPane raiz = new BorderPane(pestanas);
        raiz.setTop(menu);

        escenario.setTitle("Fintech - Billeteras digitales");
        escenario.setScene(new Scene(raiz, 1150, 720));
        escenario.show();
    }

    private void cargarDatosDeEjemplo() {
        if (DatosDemo.cargar(controlador)) {
            refrescarTodo();
            Util.mostrar(Alert.AlertType.INFORMATION, "Datos de ejemplo",
                    "Se cargaron 4 usuarios, 7 billeteras, transferencias y operaciones programadas.");
        } else {
            Util.mostrar(Alert.AlertType.INFORMATION, "Datos de ejemplo",
                    "Los datos de ejemplo ya estaban cargados.");
        }
    }

    private void refrescarTodo() {
        for (Tab pestana : pestanas.getTabs()) {
            if (pestana.getContent() instanceof Refrescable) {
                ((Refrescable) pestana.getContent()).refrescar();
            }
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}