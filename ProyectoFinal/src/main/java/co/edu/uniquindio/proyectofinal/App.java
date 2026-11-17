package co.edu.uniquindio.proyectofinal;

import co.edu.uniquindio.proyectofinal.Presentacion.ContextoAplicacion;
import co.edu.uniquindio.proyectofinal.Presentacion.ViewControllers.LoginViewController;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.util.function.BiConsumer;

/** Punto de entrada JavaFX. Inicializa persistencia y el procesador de operaciones. */
public class App extends Application {

    private ContextoAplicacion contexto;
    private Timeline procesadorAutomatico;

    @Override
    public void start(Stage escenario) {
        try {
            contexto = ContextoAplicacion.iniciar();
            mostrarVentana(escenario, contexto, "/fxml/inicio.fxml", "NEXO · Billetera digital",
                    1120, 760, (controller, stage) ->
                            ((LoginViewController) controller).configurar(contexto, stage));
            iniciarProcesamientoAutomatico();
        } catch (IOException | RuntimeException ex) {
            Alert alerta = new Alert(Alert.AlertType.ERROR);
            alerta.setTitle("No se pudo iniciar NEXO");
            alerta.setHeaderText("Error al cargar los datos del sistema");
            alerta.setContentText(ex.getMessage() == null ? "Error inesperado al iniciar." : ex.getMessage());
            alerta.showAndWait();
            escenario.close();
        }
    }

    public static Stage mostrarVentana(Stage ventanaAnterior, ContextoAplicacion contexto,
                                       String recursoFxml, String titulo, double ancho, double alto,
                                       BiConsumer<Object, Stage> configurarControlador) {
        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource(recursoFxml));
            Parent raiz = loader.load();
            Stage nueva = new Stage();
            Scene escena = new Scene(raiz, ancho, alto);
            var css = App.class.getResource("/css/fintech.css");
            if (css != null) escena.getStylesheets().add(css.toExternalForm());
            nueva.setTitle(titulo);
            nueva.setMinWidth(980);
            nueva.setMinHeight(680);
            nueva.setScene(escena);
            if (configurarControlador != null) configurarControlador.accept(loader.getController(), nueva);
            nueva.show();
            if (ventanaAnterior != null) ventanaAnterior.close();
            return nueva;
        } catch (IOException ex) {
            Alert alerta = new Alert(Alert.AlertType.ERROR);
            alerta.setTitle("Error de navegación");
            alerta.setHeaderText("No se pudo abrir la pantalla");
            alerta.setContentText("Recurso: " + recursoFxml + "\n" + ex.getMessage());
            alerta.showAndWait();
            return ventanaAnterior;
        }
    }

    private void iniciarProcesamientoAutomatico() {
        procesadorAutomatico = new Timeline(new KeyFrame(Duration.seconds(1), evento -> {
            int nuevasAlertas = contexto.getSistema().notificarOperacionesProximas(java.time.LocalDateTime.now());
            int ejecutadas = contexto.getSistema().procesarOperacionesProgramadas();
            if (nuevasAlertas > 0 || ejecutadas > 0) {
                try {
                    contexto.guardar();
                } catch (IOException ex) {
                    System.err.println("No se pudo guardar el procesamiento automático: " + ex.getMessage());
                }
            }
        }));
        procesadorAutomatico.setCycleCount(Timeline.INDEFINITE);
        procesadorAutomatico.play();
    }

    @Override
    public void stop() {
        if (procesadorAutomatico != null) procesadorAutomatico.stop();
        if (contexto != null) {
            try {
                contexto.guardar();
            } catch (IOException ex) {
                System.err.println("No se pudo guardar el estado al cerrar: " + ex.getMessage());
            }
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
