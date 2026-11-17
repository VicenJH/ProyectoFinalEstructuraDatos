package co.edu.uniquindio.proyectofinal.Presentacion.ViewControllers;

import co.edu.uniquindio.proyectofinal.App;
import co.edu.uniquindio.proyectofinal.Presentacion.ContextoAplicacion;
import co.edu.uniquindio.proyectofinal.Presentacion.EtiquetasTabla;
import co.edu.uniquindio.proyectofinal.pruebas.PruebasRegresionFintech;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class TestMonitorViewController {

    public static final class FilaResultado {
        private final SimpleStringProperty nombre;
        private final SimpleStringProperty estado;
        private final SimpleStringProperty tiempo;
        private final SimpleStringProperty comprobaciones;
        private final SimpleStringProperty detalle;

        FilaResultado(PruebasRegresionFintech.Resultado resultado) {
            nombre = new SimpleStringProperty(resultado.nombre());
            estado = new SimpleStringProperty(resultado.exitoso() ? "APROBADO" : "FALLÓ");
            tiempo = new SimpleStringProperty(resultado.duracionMs() + " ms");
            comprobaciones = new SimpleStringProperty(String.valueOf(resultado.comprobaciones()));
            detalle = new SimpleStringProperty(resultado.detalle());
        }
        public SimpleStringProperty nombreProperty() { return nombre; }
        public SimpleStringProperty estadoProperty() { return estado; }
        public SimpleStringProperty tiempoProperty() { return tiempo; }
        public SimpleStringProperty comprobacionesProperty() { return comprobaciones; }
        public SimpleStringProperty detalleProperty() { return detalle; }
    }

    @FXML private Label lblTotalGrupos, lblAprobados, lblFallidos, lblDuracion, lblEstado, lblProgreso, lblResumen;
    @FXML private ProgressBar barraProgreso;
    @FXML private Button btnEjecutar;
    @FXML private TableView<FilaResultado> tablaResultados;
    @FXML private TableColumn<FilaResultado, String> colResultadoNombre, colResultadoEstado,
            colResultadoTiempo, colResultadoComprobaciones, colResultadoDetalle;

    private ContextoAplicacion contexto;
    private Stage escenario;
    private final ObservableList<FilaResultado> filas = FXCollections.observableArrayList();
    private long inicioEjecucion;
    private static final int TOTAL_GRUPOS = 12;

    @FXML
    private void initialize() {
        colResultadoNombre.setCellValueFactory(c -> c.getValue().nombreProperty());
        colResultadoEstado.setCellValueFactory(c -> c.getValue().estadoProperty());
        colResultadoTiempo.setCellValueFactory(c -> c.getValue().tiempoProperty());
        colResultadoComprobaciones.setCellValueFactory(c -> c.getValue().comprobacionesProperty());
        colResultadoDetalle.setCellValueFactory(c -> c.getValue().detalleProperty());
        EtiquetasTabla.aplicar(colResultadoEstado);
        tablaResultados.setItems(filas);
        lblTotalGrupos.setText(String.valueOf(TOTAL_GRUPOS));
    }

    public void configurar(ContextoAplicacion contexto, Stage escenario) {
        this.contexto = contexto;
        this.escenario = escenario;
    }

    @FXML
    private void ejecutarPruebas() {
        if (btnEjecutar.isDisabled()) return;
        filas.clear();
        lblAprobados.setText("0");
        lblFallidos.setText("0");
        lblDuracion.setText("En curso");
        lblEstado.setText("Ejecutando suite de regresión");
        lblProgreso.setText("Preparando casos de prueba...");
        btnEjecutar.setDisable(true);
        inicioEjecucion = System.nanoTime();
        AtomicInteger terminadas = new AtomicInteger();

        Task<List<PruebasRegresionFintech.Resultado>> tarea = new Task<>() {
            @Override
            protected List<PruebasRegresionFintech.Resultado> call() {
                return PruebasRegresionFintech.ejecutar(resultado -> {
                    int actual = terminadas.incrementAndGet();
                    updateProgress(actual, TOTAL_GRUPOS);
                    updateMessage("Grupo " + actual + " de " + TOTAL_GRUPOS + ": " + resultado.nombre());
                    Platform.runLater(() -> agregarResultado(resultado, actual));
                });
            }
        };
        barraProgreso.progressProperty().bind(tarea.progressProperty());
        lblProgreso.textProperty().bind(tarea.messageProperty());
        tarea.setOnSucceeded(evento -> {
            barraProgreso.progressProperty().unbind();
            lblProgreso.textProperty().unbind();
            barraProgreso.setProgress(1);
            btnEjecutar.setDisable(false);
            List<PruebasRegresionFintech.Resultado> resultados = tarea.getValue();
            long aprobadas = resultados.stream().filter(PruebasRegresionFintech.Resultado::exitoso).count();
            long fallidas = resultados.size() - aprobadas;
            long duracion = (System.nanoTime() - inicioEjecucion) / 1_000_000;
            lblAprobados.setText(String.valueOf(aprobadas));
            lblFallidos.setText(String.valueOf(fallidas));
            lblDuracion.setText(duracion + " ms");
            lblEstado.setText(fallidas == 0 ? "Todas las pruebas superadas" : "Se encontraron fallos");
            lblResumen.setText("" + aprobadas + " de " + resultados.size() + " grupos aprobados · "
                    + resultados.stream().mapToInt(PruebasRegresionFintech.Resultado::comprobaciones).sum()
                    + " comprobaciones ejecutadas.");
        });
        tarea.setOnFailed(evento -> {
            barraProgreso.progressProperty().unbind();
            lblProgreso.textProperty().unbind();
            btnEjecutar.setDisable(false);
            lblEstado.setText("La suite no pudo completarse");
            lblDuracion.setText(((System.nanoTime() - inicioEjecucion) / 1_000_000) + " ms");
            mostrar(Alert.AlertType.ERROR, "Error del monitor", String.valueOf(tarea.getException()));
        });
        Thread hilo = new Thread(tarea, "fintech-test-monitor");
        hilo.setDaemon(true);
        hilo.start();
    }

    private void agregarResultado(PruebasRegresionFintech.Resultado resultado, int actual) {
        filas.add(new FilaResultado(resultado));
        long aprobadas = filas.stream().filter(f -> "APROBADO".equals(f.estadoProperty().get())).count();
        long fallidas = filas.size() - aprobadas;
        lblAprobados.setText(String.valueOf(aprobadas));
        lblFallidos.setText(String.valueOf(fallidas));
        // lblProgreso está enlazado a Task.messageProperty mientras corre la suite.
    }

    @FXML private void limpiarResultados() {
        if (btnEjecutar.isDisabled()) return;
        filas.clear();
        barraProgreso.setProgress(0);
        lblAprobados.setText("0");
        lblFallidos.setText("0");
        lblDuracion.setText("0 ms");
        lblEstado.setText("Listo para ejecutar");
        lblProgreso.setText("12 grupos · miles de comprobaciones · tiempos por grupo");
        lblResumen.setText("Aún no se han ejecutado pruebas.");
    }

    @FXML private void volverInicio() {
        App.mostrarVentana(escenario, contexto, "/fxml/inicio.fxml", "NEXO · Billetera digital", 1120, 760,
                (controller, stage) -> ((LoginViewController) controller).configurar(contexto, stage));
    }

    private void mostrar(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alerta = new Alert(tipo); alerta.setTitle(titulo); alerta.setHeaderText(null); alerta.setContentText(mensaje); alerta.showAndWait();
    }
}
