package co.edu.uniquindio.proyectofinal.Presentacion.ViewControllers;

import co.edu.uniquindio.proyectofinal.App;
import co.edu.uniquindio.proyectofinal.Estructuras.Servicios.ComparadorRendimiento;
import co.edu.uniquindio.proyectofinal.Estructuras.Servicios.ComparadorRendimiento.ResultadoComparacion;
import co.edu.uniquindio.proyectofinal.Presentacion.ContextoAplicacion;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Compara tabla hash, lista enlazada y árbol AVL en inserción y búsqueda de usuarios. */
public class RendimientoViewController {

    private static final int LIMITE = 10_000;

    @FXML private TextField txtUsuarios;
    @FXML private TextField txtConsultas;
    @FXML private Button btnEjecutar;
    @FXML private ProgressBar barraProgreso;
    @FXML private Label lblEstado;
    @FXML private Label lblConclusion;
    @FXML private BarChart<String, Number> graficaInsercion;
    @FXML private BarChart<String, Number> graficaBusqueda;
    @FXML private TableView<ResultadoComparacion> tablaResultados;
    @FXML private TableColumn<ResultadoComparacion, String> colEstructura, colElementos, colConsultas,
            colInsercion, colBusqueda, colAciertos;

    private ContextoAplicacion contexto;
    private Stage escenario;

    @FXML
    private void initialize() {
        colEstructura.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getEstructura()));
        colElementos.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getElementos())));
        colConsultas.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getConsultas())));
        colInsercion.setCellValueFactory(c -> new SimpleStringProperty(ms(c.getValue().getTiempoInsercionMilisegundos())));
        colBusqueda.setCellValueFactory(c -> new SimpleStringProperty(ms(c.getValue().getTiempoBusquedaMilisegundos())));
        colAciertos.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getAciertos())));
    }

    public void configurar(ContextoAplicacion contexto, Stage escenario) {
        this.contexto = contexto;
        this.escenario = escenario;
    }

    @FXML
    private void ejecutar() {
        int usuarios;
        int consultas;
        try {
            usuarios = Integer.parseInt(txtUsuarios.getText().trim());
            consultas = Integer.parseInt(txtConsultas.getText().trim());
        } catch (NumberFormatException ex) {
            lblEstado.setText("Escribe números enteros en ambos campos.");
            return;
        }
        if (usuarios <= 0 || consultas <= 0 || usuarios > LIMITE || consultas > LIMITE) {
            lblEstado.setText("Usa valores entre 1 y " + LIMITE + ".");
            return;
        }
        btnEjecutar.setDisable(true);
        barraProgreso.setProgress(ProgressBar.INDETERMINATE_PROGRESS);
        lblEstado.setText("Ejecutando comparación...");

        final int n = usuarios;
        final int q = consultas;
        Task<List<ResultadoComparacion>> tarea = new Task<>() {
            @Override
            protected List<ResultadoComparacion> call() {
                List<ResultadoComparacion> lista = new ArrayList<>();
                for (ResultadoComparacion r : new ComparadorRendimiento().compararBusquedaUsuarios(n, q)) {
                    lista.add(r);
                }
                return lista;
            }
        };
        tarea.setOnSucceeded(e -> Platform.runLater(() -> mostrarResultados(tarea.getValue())));
        tarea.setOnFailed(e -> Platform.runLater(() -> {
            btnEjecutar.setDisable(false);
            barraProgreso.setProgress(0);
            Throwable error = tarea.getException();
            lblEstado.setText("No se pudo ejecutar: " + (error == null ? "error desconocido" : error.getMessage()));
        }));
        Thread hilo = new Thread(tarea, "comparador-rendimiento");
        hilo.setDaemon(true);
        hilo.start();
    }

    private void mostrarResultados(List<ResultadoComparacion> resultados) {
        tablaResultados.setItems(FXCollections.observableArrayList(resultados));

        XYChart.Series<String, Number> insercion = new XYChart.Series<>();
        XYChart.Series<String, Number> busqueda = new XYChart.Series<>();
        ResultadoComparacion masRapida = null;
        for (ResultadoComparacion r : resultados) {
            insercion.getData().add(new XYChart.Data<>(r.getEstructura(), r.getTiempoInsercionMilisegundos()));
            busqueda.getData().add(new XYChart.Data<>(r.getEstructura(), r.getTiempoBusquedaMilisegundos()));
            if (masRapida == null || r.getTiempoBusquedaNanos() < masRapida.getTiempoBusquedaNanos()) {
                masRapida = r;
            }
        }
        graficaInsercion.getData().setAll(List.of(insercion));
        graficaBusqueda.getData().setAll(List.of(busqueda));

        btnEjecutar.setDisable(false);
        barraProgreso.setProgress(1);
        lblEstado.setText("Comparación terminada.");
        if (masRapida != null) {
            lblConclusion.setText("Búsqueda más rápida en esta ejecución: " + masRapida.getEstructura()
                    + ". La lista enlazada recorre elemento por elemento (O(n)); la tabla hash accede por clave "
                    + "(O(1) promedio) y el árbol AVL descarta mitades (O(log n)).");
        }
    }

    @FXML
    private void volverPanel() {
        App.mostrarVentana(escenario, contexto, "/fxml/admin.fxml", "NEXO · Administración", 1480, 940,
                (controller, stage) -> ((AdminViewController) controller).configurar(contexto, stage));
    }

    private String ms(double valor) {
        return String.format(Locale.forLanguageTag("es-CO"), "%.3f", valor);
    }
}
